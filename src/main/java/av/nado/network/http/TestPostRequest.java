package av.nado.network.http;

import av.rest.ApiRequest;
import av.util.exception.AException;

public class TestPostRequest extends ApiRequest
{
    private String type;
    
    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    @Override
    public void checkAuthorization() throws AException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void checkParameter() throws AException
    {
        // TODO Auto-generated method stub
        
    }
    
}
