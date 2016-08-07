package av.timer;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzJob extends NadoJob
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
