import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.concurrent.atomic.AtomicInteger;

class RebaseProxyClient {

    int start(final String host, final int port, final String message) {

        final AtomicInteger exitStatus = new AtomicInteger(0);
        // Configure the client.
        final EventLoopGroup group = new NioEventLoopGroup();
        try {
            final Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
//                            p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(new RebaseProxyClientHandler(message) {
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                    exitStatus.set(1);
                                    super.exceptionCaught(ctx, cause);
                                }
                            });
                        }
                    });

            // Start the client.
            final ChannelFuture f = b.connect(host, port).sync();
            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            exitStatus.set(1);
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }

        final int exit = exitStatus.get();
        System.out.println("exit: " + exit);
        return exit;
    }
}
