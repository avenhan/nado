/*****************************************************************************************
 * 
 * Copyright (C) 2015, Yunio Inc. All rights reserved.
 * http://yunio.com
 * 
 *****************************************************************************************/
package av.rest.preprocessor;

import java.util.concurrent.Executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.restexpress.exception.ExceptionUtils;
import org.restexpress.pipeline.MessageContext;

import av.util.exception.AException;

public class LogExceptionHandler extends ExecutionHandler
{
    
    private static Logger logger = LogManager.getLogger(LogExceptionHandler.class);
    
    public LogExceptionHandler(Executor executor)
    {
        super(executor);
        // TODO Auto-generated constructor stub
    }
    
    public void handle(MessageContext context, Throwable cause)
    {
        logger.debug("GExceptionHandler catch exception: {}.", cause);
        logger.catching(cause);
        AException e = null;
        if (cause instanceof AException)
        {
            e = (AException) cause;
            logger.error("Exception info: {}.", e);
            echoAException(context, e);
            return;
        }
        
        if (cause.getCause() instanceof AException)
        {
            e = (AException) cause.getCause();
            logger.error("Exception info: {}.", e);
            echoAException(context, e);
            return;
        }
        
        Throwable rootCause = ExceptionUtils.findRootCause(cause);
        e = new AException(500, cause);
        
        if (rootCause != null)
        {
            logger.error("Exception root info: {}.", rootCause);
        }
        
        echoAException(context, e);
        
    }
    
    public void echoAException(MessageContext context, AException e)
    {
        if (context == null || e == null)
        {
            return;
        }
        
        logger.error("Exception info: {}.", e);
        if (e.getCode() == 304)
        {
            context.getResponse().setResponseCode(304);
            return;
        }
        
        Object error = NadoError.getError(e);
        context.getResponse().setBody(error);
    }
}
