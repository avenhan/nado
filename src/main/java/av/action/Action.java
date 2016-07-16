package av.action;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.util.exception.AException;

public abstract class Action<T> implements Runnable
{
    private static Logger logger     = LogManager.getLogger(Action.class);
    private T             actionData = null;
    private AtomicBoolean finished   = new AtomicBoolean(true);
    
    public T getActionData()
    {
        return actionData;
    }
    
    public void setActionData(T actionData)
    {
        this.actionData = actionData;
    }
    
    protected abstract void onAction(T obj) throws AException;
    
    public boolean isFinished()
    {
        return this.finished.get();
    }
    
    public void setFinished(boolean isFinished)
    {
        this.finished.set(isFinished);
    }
    
    public void run()
    {
        try
        {
            finished.set(false);
            long startTime = System.currentTimeMillis();
            onAction(getActionData());
            long timeWaste = System.currentTimeMillis() - startTime;
            
            logger.debug("action: {} time wasted: {}ms, data: {}", this.getClass().getSimpleName(), timeWaste,
                    actionData == null ? "null" : actionData.toString());
        }
        catch (AException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            new AException(AException.ERR_FATAL, e);
        }
        finally
        {
            finished.set(true);
        }
    }
}
