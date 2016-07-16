package av.nado.remote;

import java.util.ArrayList;
import java.util.List;

import av.util.exception.AException;

public class NadoWrap
{
    public static final int KEY_PARAM_TYPE_JSON      = 0;
    public static final int KEY_PARAM_TYPE_SERIALIZE = 1;
    public static final int KEY_PARAM_TYPE_HESSIAN   = 2;
    
    private int             paramType                = KEY_PARAM_TYPE_JSON;
    private String          type;
    private String          method;
    private List<String>    params                   = new ArrayList<String>();
    
    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    public String getMethod()
    {
        return method;
    }
    
    public void setMethod(String method)
    {
        this.method = method;
    }
    
    public List<String> getParams()
    {
        return params;
    }
    
    public void setParams(List<String> params)
    {
        this.params = params;
    }
    
    public int getParamType()
    {
        return paramType;
    }
    
    public void setParamType(int paramType)
    {
        this.paramType = paramType;
    }
    
    public void addParam(Object param) throws AException
    {
        String explain = "";
        switch (this.paramType)
        {
            case KEY_PARAM_TYPE_JSON:
                explain = NadoParam.toExplain(param);
                break;
            case KEY_PARAM_TYPE_SERIALIZE:
                explain = NadoParam.serialized(param);
                break;
            case KEY_PARAM_TYPE_HESSIAN:
                explain = NadoParam.hessianEncode(param);
                break;
            default:
                throw new AException(AException.ERR_SERVER, "unknow serialized type: {}", this.paramType);
        }
        
        this.params.add(explain);
    }
}
