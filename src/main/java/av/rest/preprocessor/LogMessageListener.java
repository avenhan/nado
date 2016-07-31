/*****************************************************************************************
 * 
 * Copyright (C) 2015, Yunio Inc. All rights reserved. http://yunio.com
 * 
 *****************************************************************************************/
package av.rest.preprocessor;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.nado.util.Check;
import av.util.exception.AException;

public class LogMessageListener extends Thread
{
    public static final int           KEY_SLEEP_TIME_OUT = 1000 * 30;
    public static final int           KEY_TIME_OUT       = 2000;
    
    private static Logger             logger             = LogManager.getLogger(LogMessageListener.class);
    private static LogMessageListener m_pThis;
    private LogMessageObserver        m_observer         = null;
    
    public static LogMessageListener instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new LogMessageListener();
        }
        
        return m_pThis;
    }
    
    public void initialize(LogMessageObserver observer)
    {
        if (isAlive())
        {
            return;
        }
        
        if (m_observer == null)
        {
            m_observer = observer;
        }
        
        start();
    }
    
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                doCheckLog();
                sleep(KEY_SLEEP_TIME_OUT);
            }
        }
        catch (Exception e)
        {
            new AException(AException.ERR_FATAL, e);
        }
    }
    
    private void doCheckLog()
    {
        if (m_observer == null || !isAlive())
        {
            return;
        }
        
        Map<String, MessageLogInfo> mapInfo = m_observer.getCopyInformation();
        if (Check.IfOneEmpty(mapInfo))
        {
            return;
        }
        
        for (String id : mapInfo.keySet())
        {
            MessageLogInfo info = mapInfo.get(id);
            if (info == null || info.getTimer() == null)
            {
                continue;
            }
            outputLog(info);
        }
    }
    
    private void outputLog(MessageLogInfo info)
    {
        long currentTime = System.currentTimeMillis();
        long timeWaste = currentTime - info.getTimer().getStartMillis();
        if (timeWaste < KEY_TIME_OUT)
        {
            return;
        }
        
        StringBuilder b = new StringBuilder();
        b.append("Request=").append(info.getId()).append(" use too much time\n");
        b.append("Url: ").append(info.getUrl()).append("\n");
        b.append("Method: ").append(info.getMethod()).append("\n");
        b.append("Token: ").append(info.getToken()).append("\n");
        b.append("Start at: ").append(Check.toUTCString(info.getTimer().getStartMillis())).append(", used_time=").append(timeWaste).append("ms\n");
        
        logger.error(b.toString());
    }
}
