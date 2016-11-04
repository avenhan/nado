package av.rest.preprocessor;

import org.restexpress.exception.ExceptionMapping;
import org.restexpress.exception.ServiceException;

import av.nado.util.JsonUtil;
import av.util.exception.AException;

public class NadoExceptionMapping implements ExceptionMapping
{

    public ServiceException getExceptionFor(Throwable arg0)
    {
        AException err = null;
        if (arg0 instanceof AException)
        {
            err = (AException) arg0;
        }
        else
        {
            err = new AException(AException.ERR_SERVER, arg0);
        }
        
        NadoError error = NadoError.getError(err);
        
        try
        {
            ServiceException exception = new ServiceException(JsonUtil.toJson(error));
            return exception;
        }
        catch (AException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    
    public <T extends Throwable, U extends ServiceException> void map(Class<T> arg0, Class<U> arg1)
    {
        // TODO Auto-generated method stub
        System.out.println("receiver map....");
    }
    
}
