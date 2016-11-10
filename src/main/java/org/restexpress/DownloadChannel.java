package org.restexpress;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restexpress.pipeline.ObserversUtil;
import org.restexpress.pipeline.Postprocessor;

import av.nado.util.Check;
import av.nado.util.JsonUtil;
import av.util.exception.AException;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedNioStream;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class DownloadChannel extends SimpleChannelInboundHandler<DefaultHttpRequest>
{
    private boolean      isKeepAlive = false;
    private StreamNotify objController;
    private Response          rspd        = new Response();
    private Request           rqst;
    private StreamFullRequest fullRequest;
    
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
                if (!RestExpressUtil.isExistedUri(ctx, uri, method))
                {
                    return;
                }
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
            
            if (!request.getDecoderResult().isSuccess())
            {
                throw new AException(BAD_REQUEST.code(), "request decode error");
            }
            
            rqst = new Request((InetSocketAddress) ctx.channel().remoteAddress(), new StreamFullRequest(request),
                    RestExpress.getInstance().createRouteResolver(), RestExpress.getDefaultSerializationProvider());
            for (Map.Entry<String, String> entry : mapWildcard.entrySet())
            {
                rqst.addHeader(entry.getKey(), entry.getValue());
            }
            
            isKeepAlive = HttpHeaders.isKeepAlive(request);
            String range = request.headers().get(HttpHeaders.Names.RANGE);
            if (Check.IfOneEmpty(range))
            {
                range = "bytes=0-";
                rqst.addHeader(HttpHeaders.Names.RANGE, range);
            }
            
            ObserversUtil.observers(RestExpress.getInstance(), rqst, rspd, true, false, null);
            objController.upload(rqst, rspd);
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            if (isKeepAlive)
            {
                response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            }
            
            Set<String> headerNames = rspd.getHeaderNames();
            if (!Check.IfOneEmpty(headerNames))
            {
                for (String header : headerNames)
                {
                    response.headers().set(header, rspd.getHeader(header));
                }
            }
            
            Object obj = rspd.getBody();
            if (!(obj instanceof InputStream))
            {
                throw new Exception("invalid response body, should be inputstream when download method...");
            }
            
            InputStream is = (InputStream) obj;
            int length = is.available();
            
            if (!response.headers().contains(HttpHeaders.Names.CONTENT_LENGTH))
            {
                response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, length);
            }
            
            if (!response.headers().contains(HttpHeaders.Names.CONTENT_RANGE))
            {
                StringBuilder b = new StringBuilder("0-").append(length - 1);
                response.headers().set(HttpHeaders.Names.CONTENT_RANGE, b.toString());
            }
            
            ctx.write(response);
            sendStream(ctx, is);
            ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            
            rspd.setBody("download data here...");
            ObserversUtil.observers(RestExpress.getInstance(), rqst, rspd, false, true, null);
        }
        catch (Throwable e)
        {
            AException newE = new AException(AException.ERR_SERVER, e);
            sendResponse(ctx, newE);
            ObserversUtil.observers(RestExpress.getInstance(), rqst, rspd, false, false, newE);
        }
    }
    
    private void sendStream(ChannelHandlerContext ctx, final InputStream instream) throws IOException, AException
    {
        try
        {
            
            final ChunkedNioStream stream = new ChunkedNioStream(Channels.newChannel(instream));
            ChannelFuture sendFileFuture = ctx.write(stream, ctx.newProgressivePromise());
            
            sendFileFuture.addListener(new ChannelProgressiveFutureListener()
            {
                public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) throws Exception
                {
                }
                
                public void operationComplete(ChannelProgressiveFuture future) throws Exception
                {
                    instream.close();
                }
            });
        }
        catch (Exception e)
        {
            instream.close();
            throw new AException(INTERNAL_SERVER_ERROR.code(), e);
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
