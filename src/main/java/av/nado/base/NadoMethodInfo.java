package av.nado.base;

import java.lang.reflect.Method;

public class NadoMethodInfo
{
    public Method     method;
    public Class<?>[] params;
    
    public Method getMethod()
    {
        return method;
    }
    
    public void setMethod(Method method)
    {
        this.method = method;
        this.params = method.getParameterTypes();
    }
    
    public Class<?>[] getParams()
    {
        return params;
    }
    
    public void setParams(Class<?>[] params)
    {
        this.params = params;
    }
    
}
