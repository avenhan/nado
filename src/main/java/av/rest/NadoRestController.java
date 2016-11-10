package av.rest;

import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.restexpress.ContentType;
import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.StreamNotify;

import com.alibaba.fastjson.JSONObject;

import av.nado.util.Check;
import av.nado.util.JsonUtil;
import av.rest.preprocessor.NadoError;
import av.util.exception.AException;
import io.netty.buffer.ByteBuf;

public class NadoRestController implements StreamNotify
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
            RestInfo callInfo = null;
            if (Check.IfOneEmpty(mapMostLike))
            {
                callInfo = info;
            }
            else
            {
                String url = request.getPath();
                RestInfo otherInfo = mapMostLike.get(url);
                if (otherInfo == null)
                {
                    callInfo = info;
                }
                else
                {
                    callInfo = otherInfo;
                }
            }
            
            objRet = invokeRestInfo(request, response, callInfo);
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
    
    private Object invokeRestInfo(Request request, Response response, RestInfo info) throws Exception
    {
        RestParamInfo[] paramInfos = info.getParamInfos();
        Set<String> setNames = request.getHeaderNames();
        Map<String, String> mapHeaderNames = new HashMap<String, String>();
        for (String name : setNames)
        {
            mapHeaderNames.put(name.toLowerCase(), name);
        }
        
        boolean isRawData = info.isDownstream() || info.isUpstream();
        JSONObject jObject = null;
        Object[] objParam = new Object[paramInfos.length];
        for (int i = 0; i < paramInfos.length; i++)
        {
            RestParamInfo paramInfo = paramInfos[i];
            RestParam restParam = paramInfo.getKey();
            Class<?> paramType = paramInfo.getType();
            if (restParam == null)
            {
                if (paramType.equals(Response.class))
                {
                    objParam[i] = response;
                    continue;
                }
                
                if (paramType.equals(Request.class))
                {
                    objParam[i] = request;
                    continue;
                }
                
                Object obj = paramType.newInstance();
                if (!(obj instanceof ApiRequest))
                {
                    throw new AException(AException.ERR_SERVER, "invalid the {} parameter of method: {} in controller: {}", i, info.getMethodName(),
                            info.getController().getClass().getName());
                }
                
                ApiRequest objRequest = (ApiRequest) obj;
                ApiRequest rqst = initializeRequest(objRequest.getClass(), request);
                objParam[i] = rqst;
                
                continue;
            }
            
            String realName = mapHeaderNames.get(restParam.key().toLowerCase());
            if (!Check.IfOneEmpty(realName))
            {
                objParam[i] = JsonUtil.toTypeObject(paramType, request.getHeader(realName));
                continue;
            }
            
            if (isRawData)
            {
                if (restParam.required())
                {
                    throw new AException(AException.ERR_INVALID_PARAMETER, "not involved {} json data on method: {} in controller: {}",
                            restParam.key(), info.getMethodName(), info.getController().getClass().getName());
                }
                
                objParam[i] = explainObject(paramInfo, restParam.defaultValue());
                continue;
            }
            
            if (jObject == null)
            {
                ByteBuf bb = request.getBody();
                bb.readerIndex(0); // always set the reader index back to the
                                   // beginning
                String body = request.getBody().toString(ContentType.CHARSET);
                jObject = JSONObject.parseObject(body);
                if (jObject == null)
                {
                    throw new AException(AException.ERR_INVALID_PARAMETER, "invalid json data on method: {} in controller: {}", info.getMethodName(),
                            info.getController().getClass().getName());
                }
            }
            
            if (jObject.containsKey(restParam.key()))
            {
                Object obj = jObject.get(restParam.key());
                objParam[i] = explainObject(paramInfo, obj);
                continue;
            }
            
            if (restParam.required())
            {
                throw new AException(AException.ERR_INVALID_PARAMETER, "not involved {} json data on method: {} in controller: {}", restParam.key(),
                        info.getMethodName(), info.getController().getClass().getName());
            }
            
            objParam[i] = explainObject(paramInfo, restParam.defaultValue());
        }
        
        return info.getMethodAccess().invoke(info.getController(), info.getMethodName(), objParam);
    }
    
    private Object explainObject(RestParamInfo paramInfo, Object obj) throws Exception
    {
        Class<?> paramType = paramInfo.getType();
        if (paramType.isPrimitive())
        {
            if (Check.IfOneEmpty(obj))
            {
                if (paramType.equals(Boolean.class) || paramType.equals(boolean.class))
                {
                    return false;
                }
                if (paramType.equals(Double.class) || paramType.equals(double.class) || paramType.equals(Float.class)
                        || paramType.equals(float.class))
                {
                    return 0.0;
                }
                if (paramType.equals(char.class) || paramType.equals(Character.class))
                {
                    return (char) 0;
                }
                if (paramType.equals(short.class) || paramType.equals(Short.class))
                {
                    return (short) 0;
                }
                if (paramType.equals(byte.class) || paramType.equals(Byte.class))
                {
                    return (byte) 0;
                }
                else
                {
                    return 0;
                }
            }
            return JsonUtil.toTypeObject(paramType, obj);
        }
        
        if (paramType.isArray())
        {
            if (Check.IfOneEmpty(obj))
            {
                return JsonUtil.toTypeObject(paramType, "[]");
            }
            return JsonUtil.toTypeObject(paramType, obj);
        }
        
        List<Class<?>> lstContains = paramInfo.getContains();
        if (Check.IfOneEmpty(lstContains))
        {
            if (Check.IfOneEmpty(obj))
            {
                return null;
            }
            
            return JsonUtil.toTypeObject(paramType, obj);
        }
        
        if (paramType.equals(List.class) || paramType.equals(Collection.class))
        {
            if (Check.IfOneEmpty(obj))
            {
                return Collections.EMPTY_LIST;
            }
            
            return JsonUtil.toList(lstContains.get(0), JsonUtil.toJson(obj));
        }
        else if (paramType.equals(Set.class))
        {
            if (Check.IfOneEmpty(obj))
            {
                return Collections.EMPTY_SET;
            }
            return JsonUtil.toSet(lstContains.get(0), JsonUtil.toJson(obj), null);
        }
        else if (paramType.equals(Map.class))
        {
            if (Check.IfOneEmpty(obj))
            {
                return Collections.EMPTY_MAP;
            }
            return JsonUtil.toMap(lstContains.get(0), lstContains.get(1), JsonUtil.toJson(obj), null);
        }
        else
        {
            return JsonUtil.toTypeObject(paramType, obj);
        }
    }
    
    private <T extends ApiRequest> T initializeRequest(Class<T> clazz, Request request) throws AException
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
    
    private <T extends ApiRequest> void setRequestData(T rqst, Request request) throws AException
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
    
    public Object upload(Request request, Response response) throws Exception
    {
        Object objRet = null;
        RestInfo callInfo = null;
        if (Check.IfOneEmpty(mapMostLike))
        {
            callInfo = info;
        }
        else
        {
            String url = request.getPath();
            RestInfo otherInfo = mapMostLike.get(url);
            if (otherInfo == null)
            {
                callInfo = info;
            }
            else
            {
                callInfo = otherInfo;
            }
        }
        
        objRet = invokeRestInfo(request, response, callInfo);
        return objRet;
    }
    
    public Object download(Request request, Response response) throws Exception
    {
        return upload(request, response);
    }
}
