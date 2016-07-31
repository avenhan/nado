/*****************************************************************************************
 * 
 * Copyright (C) 2015, Yunio Inc. All rights reserved. http://yunio.com
 * 
 *****************************************************************************************/
package av.rest.preprocessor;

import av.rest.preprocessor.LogMessageObserver.Timer;

public class MessageLogInfo
{
    private String id;
    private Timer  timer;
    private String url;
    private String method;
    private String token;
    
    public MessageLogInfo()
    {
        timer = new Timer();
    }
    
    public String getId()
    {
        return id;
    }
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public Timer getTimer()
    {
        return timer;
    }
    
    public void setTimer(Timer timer)
    {
        this.timer = timer;
    }
    
    public String getUrl()
    {
        return url;
    }
    
    public void setUrl(String url)
    {
        this.url = url;
    }
    
    public String getMethod()
    {
        return method;
    }
    
    public void setMethod(String method)
    {
        this.method = method;
    }
    
    public String getToken()
    {
        return token;
    }
    
    public void setToken(String token)
    {
        this.token = token;
    }
    
    public void stop()
    {
        if (timer != null)
        {
            timer.stop();
        }
    }
}
