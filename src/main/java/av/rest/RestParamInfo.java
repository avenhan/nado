package av.rest;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class RestParamInfo
{
    private String         name;
    private Class<?>       type;
    private List<Class<?>> contains = new ArrayList<Class<?>>();
    private RestParam      key;
    private Parameter      param;
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public Class<?> getType()
    {
        return type;
    }
    
    public void setType(Class<?> type)
    {
        this.type = type;
    }
    
    public List<Class<?>> getContains()
    {
        return contains;
    }
    
    public void setContains(List<Class<?>> contains)
    {
        this.contains = contains;
    }
    
    public RestParam getKey()
    {
        return key;
    }
    
    public void setKey(RestParam key)
    {
        this.key = key;
    }
    
    public Parameter getParam()
    {
        return param;
    }
    
    public void setParam(Parameter param)
    {
        this.param = param;
    }
    
}
