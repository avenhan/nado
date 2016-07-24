package av.nado.proxy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;

public class TypeInfo
{
    private static Logger logger = LogManager.getLogger(ProxyManager.class);
    
    private Class<?>      type;
    private MethodAccess  methodAccess;
    private FieldAccess   fieldAccess;
    
    private InvokeProxy   proxy;
    
    public Class<?> getType()
    {
        return type;
    }
    
    public void setType(Class<?> type)
    {
        this.type = type;
        methodAccess = MethodAccess.get(type);
        try
        {
            fieldAccess = FieldAccess.get(type);
        }
        catch (Exception e)
        {
            logger.debug("interface ...", e);
        }
    }
    
    public MethodAccess getMethodAccess()
    {
        return methodAccess;
    }
    
    public void setMethodAccess(MethodAccess methodAccess)
    {
        this.methodAccess = methodAccess;
    }
    
    public FieldAccess getFieldAccess()
    {
        return fieldAccess;
    }
    
    public void setFieldAccess(FieldAccess fieldAccess)
    {
        this.fieldAccess = fieldAccess;
    }
    
    public InvokeProxy getProxy()
    {
        return proxy;
    }
    
    public void setProxy(InvokeProxy proxy)
    {
        this.proxy = proxy;
    }
    
}
