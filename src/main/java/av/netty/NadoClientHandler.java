package av.netty;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import av.nado.remote.RemoteIp;
import av.util.trace.Trace;

public class NadoClientHandler extends SimpleChannelUpstreamHandler
{
    private static Logger    logger = LogManager.getLogger(NadoClientHandler.class);
    
    private RemoteIp         remoteIp;
    private NettyChannelInfo info;
    
    public NadoClientHandler(RemoteIp remoteIp)
    {
        this.remoteIp = remoteIp;
    }
    
    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
    {
        Channel channel = e.getChannel();
        info = NettyManager.instance().addConnected(remoteIp, channel, false);
        if (info != null)
        {
            channel.setAttachment(info);
        }
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
        
        // Trace.print("client receive: {}", (String) e.getMessage());
        
        info.recieve((String) e.getMessage());
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
    {
        Trace.print("client exception closed...");
        e.getCause().printStackTrace();
        e.getChannel().close();
        NettyManager.instance().removeServer(remoteIp);
    }
    
}
