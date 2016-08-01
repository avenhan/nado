package av.nado.network.http;

import java.util.ArrayList;
import java.util.List;

import av.nado.util.Check;
import av.rest.ApiRequest;
import av.util.exception.AException;

public class NadoFunctionRequest extends ApiRequest
{
    private int          paramType;
    private String       type;
    private String       method;
    private List<String> params    = new ArrayList<String>();
    
    public int getParamType()
    {
        return paramType;
    }
    
    public void setParamType(int paramType)
    {
        this.paramType = paramType;
    }
    
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
    
    @Override
    public void checkAuthorization() throws AException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void checkParameter() throws AException
    {
        if (Check.IfOneEmpty(type, method))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameters");
        }
    }
    
}
