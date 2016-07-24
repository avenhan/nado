package av.nado.proxy;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.esotericsoftware.reflectasm.MethodAccess;

import av.nado.util.Check;
import av.util.exception.AException;

public class ProxyManager
{
    private static Logger           logger     = LogManager.getLogger(ProxyManager.class);
    
    private Map<Class<?>, TypeInfo> m_mapTypes = new HashMap<Class<?>, TypeInfo>();
    private static ProxyManager     m_pThis;
    
    public static ProxyManager instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new ProxyManager();
        }
        
        return m_pThis;
    }
    
    /**
     * 
     * parameters like as: interface type, String; interface type, String;
     * interface type, String;
     * 
     */
    public void setTypes(Object... interfaceTypeAndRemotes) throws AException
    {
        if (interfaceTypeAndRemotes == null || interfaceTypeAndRemotes.length % 2 == 1)
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        Map<Class<?>, String> mapTypes = new HashMap<Class<?>, String>();
        for (int i = 0; i < interfaceTypeAndRemotes.length; i += 2)
        {
            Class<?> type = null;
            String remote = null;
            for (int j = i; j <= i + 1; j++)
            {
                Object objGot = interfaceTypeAndRemotes[j];
                if (objGot instanceof Class<?>)
                {
                    type = (Class<?>) objGot;
                }
                
                if (objGot instanceof String)
                {
                    remote = (String) objGot;
                }
            }
            
            if (Check.IfOneEmpty(type, remote))
            {
                throw new AException(AException.ERR_SERVER, "invalid parameter");
            }
            
            mapTypes.put(type, remote);
        }
        
        setTypes(mapTypes);
    }
    
    public void setTypes(Map<Class<?>, String> mapTypes) throws AException
    {
        if (Check.IfOneEmpty(mapTypes))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        for (Map.Entry<Class<?>, String> entry : mapTypes.entrySet())
        {
            Class<?> type = entry.getKey();
            InvokeProxy proxy = initializeProxy(type, entry.getValue());
            
            TypeInfo info = new TypeInfo();
            info.setType(type);
            info.setProxy(proxy);
            
            m_mapTypes.put(type, info);
        }
    }
    
    public Object call(Class<?> type, String method, Object... args) throws AException
    {
        if (Check.IfOneEmpty(type, method))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        TypeInfo info = m_mapTypes.get(type);
        if (info == null)
        {
            throw new AException(AException.ERR_SERVER, "not found type: {}", type.getName());
        }
        
        Object objProxy = info.getProxy().getObjProxy();
        MethodAccess methodAccess = info.getMethodAccess();
        int index = methodAccess.getIndex(method);
        if (index < 0)
        {
            throw new AException(AException.ERR_SERVER, "not found method: {} of type: {}", method, type.getName());
        }
        
        return methodAccess.invoke(objProxy, index, args);
    }
    
    public <T> T get(Class<T> type) throws AException
    {
        if (Check.IfOneEmpty(type))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        TypeInfo info = m_mapTypes.get(type);
        if (info == null)
        {
            throw new AException(AException.ERR_SERVER, "not found type: {}", type.getName());
        }
        
        Object objProxy = info.getProxy().getObjProxy();
        return type.cast(objProxy);
    }
    
    private InvokeProxy initializeProxy(Class<?> type, String remoteType) throws AException
    {
        if (Check.IfOneEmpty(type, remoteType))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        Class<?>[] types = new Class<?>[1];
        types[0] = type;
        
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InvokeProxy proxy = new InvokeProxy();
        proxy.setInterfaceType(type);
        proxy.setRemoteType(remoteType);
        
        Object objProxy = Proxy.newProxyInstance(loader, types, proxy);
        proxy.setObjProxy(objProxy);
        
        return proxy;
    }
}
