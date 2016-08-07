package av.timer;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

import com.esotericsoftware.reflectasm.MethodAccess;

import av.nado.util.Check;
import av.util.exception.AException;
import av.util.trace.FunctionTime;
import av.util.trace.Trace;

// http://www.cnblogs.com/drift-ice/p/3817269.html
public class QuartzManager
{
    private static Logger        logger         = LogManager.getLogger(QuartzManager.class);
    public static final String   KEY_GROUP_NAME = "nado";
    public static final String   KEY_METHOD     = "method#";
    public static final String   KEY_ID         = "quartz";
    
    private static QuartzManager m_pThis;
    private Scheduler            scheduler;
    private String               groupName      = KEY_GROUP_NAME;
    
    public static void main(String[] arg) throws Exception
    {
        QuartzManager.instance().addJob(QuartzManager.instance(), "onTimerTest", "aven");
        
        while (true)
        {
            Thread.sleep(5000);
        }
    }
    
    @Timer(cron = "*/2 * * * * ?", count = 10, exclusive = true)
    protected void onTimerTest(String name) throws AException
    {
        Trace.print("do timer name: {} time: {}", name, System.currentTimeMillis());
    }
    
    private QuartzManager() throws AException
    {
        try
        {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
        }
        catch (SchedulerException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    public static QuartzManager instance() throws AException
    {
        if (m_pThis == null)
        {
            m_pThis = new QuartzManager();
        }
        
        return m_pThis;
    }
    
    public void pause() throws AException
    {
        FunctionTime time = new FunctionTime();
        try
        {
            StackTraceElement trace = ((new Exception()).getStackTrace())[1];
            String method = trace.getMethodName();
            String type = trace.getClassName();
            time.addCurrentTime("get trace");
            
            String jobName = new StringBuilder(KEY_METHOD).append(type).append(".").append(method).toString();
            pause(jobName);
        }
        finally
        {
            time.print();
        }
    }
    
    public void pause(String jobName) throws AException
    {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        try
        {
            scheduler.pauseJob(jobKey);
        }
        catch (SchedulerException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    public void resume(String jobName) throws AException
    {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        try
        {
            scheduler.resumeJob(jobKey);
        }
        catch (SchedulerException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    public void delete() throws AException
    {
        FunctionTime time = new FunctionTime();
        try
        {
            StackTraceElement trace = ((new Exception()).getStackTrace())[1];
            String method = trace.getMethodName();
            String type = trace.getClassName();
            time.addCurrentTime("get trace");
            
            String jobName = new StringBuilder(KEY_METHOD).append(type).append(".").append(method).toString();
            delete(jobName);
        }
        finally
        {
            time.print();
        }
    }
    
    public void delete(String jobName) throws AException
    {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        try
        {
            scheduler.deleteJob(jobKey);
        }
        catch (SchedulerException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    public void run(String jobName) throws AException
    {
        JobKey jobKey = JobKey.jobKey(jobName, groupName);
        try
        {
            scheduler.triggerJob(jobKey);
        }
        catch (SchedulerException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    public void setTimeCron(String jobName, String cronTime) throws AException
    {
        try
        {
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, groupName);
            
            // 获取trigger，即在spring配置文件中定义的 bean id="myTrigger"
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            
            // 表达式调度构建器
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronTime);
            
            // 按新的cronExpression表达式重新构建trigger
            trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            
            // 按新的trigger重新设置job执行
            scheduler.rescheduleJob(triggerKey, trigger);
        }
        catch (SchedulerException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    public void addJob(Object obj) throws AException
    {
        if (Check.IfOneEmpty(obj))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        FunctionTime time = new FunctionTime();
        try
        {
            Method[] arrMethods = obj.getClass().getDeclaredMethods();
            for (Method method : arrMethods)
            {
                if (method.isAnnotationPresent(Timer.class))
                {
                    addJob(obj, method.getName());
                    time.addCurrentTime("method[{}]", method.getName());
                }
            }
        }
        finally
        {
            time.print();
        }
    }
    
    public void addJob(Object obj, String methodName, Object... params) throws AException
    {
        if (Check.IfOneEmpty(obj, methodName))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        FunctionTime time = new FunctionTime();
        try
        {
            Method[] arrMethods = obj.getClass().getDeclaredMethods();
            Timer timer = null;
            for (Method method : arrMethods)
            {
                if (!method.getName().equalsIgnoreCase(methodName))
                {
                    continue;
                }
                
                if (!method.isAnnotationPresent(Timer.class))
                {
                    continue;
                }
                
                if (method.getParameterCount() != params.length)
                {
                    continue;
                }
                
                method.setAccessible(true);
                timer = method.getAnnotation(Timer.class);
                break;
            }
            
            time.addCurrentTime("method");
            
            String jobName = new StringBuilder(KEY_METHOD).append(obj.getClass().getName()).append(".").append(methodName).toString();
            MethodAccess methodAccess = MethodAccess.get(obj.getClass());
            QuartzInfo info = new QuartzInfo();
            info.setObjInvoke(obj);
            info.setMethodAccess(methodAccess);
            info.setJobName(jobName);
            info.setCronTime(timer.cron());
            info.setGroupName(groupName);
            info.setOnce(timer.count());
            info.setTimeout(timer.time());
            info.setMethodName(methodName);
            info.setArrParams(params);
            info.setExclusive(timer.exclusive());
            
            time.addCurrentTime("info");
            addJob(info);
        }
        finally
        {
            time.print();
        }
    }
    
    public void addJob(QuartzInfo info) throws AException
    {
        if (Check.IfOneEmpty(info) || Check.IfOneEmpty(info.getJobName(), info.getMethodName()))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        String groupNameTemp = info.getGroupName();
        if (Check.IfOneEmpty(groupNameTemp))
        {
            info.setGroupName(groupName);
        }
        
        FunctionTime time = new FunctionTime();
        try
        {
            String jobName = info.getJobName();
            JobDetail detail = null;
            Trigger trigger = null;
            
            if (info.isExclusive())
            {
                detail = JobBuilder.newJob(QuartzJobExclude.class).withIdentity(jobName, info.getGroupName()).build();
            }
            else
            {
                detail = JobBuilder.newJob(QuartzJob.class).withIdentity(jobName, info.getGroupName()).build();
            }
            
            time.addCurrentTime("prepare detail");
            if (scheduler.checkExists(detail.getKey()))
            {
                time.add("job exist", info.getJobName());
                return;
            }
            
            time.add("job ok", info.getJobName());
            time.addCurrentTime("check key");
            if (!Check.IfOneEmpty(info.getCronTime()))
            {
                trigger = TriggerBuilder.newTrigger().withIdentity(jobName, info.getGroupName()).startNow()
                        .withSchedule(CronScheduleBuilder.cronSchedule(info.getCronTime())).build();
                
            }
            else
            {
                long elapseTime = info.getTimeout();
                if (elapseTime < 1)
                {
                    elapseTime = 1;
                }
                if (info.getOnce() > 0)
                {
                    trigger = TriggerBuilder.newTrigger().withIdentity(jobName, info.getGroupName()).startNow()
                            .withSchedule(
                                    SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(elapseTime).withRepeatCount(info.getOnce() - 1))
                            .build();
                }
                else
                {
                    trigger = TriggerBuilder.newTrigger().withIdentity(jobName, info.getGroupName()).startNow()
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(elapseTime).repeatForever()).build();
                }
            }
            
            time.addCurrentTime("prepare trigger");
            detail.getJobDataMap().put(KEY_ID, info);
            time.addCurrentTime("put id");
            
            scheduler.scheduleJob(detail, trigger);
            logger.debug("group: {} add job: {} run at: {}", info.getGroupName(), jobName, info.getCronTime());
        }
        catch (SchedulerException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            time.print();
        }
    }
}
