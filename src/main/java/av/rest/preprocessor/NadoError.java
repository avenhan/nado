package av.rest.preprocessor;

import av.util.exception.AException;

public class NadoError
{
    private int    code;
    private String message;
    private String id;
    
    public static NadoError getError(AException e)
    {
        NadoError error = new NadoError();
        error.setCode(e.getCode());
        error.setId(e.getId());
        error.setMessage(e.getClientMessage());
        
        return error;
    }
    
    public int getCode()
    {
        return code;
    }
    
    public void setCode(int code)
    {
        this.code = code;
    }
    
    public String getMessage()
    {
        return message;
    }
    
    public void setMessage(String message)
    {
        this.message = message;
    }
    
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
}
