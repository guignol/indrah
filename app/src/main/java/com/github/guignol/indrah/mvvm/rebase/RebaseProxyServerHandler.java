package com.github.guignol.indrah.mvvm.rebase;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

class RebaseProxyServerHandler extends ChannelInboundHandlerAdapter {

    @NotNull
    private final Consumer<Path> pathHandler;

    RebaseProxyServerHandler(@NotNull Consumer<Path> pathHandler) {
        this.pathHandler = pathHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        final ByteBuf in = (ByteBuf) msg;
        final String string;
        try {
            System.out.println("edit ************************************");
            string = in.toString(CharsetUtil.UTF_8);
//            System.out.println(string);
            pathHandler.accept(Paths.get(string));
            System.out.println("***************************************");
        } finally {
            ReferenceCountUtil.release(msg);
        }
        // 打ち返す
        final ByteBuf returnBuffer = ctx.alloc().buffer().writeBytes(string.getBytes(CharsetUtil.UTF_8));
        ctx.writeAndFlush(returnBuffer);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
