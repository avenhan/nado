package av.rest;

import av.util.exception.AException;

public abstract class ApiRequest
{
    private String accept;
    private String authorization;
    private String contentLength;
    private String host;
    private String userAgent;
    private String ipAddr;
    
    public abstract void checkAuthorization() throws AException;
    
    public abstract void checkParameter() throws AException;
    
    public String getAccept()
    {
        return accept;
    }
    
    public void setAccept(String accept)
    {
        this.accept = accept;
    }
    
    public String getAuthorization()
    {
        return authorization;
    }
    
    public void setAuthorization(String authorization)
    {
        this.authorization = authorization;
    }
    
    public String getContentLength()
    {
        return contentLength;
    }
    
    public void setContentLength(String contentLength)
    {
        this.contentLength = contentLength;
    }
    
    public String getHost()
    {
        return host;
    }
    
    public void setHost(String host)
    {
        this.host = host;
    }
    
    public String getUserAgent()
    {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }
    
    public String getIpAddr()
    {
        return ipAddr;
    }
    
    public void setIpAddr(String ipAddr)
    {
        this.ipAddr = ipAddr;
    }
}
