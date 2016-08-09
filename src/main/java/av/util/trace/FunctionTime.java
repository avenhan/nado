package av.util.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.nado.util.Check;
import av.nado.util.JsonUtil;
import av.util.exception.AException;

public class FunctionTime
{
    private static Logger                    logger             = LogManager.getLogger(FunctionTime.class);
    
    private static ThreadLocal<FunctionTime> threadFunctionTime = new ThreadLocal<FunctionTime>()
                                                                {
                                                                    private final AtomicInteger count = new AtomicInteger();
                                                                    
                                                                    @Override
                                                                    protected synchronized FunctionTime initialValue()
                                                                    {
                                                                        logger.debug("FunctionTime: thread={}, count={}.",
                                                                                Thread.currentThread().getName(), count.addAndGet(1));
                                                                        return new FunctionTime();
                                                                    }
                                                                };
    
    long                                     startTime          = 0;
    long                                     printTime          = 0;
    StackTraceElement                        trace              = null;
    private final List<String>               listOUt            = new ArrayList<String>();
    
    public static FunctionTime get()
    {
        FunctionTime ftime = threadFunctionTime.get();
        ftime.reset();
        return ftime;
    }
    
    public FunctionTime()
    {
        if (Trace.isGetTrace())
        {
            trace = ((new Exception()).getStackTrace())[1];
            startTime = System.currentTimeMillis();
        }
    }
    
    public void reset()
    {
        if (Trace.isGetTrace())
        {
            trace = ((new Exception()).getStackTrace())[2];
        }
        
        listOUt.clear();
        startTime = System.currentTimeMillis();
    }
    
    public void addCurrentTime(String key)
    {
        if (!Trace.isGetTrace() || Check.IfOneEmpty(key))
        {
            return;
        }
        
        long endTime = System.currentTimeMillis();
        long timeWaste = endTime - startTime;
        
        add(key, new StringBuffer("").append(timeWaste).append("ms").toString());
    }
    
    public void addCurrentTime(String format, Object... objs)
    {
        if (!Trace.isGetTrace() || Check.IfOneEmpty(format))
        {
            return;
        }
        
        long endTime = System.currentTimeMillis();
        long timeWaste = endTime - startTime;
        
        add(Trace.print(false, format, objs), new StringBuffer("").append(timeWaste).append("ms").toString());
    }
    
    public void add(String key, Object value)
    {
        if (!Trace.isGetTrace())
        {
            return;
        }
        
        if (Check.IfOneEmpty(key, value))
        {
            return;
        }
        
        try
        {
            listOUt.add(new StringBuilder(key).append(": ").append(JsonUtil.toJson(value)).toString());
        }
        catch (AException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public void print()
    {
        if (trace == null || !Trace.isGetTrace())
        {
            return;
        }
        
        long endTime = System.currentTimeMillis();
        long timeWaste = endTime - startTime;
        printTime = endTime;
        
        StringBuilder ret = new StringBuilder(trace.getClassName()).append(" ");
        ret.append(trace.getLineNumber()).append(" ");
        ret.append(trace.getMethodName()).append(" - ");
        ret.append("time waste: ").append(timeWaste).append("ms ");
        if (!listOUt.isEmpty())
        {
            ret.append(" {");
            boolean isFirst = true;
            for (String info : listOUt)
            {
                if (isFirst)
                {
                    ret.append(info);
                    isFirst = false;
                }
                else
                {
                    ret.append(", ").append(info);
                }
            }
            ret.append("}");
        }
        
        logger.debug(ret.toString());
    }
}
