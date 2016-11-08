package org.restexpress;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.util.HashMap;
import java.util.Map;

import av.util.exception.AException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class UploadChannel extends SimpleChannelInboundHandler<Object>FullHttpRequest
{
    private boolean isKeepAlive = false;
    private Object  objController;
    private Response            rspd        = new Response();
    private Map<String, String> mapWildcard = new HashMap<String, String>();
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        ReferenceCountUtil.retain(msg);
        try
        {
            if (msg instanceof DefaultHttpRequest)
            {
                /**
                 * The HTTP request is divided into three parts, the first part
                 * here, mostly the head analysis
                 */
                DefaultHttpRequest request = (DefaultHttpRequest) msg;
                String uri = request.getUri();
                HttpMethod method = request.getMethod();
                if (method != HttpMethod.PUT)
                {
                    ctx.fireChannelRead(msg);
                    return;
                }
                
                objController = RestExpress.uploadUri().getPathAttach(uri, mapWildcard);
                if (objController == null)
                {
                    /**
                     * If the request is not required to upload files, the data
                     * forwarding straight down a handler processing
                     */
                    ctx.fireChannelRead(msg);
                    return;
                }
                
                isKeepAlive = HttpHeaders.isKeepAlive(request);
                
                // Request rqst = new Request(null, null);
                // for (Entry<String, String> entry :
                // request.headers().entries())
                // {
                // rqst.addHeader(entry.getKey(), entry.getValue());
                // }
                //
                // for (Map.Entry<String, String> entry :
                // mapWildcard.entrySet())
                // {
                // rqst.addHeader(entry.getKey(), entry.getValue());
                // }
                
            }
            else if (msg instanceof LastHttpContent && objController != null)
            {
                /**
                 * The HTTP request is divided into three parts, the last part,
                 * including the content data last
                 */
                LastHttpContent request = (LastHttpContent) msg;
                
                ByteBuf buffer = request.content();
                int comeBuffSize = buffer.readableBytes();
                System.out.println("last comme buffer size: " + comeBuffSize);
                
                sendResponse(ctx, HttpResponseStatus.OK, "ok...");
                ReferenceCountUtil.release(msg);
            }
            else if (msg instanceof HttpContent && objController != null)
            {
                /**
                 * The HTTP request is divided into three parts, the middle
                 * part, contains Chunked data upload
                 */
                HttpContent request = (HttpContent) msg;
                ByteBuf buffer = request.content();
                int comeBuffSize = buffer.readableBytes();
                System.out.println("comme buffer size: " + comeBuffSize);
                
                ReferenceCountUtil.release(msg);
            }
            else
            {
                /**
                 * If the request is not required to upload files, the data
                 * forwarding straight down a handler processing
                 */
                ctx.fireChannelRead(msg);
            }
        }
        catch (Throwable e)
        {
            AException newE = new AException(AException.ERR_SERVER, e);
            sendResponse(ctx, newE);
        }
    }
    
    private void sendResponse(ChannelHandlerContext ctx, AException e)
    {
        
    }
    
    private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String body)
    {
        HttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(body, CharsetUtil.UTF_8));
        
        httpResponse.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        httpResponse.headers().set(CONNECTION, "close");
        ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
    }
}
