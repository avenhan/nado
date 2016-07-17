package av.netty;

import java.net.SocketAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import av.util.trace.Trace;

public class NadoServerHandler extends SimpleChannelHandler
{
    private static Logger logger = LogManager.getLogger(NadoServerHandler.class);
    
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    {
        Channel channel = e.getChannel();
        SocketAddress address = channel.getRemoteAddress();
        NettyChannelInfo info = NettyManager.instance().addConnected(address, channel, true);
        if (info != null)
        {
            channel.setAttachment(info);
        }
        Trace.print("I'm server. receive client connected...at: {}", address);
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
    {
        Channel channel = e.getChannel();
        NettyChannelInfo info = (NettyChannelInfo) channel.getAttachment();
        if (info == null)
        {
            channel.close();
            logger.debug("can not get channel info at client: {}", channel.getRemoteAddress());
            return;
        }
        
        // Trace.print("server receive: {}", (String) e.getMessage());
        info.recieve((String) e.getMessage());
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
    {
        Trace.print("server exception closed...");
        e.getCause().printStackTrace();
        Channel ch = e.getChannel();
        ch.close();
    }
    
}
