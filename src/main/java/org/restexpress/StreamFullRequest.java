package org.restexpress;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

public class StreamFullRequest implements FullHttpRequest
{
    private HttpMethod  method;
    private String      uri;
    private HttpVersion httpVersion;
    private HttpHeaders headers;
    private ByteBuf     byteBuf;
    
    public StreamFullRequest(DefaultHttpRequest rqst)
    {
        setMethod(rqst.getMethod());
        setUri(rqst.getUri());
        this.headers = rqst.headers();
        this.httpVersion = rqst.getProtocolVersion();
    }
    
    public HttpMethod getMethod()
    {
        return method;
    }
    
    public HttpMethod method()
    {
        return method;
    }
    
    public String getUri()
    {
        return uri;
    }
    
    public String uri()
    {
        return uri;
    }
    
    public HttpVersion getProtocolVersion()
    {
        return httpVersion;
    }
    
    public HttpVersion protocolVersion()
    {
        return httpVersion;
    }
    
    public HttpHeaders headers()
    {
        // TODO Auto-generated method stub
        return headers;
    }
    
    public DecoderResult getDecoderResult()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public DecoderResult decoderResult()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void setDecoderResult(DecoderResult result)
    {
        // TODO Auto-generated method stub
        
    }
    
    public HttpHeaders trailingHeaders()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public ByteBuf content()
    {
        return byteBuf;
    }
    
    public int refCnt()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    public boolean release()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean release(int decrement)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    public FullHttpRequest copy()
    {
        // TODO Auto-generated method stub
        return this;
    }
    
    public FullHttpRequest duplicate()
    {
        // TODO Auto-generated method stub
        return this;
    }
    
    public FullHttpRequest retainedDuplicate()
    {
        // TODO Auto-generated method stub
        return this;
    }
    
    public FullHttpRequest replace(ByteBuf content)
    {
        this.byteBuf = content;
        return this;
    }
    
    public FullHttpRequest retain(int increment)
    {
        // TODO Auto-generated method stub
        return this;
    }
    
    public FullHttpRequest retain()
    {
        // TODO Auto-generated method stub
        return this;
    }
    
    public FullHttpRequest touch()
    {
        // TODO Auto-generated method stub
        return this;
    }
    
    public FullHttpRequest touch(Object hint)
    {
        return this;
    }
    
    public FullHttpRequest setProtocolVersion(HttpVersion version)
    {
        this.httpVersion = version;
        return this;
    }
    
    public FullHttpRequest setMethod(HttpMethod method)
    {
        this.method = method;
        return this;
    }
    
    public FullHttpRequest setUri(String uri)
    {
        this.uri = uri;
        return this;
    }
    
}
