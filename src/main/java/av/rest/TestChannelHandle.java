package av.rest;

import java.net.SocketAddress;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.EventExecutor;

public class TestChannelHandle implements ChannelHandlerContext
{

    public ChannelFuture bind(SocketAddress arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture bind(SocketAddress arg0, ChannelPromise arg1)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture close()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture close(ChannelPromise arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture connect(SocketAddress arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture connect(SocketAddress arg0, SocketAddress arg1)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture connect(SocketAddress arg0, ChannelPromise arg1)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture connect(SocketAddress arg0, SocketAddress arg1, ChannelPromise arg2)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture deregister()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture deregister(ChannelPromise arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture disconnect()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture disconnect(ChannelPromise arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture newFailedFuture(Throwable arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelProgressivePromise newProgressivePromise()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelPromise newPromise()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture newSucceededFuture()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelPromise voidPromise()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture write(Object arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture write(Object arg0, ChannelPromise arg1)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture writeAndFlush(Object arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelFuture writeAndFlush(Object arg0, ChannelPromise arg1)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ByteBufAllocator alloc()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public <T> Attribute<T> attr(AttributeKey<T> arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public Channel channel()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public EventExecutor executor()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext fireChannelActive()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext fireChannelInactive()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext fireChannelRead(Object arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext fireChannelReadComplete()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext fireChannelRegistered()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext fireChannelUnregistered()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext fireChannelWritabilityChanged()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext fireExceptionCaught(Throwable arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext fireUserEventTriggered(Object arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext flush()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandler handler()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public <T> boolean hasAttr(AttributeKey<T> arg0)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean isRemoved()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    public String name()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelPipeline pipeline()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ChannelHandlerContext read()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
