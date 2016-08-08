package av.test.dubbo;

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import av.util.trace.Trace;

public class DubboServer
{
    private static AbstractXmlApplicationContext context;
    
    public static void main(String[] args) throws Exception
    {
        context = new FileSystemXmlApplicationContext("conf/spring/service-provider.xml");
        context.start();
        Trace.print("dubbo service is started....");
        context.registerShutdownHook();
        
        while (true)
        {
            Thread.sleep(5000);
        }
    }
}
