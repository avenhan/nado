package av.nado.base;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.reflectasm.MethodAccess;

import av.util.exception.AException;

public class NadoInfo<T>
{
    private Class<T>                          type;
    private String                            name;
    private List<Method>                      lstMethods        = new ArrayList<Method>();
    private Map<String, List<NadoMethodInfo>> mapMethodExplains = new HashMap<String, List<NadoMethodInfo>>();
    private T                                 object;
    
    private MethodAccess                      methodAccess;
    
    public Class<T> getType()
    {
        return type;
    }
    
    public void setType(Class<T> type) throws AException
    {
        this.type = type;
        this.name = type.getName();
        try
        {
            this.object = type.newInstance();
        }
        catch (InstantiationException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_SERVER, e);
        }
        
        methodAccess = MethodAccess.get(type);
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public List<Method> getLstMethods()
    {
        return lstMethods;
    }
    
    public void setLstMethods(List<Method> lstMethods)
    {
        this.lstMethods = lstMethods;
    }
    
    public Map<String, List<NadoMethodInfo>> getMapMethodExplains()
    {
        return mapMethodExplains;
    }
    
    public void setMapMethodExplains(Map<String, List<NadoMethodInfo>> mapMethodExplains)
    {
        this.mapMethodExplains = mapMethodExplains;
    }
    
    public T getObject()
    {
        return object;
    }
    
    public void setObject(T object)
    {
        this.object = object;
    }
    
    public MethodAccess getMethodAccess()
    {
        return methodAccess;
    }
    
    public void setMethodAccess(MethodAccess methodAccess)
    {
        this.methodAccess = methodAccess;
    }
    
    public void addMethod(Method method)
    {
        if (method == null)
        {
            return;
        }
        this.lstMethods.add(method);
        
        String methodName = method.getName();
        // Class<?> retType = method.getReturnType();
        Class<?>[] paramTypes = method.getParameterTypes();
        
        StringBuilder b = new StringBuilder(methodName);
        for (Class<?> param : paramTypes)
        {
            b.append("#").append(param.getName());
        }
        
        List<NadoMethodInfo> lstMethods = mapMethodExplains.get(methodName);
        if (lstMethods == null)
        {
            lstMethods = new ArrayList<NadoMethodInfo>();
            mapMethodExplains.put(methodName, lstMethods);
        }
        
        NadoMethodInfo methodInfo = new NadoMethodInfo();
        methodInfo.setMethod(method);
        
        lstMethods.add(methodInfo);
    }
}
