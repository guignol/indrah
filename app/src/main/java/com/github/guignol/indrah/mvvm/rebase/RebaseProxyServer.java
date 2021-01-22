package com.github.guignol.indrah.mvvm.rebase;

import com.github.guignol.indrah.command.Command;
import com.github.guignol.swing.rx.EventStatus;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.reactivex.Single;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

public class RebaseProxyServer {

    public Single<EventStatus> start(@NotNull Function<Integer, Command> commandFactory,
                                     @NotNull Consumer<Path> pathHandler) {
        return Single.create(e -> {
            // Configure the server.
            final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            final EventLoopGroup workerGroup = new NioEventLoopGroup();
            final Runnable shutDown = () -> {
                // Shut down all event loops to terminate all threads.
                final boolean shutBoss = shutDown(bossGroup) != null;
                final boolean shutWork = shutDown(workerGroup) != null;
                if (shutBoss || shutWork) {
                    System.out.println("shutdown RebaseProxyServer");
                }
            };
            final ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
//                            p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(new RebaseProxyServerHandler(pathHandler) {
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    super.exceptionCaught(ctx, cause);
                                    shutDown.run();
                                }
                            });
                        }
                    });

            // Start the server.
            // 0を指定すると空いているポートを探してくれるっぽい
            // TODO 動的にすると、毎回セキュリティソフトに引っかかるので微妙
            final ChannelFuture f = b.bind(65187).sync();
            final int port = ((InetSocketAddress) f.channel().localAddress()).getPort();
            System.out.println("port: " + port);

            // 終了処理
            f.channel().closeFuture().addListener(future -> e.onSuccess(EventStatus.NEXT));

            System.out.println("git rebase begin: " + ZonedDateTime.now());
            final Command command = commandFactory.apply(port);
            if (command == null) {
                shutDown.run();
            } else {
                command.toSingle().subscribe((output, throwable) -> {
                    // 10秒以上かかったりするが、CLIでも同じなので諦める
                    System.out.println("git rebase end: " + ZonedDateTime.now());
                    if (throwable == null) {
                        output.print();
                    } else {
                        throwable.printStackTrace();
                    }
                    shutDown.run();
                });
            }
        });
    }

    @Nullable
    private static Future<?> shutDown(EventLoopGroup group) {
        if (group.isShutdown() || group.isShuttingDown()) {
            return null;
        }
        return group.shutdownGracefully();
    }
}
