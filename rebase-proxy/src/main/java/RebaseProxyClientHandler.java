import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class RebaseProxyClientHandler extends ChannelInboundHandlerAdapter {

    private final String message;

    /**
     * Creates a client-side handler.
     */
    RebaseProxyClientHandler(String message) {
        this.message = message;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("out ************************************");
        System.out.println(message);
        System.out.println("****************************************");
        final ByteBuf byteBuf = ctx.alloc().buffer().writeBytes(message.getBytes(CharsetUtil.UTF_8));
        ctx.writeAndFlush(byteBuf);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        final ByteBuf in = (ByteBuf) msg;
        try {
            System.out.println("in ************************************");
            System.out.println(in.toString(CharsetUtil.UTF_8));
            System.out.println("***************************************");
        } finally {
            ReferenceCountUtil.release(msg);
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
