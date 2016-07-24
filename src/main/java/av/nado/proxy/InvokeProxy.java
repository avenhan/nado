package av.nado.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import av.nado.remote.NadoRemote;
import av.util.trace.Trace;

public class InvokeProxy implements InvocationHandler
{
    private String   remoteType;
    private Class<?> interfaceType;
    
    private Object   objProxy;
    
    public String getRemoteType()
    {
        return remoteType;
    }
    
    public void setRemoteType(String remoteType)
    {
        this.remoteType = remoteType;
    }
    
    public Class<?> getInterfaceType()
    {
        return interfaceType;
    }
    
    public void setInterfaceType(Class<?> interfaceType)
    {
        this.interfaceType = interfaceType;
    }
    
    public Object getObjProxy()
    {
        return objProxy;
    }
    
    public void setObjProxy(Object objProxy)
    {
        this.objProxy = objProxy;
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        String methodName = method.getName();
        Trace.print("type: {} method: {}", remoteType, methodName);
        
        return NadoRemote.instance().invoke(remoteType, methodName, args);
    }
}
