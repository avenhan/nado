package av.action;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import av.util.exception.AException;

public class ActionPool
{
    private ThreadPoolExecutor m_threadPool = null;
    private static ActionPool  m_pThis      = null;
    private ActionConfig       m_config     = new ActionConfig();
    
    public static synchronized ActionPool instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new ActionPool();
        }
        
        return m_pThis;
    }
    
    public synchronized void setConfig(ActionConfig config)
    {
        if (config == null)
        {
            return;
        }
        
        m_config = config;
    }
    
    public ActionConfig getConfig()
    {
        return m_config;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public synchronized void addAction(Action action, Object objActionData) throws AException
    {
        initialize();
        
        if (action == null || objActionData == null)
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        action.setActionData(objActionData);
        action.setFinished(false);
        m_threadPool.execute(action);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public synchronized void addAction(Action action) throws AException
    {
        initialize();
        
        if (action == null)
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        action.setActionData(null);
        action.setFinished(false);
        m_threadPool.execute(action);
    }
    
    // self functions
    private ActionPool()
    {
    }
    
    private synchronized void initialize()
    {
        if (m_threadPool != null)
        {
            return;
        }
        
        m_threadPool = new ThreadPoolExecutor(m_config.getMinThreadCount(), m_config.getMaxThreadCount(), m_config.getKeepAliveTimeMs(),
                TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(m_config.getCachedSize()), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
