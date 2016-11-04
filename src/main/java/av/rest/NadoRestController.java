package av.rest;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restexpress.Request;
import org.restexpress.Response;

import av.nado.util.Check;
import av.rest.preprocessor.NadoError;
import av.util.exception.AException;

public class NadoRestController
{
    public static final String                             KEY_X_FORWORD_FOR   = "X-Forwarded-For";
    public static final String                             KEY_REMOTE_PORT     = "REMOTE_PORT";
    public static final String                             KEY_REAL_IP         = "X-Real-IP";
    
    private static Logger                                  logger              = LogManager.getLogger(NadoRestController.class);
    private RestInfo                                       info;
    private Map<String, RestInfo>                          mapRestInfo         = new HashMap<String, RestInfo>();
    
    private final static ConcurrentHashMap<String, Method> cachedMethodByClass = new ConcurrentHashMap<String, Method>();
    
    private Map<String, RestInfo>                          mapMostLike         = new HashMap<String, RestInfo>();
    
    public RestInfo getInfo()
    {
        return info;
    }
    
    public void setInfo(RestInfo info)
    {
        this.info = info;
    }
    
    public void addMostLike(Map<String, RestInfo> map)
    {
        if (Check.IfOneEmpty(map))
        {
            return;
        }
        
        for (Map.Entry<String, RestInfo> entry : map.entrySet())
        {
            if (entry.getKey().charAt(0) == '/')
            {
                mapMostLike.put(entry.getKey(), entry.getValue());
            }
            else
            {
                mapMostLike.put(new StringBuilder("/").append(entry.getKey()).toString(), entry.getValue());
            }
        }
    }
    
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
        
        setRequestData(rqst, request);
        
        rqst.checkAuthorization();
        rqst.checkParameter();
        return rqst;
    }
    
    public <T extends ApiRequest> void setRequestData(T rqst, Request request) throws AException
    {
        Set<String> setNames = request.getHeaderNames();
        setNames.remove("Accept");
        setNames.remove("Authorization");
        setNames.remove("Content-Length");
        setNames.remove("Host");
        setNames.remove("User-Agent");
        setNames.remove("UserAuth");
        if (setNames == null || setNames.isEmpty())
        {
            return;
        }
        
        ClassNameCache nameCache = new ClassNameCache();
        nameCache.setClazz(rqst.getClass());
        
        for (String name : setNames)
        {
            nameCache.setName(name);
            String value = request.getHeader(name);
            Object rightValue = null;
            Method method = getMethodByName(nameCache, name);
            if (method == null)
            {
                continue;
            }
            String parameterTypes = method.getParameterTypes()[0].getName();
            if (parameterTypes.equalsIgnoreCase("java.lang.String"))
            {
                rightValue = value;
            }
            else if (parameterTypes.equalsIgnoreCase("int") || parameterTypes.equals("java.lang.Integer"))
            {
                rightValue = Integer.parseInt(value);
            }
            else if (parameterTypes.equalsIgnoreCase("Long") || parameterTypes.equals("java.lang.Long"))
            {
                rightValue = Long.parseLong(value);
            }
            else if (parameterTypes.equalsIgnoreCase("Boolean") || parameterTypes.equalsIgnoreCase("java.lang.Boolean"))
            {
                rightValue = Boolean.parseBoolean(value);
            }
            else
            {
                rightValue = value;
            }
            try
            {
                method.invoke(rqst, rightValue);
            }
            catch (Exception e)
            {
                logger.error("Invalid request. More info: {}", e.toString());
                throw new AException(400, "request is invalid");
            }
        }
    }
    
    public Method getMethodByName(ClassNameCache nameCache, String name)
    {
        Method method = cachedMethodByClass.get(nameCache.toString());
        if (method != null)
        {
            return method;
        }
        else
        {
            Method[] methodArray = nameCache.getClazz().getMethods();
            for (int i = 0; i < methodArray.length; i++)
            {
                StringBuilder builder = new StringBuilder("set").append(name.substring(0, 1).toUpperCase()).append(name.substring(1));
                if (methodArray[i].getName().equalsIgnoreCase(builder.toString()))
                {
                    method = methodArray[i];
                    cachedMethodByClass.put(nameCache.toString(), method);
                    return method;
                }
            }
        }
        return null;
    }
    
    public Object head(Request request, Response response) throws Exception
    {
        return create(request, response);
    }
    
    public Object create(Request request, Response response) throws Exception
    {
        Object objRet = null;
        try
        {
            if (Check.IfOneEmpty(mapMostLike))
            {
                ApiRequest rqst = initializeRequest(info.getRqstType(), request);
                objRet = info.getMethodAccess().invoke(info.getController(), info.getMethodName(), rqst, response);
            }
            else
            {
                String url = request.getPath();
                RestInfo otherInfo = mapMostLike.get(url);
                if (otherInfo == null)
                {
                    ApiRequest rqst = initializeRequest(info.getRqstType(), request);
                    objRet = info.getMethodAccess().invoke(info.getController(), info.getMethodName(), rqst, response);
                }
                else
                {
                    ApiRequest rqst = initializeRequest(otherInfo.getRqstType(), request);
                    objRet = otherInfo.getMethodAccess().invoke(otherInfo.getController(), otherInfo.getMethodName(), rqst, response);
                }
            }
        }
        catch (AException e)
        {
            NadoError err = new NadoError();
            err.setCode(e.getCode());
            err.setId(e.getId());
            err.setMessage(e.getClientMessage());
            
            response.setResponseCode(e.getCode());
            objRet = err;
        }
        catch (Throwable ea)
        {
            AException e = new AException(AException.ERR_SERVER, ea);
            NadoError err = new NadoError();
            err.setCode(e.getCode());
            err.setId(e.getId());
            err.setMessage(e.getClientMessage());
            
            response.setResponseCode(e.getCode());
            objRet = err;
        }
        
        return objRet;
    }
    
    public Object read(Request request, Response response) throws Exception
    {
        return create(request, response);
    }
    
    public Object update(Request request, Response response) throws Exception
    {
        return create(request, response);
    }
    
    public Object delete(Request request, Response response) throws Exception
    {
        // String uri = request.getPath();
        // StringBuilder b = new StringBuilder("delete").append(uri);
        // RestInfo info = mapRestInfo.get(b.toString());
        // if (info == null)
        // {
        // throw new AException(AException.ERR_NOT_FOUND, "not found url: {}",
        // b.toString());
        // }
        
        return create(request, response);
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
}
