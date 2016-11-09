package org.restexpress;

import java.util.HashMap;
import java.util.Map;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.ReferenceCountUtil;

public class DownloadChannel extends SimpleChannelInboundHandler<DefaultHttpRequest>
{
    private boolean      isKeepAlive = false;
    private StreamNotify objController;
    
    @SuppressWarnings("deprecation")
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultHttpRequest request) throws Exception
    {
        ReferenceCountUtil.retain(request);
        
        try
        {
            String uri = request.getUri();
            HttpMethod method = request.getMethod();
            if (method != HttpMethod.GET)
            {
                ctx.fireChannelRead(request);
                return;
            }
            
            Map<String, String> mapWildcard = new HashMap<String, String>();
            objController = (StreamNotify) RestExpress.downloadUri().getPathAttach(uri, mapWildcard);
            if (objController == null)
            {
                ctx.fireChannelRead(request);
                return;
            }
            
            isKeepAlive = HttpHeaders.isKeepAlive(request);
            String range = request.headers().get(HttpHeaders.Names.RANGE);
            
        }
        catch (Throwable e)
        {
            // TODO: handle exception
        }
    }
    
}
