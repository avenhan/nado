package av.timer;

import java.util.concurrent.atomic.AtomicInteger;

import com.esotericsoftware.reflectasm.MethodAccess;

public class QuartzInfo
{
    private String        groupName;
    private String        jobName;
    private String        cronTime;
    
    // local interface or class
    private MethodAccess  methodAccess;
    private Object        objInvoke;
    
    // remote type
    private String        remoteType;
    
    // method and parameters
    private String        methodName;
    private Object[]      arrParams;
    
    // -1 : for ever
    private int           once      = -1;
    private AtomicInteger runs      = new AtomicInteger(0);
    
    // time in ms
    private long          timeout   = -1;
    
    private boolean       exclusive = false;
    
    public String getGroupName()
    {
        return groupName;
    }
    
    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }
    
    public String getJobName()
    {
        return jobName;
    }
    
    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }
    
    public String getCronTime()
    {
        return cronTime;
    }
    
    public void setCronTime(String cronTime)
    {
        this.cronTime = cronTime;
    }
    
    public MethodAccess getMethodAccess()
    {
        return methodAccess;
    }
    
    public void setMethodAccess(MethodAccess methodAccess)
    {
        this.methodAccess = methodAccess;
    }
    
    public Object getObjInvoke()
    {
        return objInvoke;
    }
    
    public void setObjInvoke(Object objInvoke)
    {
        this.objInvoke = objInvoke;
    }
    
    public String getRemoteType()
    {
        return remoteType;
    }
    
    public void setRemoteType(String remoteType)
    {
        this.remoteType = remoteType;
    }
    
    public String getMethodName()
    {
        return methodName;
    }
    
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }
    
    public Object[] getArrParams()
    {
        return arrParams;
    }
    
    public void setArrParams(Object[] arrParams)
    {
        this.arrParams = arrParams;
    }
    
    public void setParams(Object... params)
    {
        this.arrParams = params;
    }
    
    public int getOnce()
    {
        return once;
    }
    
    public void setOnce(int once)
    {
        this.once = once;
    }
    
    public long getTimeout()
    {
        return timeout;
    }
    
    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }
    
    public AtomicInteger getRuns()
    {
        return runs;
    }
    
    public void setRuns(AtomicInteger runs)
    {
        this.runs = runs;
    }
    
    public boolean isExclusive()
    {
        return exclusive;
    }
    
    public void setExclusive(boolean exclusive)
    {
        this.exclusive = exclusive;
    }
    
    public boolean canRun()
    {
        if (this.runs.get() >= this.once)
        {
            return false;
        }
        
        this.runs.incrementAndGet();
        return true;
    }
}
