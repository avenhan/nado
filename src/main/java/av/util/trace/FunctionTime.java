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
    private static double                    KEY_MILL           = 1000000.0;
    
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
    private final List<String>               listOut            = new ArrayList<String>();
    
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
            startTime = System.nanoTime();
        }
    }
    
    public void reset()
    {
        if (Trace.isGetTrace())
        {
            trace = ((new Exception()).getStackTrace())[2];
        }
        
        listOut.clear();
        startTime = System.nanoTime();
    }
    
    public void addCurrentTime(String key)
    {
        if (!Trace.isGetTrace() || Check.IfOneEmpty(key))
        {
            return;
        }
        
        long endTime = System.nanoTime();
        long timeWaste = endTime - startTime;
        
        add(key, new StringBuffer("").append(timeWaste / KEY_MILL).append("ms").toString());
    }
    
    public void addCurrentTime(String format, Object... objs)
    {
        if (!Trace.isGetTrace() || Check.IfOneEmpty(format))
        {
            return;
        }
        
        long endTime = System.nanoTime();
        long timeWaste = endTime - startTime;
        
        add(Trace.print(false, format, objs), new StringBuffer("").append(timeWaste / KEY_MILL).append("ms").toString());
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
        
        if (value instanceof String || value instanceof Double || value instanceof Integer || value instanceof Long || value instanceof Boolean)
        {
            listOut.add(new StringBuilder(key).append(": ").append(value).toString());
        }
        else
        {
            try
            {
                listOut.add(new StringBuilder(key).append(": ").append(JsonUtil.toJson(value)).toString());
            }
            catch (AException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    public void print()
    {
        if (trace == null || !Trace.isGetTrace())
        {
            return;
        }
        
        long endTime = System.nanoTime();
        long timeWaste = endTime - startTime;
        printTime = endTime;
        
        StringBuilder ret = new StringBuilder(trace.getClassName()).append(" ");
        ret.append(trace.getLineNumber()).append(" ");
        ret.append(trace.getMethodName()).append(" - ");
        ret.append("time waste: ").append(timeWaste / KEY_MILL).append("ms ");
        if (!listOut.isEmpty())
        {
            ret.append(" {");
            boolean isFirst = true;
            for (String info : listOut)
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
