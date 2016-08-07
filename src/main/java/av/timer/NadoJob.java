package av.timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

import com.esotericsoftware.reflectasm.MethodAccess;

import av.nado.remote.NadoRemote;
import av.nado.util.Check;
import av.util.exception.AException;

public abstract class NadoJob implements Job
{
    private static Logger logger = LogManager.getLogger(QuartzJob.class);
    
    protected Logger getLogger()
    {
        return logger;
    }
    
    protected void jobWork(JobExecutionContext context) throws Throwable
    {
        JobDetail detail = context.getJobDetail();
        if (detail == null)
        {
            logger.debug("get detail failed ...");
            return;
        }
        
        JobKey key = detail.getKey();
        String name = key.getName();
        String groupName = key.getGroup();
        
        JobDataMap dataMap = detail.getJobDataMap();
        if (dataMap == null)
        {
            logger.debug("group: {} job: {} data map is null", groupName, name);
            return;
        }
        
        QuartzInfo info = (QuartzInfo) dataMap.get(QuartzManager.KEY_ID);
        if (info == null)
        {
            logger.debug("group: {} job: {} info is null", groupName, name);
            return;
        }
        
        if (info.getOnce() > 0 && !Check.IfOneEmpty(info.getCronTime()))
        {
            if (!info.canRun())
            {
                try
                {
                    QuartzManager.instance().delete(name);
                }
                catch (AException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                return;
            }
        }
        
        MethodAccess methodAccess = info.getMethodAccess();
        if (methodAccess != null)
        {
            methodAccess.invoke(info.getObjInvoke(), info.getMethodName(), info.getArrParams());
        }
        else
        {
            NadoRemote.instance().invoke(info.getRemoteType(), info.getMethodName(), info.getArrParams());
        }
        
        if (info.getOnce() > 0 && !Check.IfOneEmpty(info.getCronTime()) && info.getRuns().get() >= info.getOnce())
        {
            try
            {
                QuartzManager.instance().delete(name);
            }
            catch (AException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
