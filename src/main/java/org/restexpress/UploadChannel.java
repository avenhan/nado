package org.restexpress;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restexpress.pipeline.ObserversUtil;
import org.restexpress.pipeline.Postprocessor;

import av.nado.util.Check;
import av.nado.util.JsonUtil;
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

public class UploadChannel extends SimpleChannelInboundHandler<Object>
{
    private boolean             isKeepAlive = false;
    private StreamNotify        objController;
    private Response            rspd        = new Response();
    private Request             rqst;
    private StreamFullRequest   fullRequest;
    private Map<String, String> mapWildcard = new HashMap<String, String>();
    
    @SuppressWarnings("deprecation")
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
                
                objController = (StreamNotify) RestExpress.uploadUri().getPathAttach(uri, mapWildcard);
                if (objController == null)
                {
                    /**
                     * If the request is not required to upload files, the data
                     * forwarding straight down a handler processing
                     */
                    ctx.fireChannelRead(msg);
                    return;
                }
                
                fullRequest = new StreamFullRequest(request);
                isKeepAlive = HttpHeaders.isKeepAlive(request);
                rqst = new Request((InetSocketAddress) ctx.channel().remoteAddress(), fullRequest, RestExpress.getInstance().createRouteResolver(),
                        RestExpress.getDefaultSerializationProvider());
                for (Map.Entry<String, String> entry : mapWildcard.entrySet())
                {
                    rqst.addHeader(entry.getKey(), entry.getValue());
                }
                
                String range = request.headers().get(HttpHeaders.Names.RANGE);
                if (Check.IfOneEmpty(range))
                {
                    range = "bytes=0-";
                    rqst.addHeader(HttpHeaders.Names.RANGE, range);
                }
                
                rqst.addHeader(HttpHeaderExt.HEADER_BYTE_CONTINUE, "true");
                ObserversUtil.observers(RestExpress.getInstance(), rqst, rspd, true, false, null);
                objController.upload(rqst, rspd);
            }
            else if (msg instanceof LastHttpContent && objController != null)
            {
                /**
                 * The HTTP request is divided into three parts, the last part,
                 * including the content data last
                 */
                LastHttpContent request = (LastHttpContent) msg;
                
                ByteBuf buffer = request.content();
                RestExpressUtil.setHeader(rqst, HttpHeaderExt.HEADER_BYTE_CONTINUE, "false");
                fullRequest.replace(buffer);
                
                rspd.setResponseCode(HttpResponseStatus.OK.code());
                Object ret = objController.upload(rqst, rspd);
                if (ret == null)
                {
                    ret = "";
                    sendResponse(ctx, rspd.getResponseStatus(), "");
                }
                else if (ret instanceof String)
                {
                    sendResponse(ctx, rspd.getResponseStatus(), (String) ret);
                }
                else
                {
                    sendResponse(ctx, rspd.getResponseStatus(), JsonUtil.toJson(ret));
                }
                
                ReferenceCountUtil.release(msg);
                
                ByteBuf emptyBuf = RestExpressUtil.emptyBuf();
                emptyBuf.writeBytes("upload data here ....".getBytes());
                fullRequest.replace(emptyBuf);
                rspd.setBody(ret);
                ObserversUtil.observers(RestExpress.getInstance(), rqst, rspd, false, true, null);
            }
            else if (msg instanceof HttpContent && objController != null)
            {
                /**
                 * The HTTP request is divided into three parts, the middle
                 * part, contains Chunked data upload
                 */
                HttpContent request = (HttpContent) msg;
                ByteBuf buffer = request.content();
                fullRequest.replace(buffer);
                rqst.addHeader(HttpHeaderExt.HEADER_BYTE_CONTINUE, "true");
                objController.upload(rqst, rspd);
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
            ByteBuf emptyBuf = RestExpressUtil.emptyBuf();
            emptyBuf.writeBytes("upload data here ....".getBytes());
            fullRequest.replace(emptyBuf);
            
            AException newE = new AException(AException.ERR_SERVER, e);
            sendResponse(ctx, newE);
            ObserversUtil.observers(RestExpress.getInstance(), rqst, rspd, false, false, newE);
        }
    }
    
    private void sendResponse(ChannelHandlerContext ctx, AException e)
    {
        rspd.setException(e);
        rspd.setResponseCode(e.getCode());
        
        List<Postprocessor> lstPostprocessors = RestExpress.getInstance().getFinallyProcessors();
        for (Postprocessor postprocessor : lstPostprocessors)
        {
            postprocessor.process(rqst, rspd);
        }
        
        if (rspd.getBody() == null)
        {
            try
            {
                rspd.setBody(JsonUtil.toJson(e));
            }
            catch (AException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        
        sendResponse(ctx, rspd.getResponseStatus(), rspd.getBody().toString());
    }
    
    private void sendResponse(ChannelHandlerContext ctx, HttpResponseStatus status, String body)
    {
        HttpResponse httpResponse = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer(body, CharsetUtil.UTF_8));
        
        httpResponse.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        httpResponse.headers().set(CONNECTION, "close");
        ctx.writeAndFlush(httpResponse).addListener(ChannelFutureListener.CLOSE);
    }
    
}
