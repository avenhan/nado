package av.timer;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class QuartzJobExclude extends NadoJob
{
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        try
        {
            jobWork(context);
        }
        catch (Throwable e)
        {
            getLogger().catching(e);
        }
    }
}
