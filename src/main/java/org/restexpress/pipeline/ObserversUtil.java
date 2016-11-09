package org.restexpress.pipeline;

import java.util.List;

import org.restexpress.Request;
import org.restexpress.Response;
import org.restexpress.RestExpress;

import av.nado.util.Check;

public class ObserversUtil
{
    public static void observers(RestExpress rest, Request request, Response response, boolean isRcv, boolean isSuccess, Exception e)
    {
        List<MessageObserver> lstMsgObservers = rest.getMessageObservers();
        if (Check.IfOneEmpty(lstMsgObservers))
        {
            return;
        }
        
        if (isRcv)
        {
            for (MessageObserver messageObserver : lstMsgObservers)
            {
                if (messageObserver == null)
                {
                    continue;
                }
                messageObserver.onReceived(request, response);
                
            }
            
            return;
        }
        
        if (isSuccess)
        {
            for (MessageObserver messageObserver : lstMsgObservers)
            {
                if (messageObserver == null)
                {
                    continue;
                }
                messageObserver.onComplete(request, response);
            }
            
            return;
        }
        
        if (e != null)
        {
            for (MessageObserver messageObserver : lstMsgObservers)
            {
                if (messageObserver == null)
                {
                    continue;
                }
                messageObserver.onException(e, request, response);
            }
            
            return;
        }
    }
}
