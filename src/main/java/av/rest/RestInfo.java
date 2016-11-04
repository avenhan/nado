package av.rest;

import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.reflectasm.MethodAccess;

import io.netty.handler.codec.http.HttpMethod;

public class RestInfo
{
    private static Map<String, HttpMethod> mapHttpMethod = new HashMap<String, HttpMethod>();
    
    static
    {
        mapHttpMethod.put("get", HttpMethod.GET);
        mapHttpMethod.put("post", HttpMethod.POST);
        mapHttpMethod.put("put", HttpMethod.PUT);
        mapHttpMethod.put("delete", HttpMethod.DELETE);
        mapHttpMethod.put("head", HttpMethod.HEAD);
    }
    
    private Class<?>                    type;
    private String                      uri;
    private String                      method;
    private MethodAccess                methodAccess;
    private Object                      controller;
    private String                      methodName;
    private Class<? extends ApiRequest> rqstType;
    
    private String[]                    arrPaths;
    private boolean                     hasPattern = false;
    
    private Class<?>[]                  paramTypes;
    
    public Class<?> getType()
    {
        return type;
    }
    
    public void setType(Class<?> type)
    {
        this.type = type;
        this.methodAccess = MethodAccess.get(type);
    }
    
    public String getUri()
    {
        return uri;
    }
    
    public void setUri(String uri)
    {
        this.uri = uri;
        this.arrPaths = uri.split("/");
        for (String path : arrPaths)
        {
            if (path.charAt(0) == '{' && path.charAt(path.length() - 1) == '}')
            {
                this.hasPattern = true;
                break;
            }
        }
    }
    
    public String getMethod()
    {
        return method;
    }
    
    @SuppressWarnings("static-access")
    public HttpMethod getHttpMethod()
    {
        HttpMethod httpMethod = mapHttpMethod.get(method);
        if (httpMethod == null)
        {
            return httpMethod.GET;
        }
        
        return httpMethod;
    }
    
    public void setMethod(String method)
    {
        this.method = method;
    }
    
    public MethodAccess getMethodAccess()
    {
        return methodAccess;
    }
    
    public void setMethodAccess(MethodAccess methodAccess)
    {
        this.methodAccess = methodAccess;
    }
    
    public Object getController()
    {
        return controller;
    }
    
    public void setController(Object controller)
    {
        this.controller = controller;
    }
    
    public String getMethodName()
    {
        return methodName;
    }
    
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }
    
    public Class<? extends ApiRequest> getRqstType()
    {
        return rqstType;
    }
    
    public void setRqstType(Class<? extends ApiRequest> rqstType)
    {
        this.rqstType = rqstType;
    }
    
    public String[] getArrPaths()
    {
        return arrPaths;
    }
    
    public void setArrPaths(String[] arrPaths)
    {
        this.arrPaths = arrPaths;
    }
    
    public boolean isHasPattern()
    {
        return hasPattern;
    }
    
    public void setHasPattern(boolean hasPattern)
    {
        this.hasPattern = hasPattern;
    }
    
    public Class<?>[] getParamTypes()
    {
        return paramTypes;
    }
    
    public void setParamTypes(Class<?>[] paramTypes)
    {
        this.paramTypes = paramTypes;
    }
    
}
