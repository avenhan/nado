package av.rest;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restexpress.Request;
import org.restexpress.Response;

import av.nado.util.Check;
import av.util.exception.AException;

public class NadoRestController
{
    public static final String    KEY_X_FORWORD_FOR = "X-Forwarded-For";
    public static final String    KEY_REMOTE_PORT   = "REMOTE_PORT";
    public static final String    KEY_REAL_IP       = "X-Real-IP";
    
    private static Logger         logger            = LogManager.getLogger(NadoRestController.class);
    
    private Map<String, RestInfo> mapRestInfo       = new HashMap<String, RestInfo>();
    
    public <T extends ApiRequest> T initializeRequest(Class<T> clazz, Request request) throws AException
    {
        if (request == null)
        {
            logger.error("Request is null.");
            throw new AException(AException.ERR_INVALID_PARAMETER, "bad request.");
        }
        T rqst = null;
        try
        {
            rqst = request.getBodyAs(clazz);
            if (rqst == null)
            {
                rqst = clazz.newInstance();
            }
        }
        catch (Exception e)
        {
            logger.error("Request serialization failed. More info: {}.", e.toString());
            throw new AException(AException.ERR_INVALID_PARAMETER, e, "bad request.");
        }
        
        rqst.setAccept(request.getHeader("Accept"));
        rqst.setContentLength(request.getHeader("Content-Length"));
        rqst.setHost(request.getHeader("Host"));
        rqst.setUserAgent(request.getHeader("User-Agent"));
        
        SocketAddress remoteAddress = request.getRemoteAddress();
        String ipAddr = request.getHeader(KEY_X_FORWORD_FOR);
        if (Check.IfOneEmpty(ipAddr))
        {
            rqst.setIpAddr(remoteAddress.toString());
        }
        else
        {
            rqst.setIpAddr(ipAddr);
        }
        
        String auth = request.getHeader("Authorization");
        rqst.setAuthorization(auth);
        
        rqst.checkAuthorization();
        rqst.checkParameter();
        return rqst;
    }
    
    public Object create(Request request, Response response) throws Exception
    {
        String uri = request.getPath();
        StringBuilder b = new StringBuilder("post").append(uri);
        RestInfo info = mapRestInfo.get(b.toString());
        if (info == null)
        {
            throw new AException(AException.ERR_NOT_FOUND, "not found url: {}", b.toString());
        }
        
        ApiRequest rqst = initializeRequest(info.getRqstType(), request);
        return info.getMethodAccess().invoke(info.getController(), info.getMethodName(), rqst, response);
    }
    
    public Object read(Request request, Response response) throws Exception
    {
        String uri = request.getPath();
        StringBuilder b = new StringBuilder("get").append(uri);
        RestInfo info = mapRestInfo.get(b.toString());
        if (info == null)
        {
            throw new AException(AException.ERR_NOT_FOUND, "not found url: {}", b.toString());
        }
        
        ApiRequest rqst = initializeRequest(info.getRqstType(), request);
        return info.getMethodAccess().invoke(info.getController(), info.getMethodName(), rqst, response);
    }
    
    public Object update(Request request, Response response) throws Exception
    {
        String uri = request.getPath();
        StringBuilder b = new StringBuilder("put").append(uri);
        RestInfo info = mapRestInfo.get(b.toString());
        if (info == null)
        {
            throw new AException(AException.ERR_NOT_FOUND, "not found url: {}", b.toString());
        }
        
        ApiRequest rqst = initializeRequest(info.getRqstType(), request);
        return info.getMethodAccess().invoke(info.getController(), info.getMethodName(), rqst, response);
    }
    
    public Object delete(Request request, Response response) throws Exception
    {
        String uri = request.getPath();
        StringBuilder b = new StringBuilder("delete").append(uri);
        RestInfo info = mapRestInfo.get(b.toString());
        if (info == null)
        {
            throw new AException(AException.ERR_NOT_FOUND, "not found url: {}", b.toString());
        }
        
        ApiRequest rqst = initializeRequest(info.getRqstType(), request);
        return info.getMethodAccess().invoke(info.getController(), info.getMethodName(), rqst, response);
    }
    
    protected void addRestInformation(RestInfo info) throws AException
    {
        String b = new StringBuilder(info.getMethod()).append("/").append(info.getUri()).toString();
        RestInfo existInfo = mapRestInfo.get(b);
        if (existInfo != null)
        {
            throw new AException(AException.ERR_FATAL, "existed uri: {} http method: {} at: {}.{}", info.getUri(), info.getMethod(),
                    existInfo.getClass().getName(), existInfo.getMethodName());
        }
        
        mapRestInfo.put(b, info);
    }
    
    private void isPattenUri(String uri)
    {
        
    }
}
