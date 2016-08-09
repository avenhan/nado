package av.test.dubbo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import av.nado.util.CompareKey;
import av.nado.util.CompareType;
import av.nado.util.JsonUtil;
import av.test.nado.TestOutputInter;
import av.test.nado.TestRemote;
import av.test.nado.TestRemoteInter;
import av.util.trace.FunctionTime;
import av.util.trace.Trace;

public class DubboConsumer
{
    private static AbstractXmlApplicationContext context;
    private static ScheduledExecutorService      m_schedule = null;
    
    private static TestRemoteInter               testRemote = null;
    private static TestOutputInter               testOutput = null;
    private static int                           count      = 0;
    
    public static void main(String[] args) throws Exception
    {
        testDubbo();
        while (true)
        {
            Thread.sleep(5000);
        }
    }
    
    public static void testDubbo() throws Exception
    {
        DubboServer.startServer();
        
        context = new FileSystemXmlApplicationContext("conf/spring/service-consumer.xml");
        context.start();
        Trace.print("dubbo consumer is started....");
        
        testRemote = (TestRemoteInter) context.getBean("TestRemoteInter");
        testOutput = (TestOutputInter) context.getBean("TestOutputInter");
        
        testRemote.setName("hello");
        
        m_schedule = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        m_schedule.scheduleWithFixedDelay(new Runnable()
        {
            public void run()
            {
                try
                {
                    test();
                    count++;
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                }
            }
        }, 100, 5000, TimeUnit.MILLISECONDS);
    }
    
    static void testSetName() throws Exception
    {
        FunctionTime time = new FunctionTime();
        testRemote.setName("aven");
        time.print();
    }
    
    static void testMap() throws Exception
    {
        FunctionTime time = new FunctionTime();
        try
        {
            Map<CompareKey, Object> mapCondition = new HashMap<CompareKey, Object>();
            mapCondition.put(new CompareKey("id", CompareType.CT_BIGGER), 200);
            Object ret = testOutput.testMap(mapCondition, 30L, 20);
            System.out.println(JsonUtil.toJson(ret));
        }
        finally
        {
            time.print();
        }
    }
    
    static void testSimpleList() throws Exception
    {
        FunctionTime time = new FunctionTime();
        try
        {
            List<String> lst = new ArrayList<String>();
            lst.add("1");
            Object ret = testOutput.testSimpleList(lst);
            System.out.println(JsonUtil.toJson(ret));
        }
        finally
        {
            time.print();
        }
    }
    
    static void testList() throws Exception
    {
        TestRemote testRemote = new TestRemote();
        testRemote.setName("aven");
        testRemote.setId(1002);
        
        FunctionTime time = new FunctionTime();
        try
        {
            List<TestRemote> lst2 = new ArrayList<TestRemote>();
            lst2.add(testRemote);
            Object ret = testOutput.testList(lst2);
            System.out.println(JsonUtil.toJson(ret));
        }
        finally
        {
            time.print();
        }
    }
    
    static void testObject() throws Exception
    {
        TestRemote testRemote = new TestRemote();
        testRemote.setName("aven");
        testRemote.setId(1002);
        
        FunctionTime time = new FunctionTime();
        try
        {
            Object ret = testOutput.testObject(testRemote);
            System.out.println(JsonUtil.toJson(ret));
        }
        finally
        {
            time.print();
        }
    }
    
    static void testNull() throws Exception
    {
        TestRemote testRemote = new TestRemote();
        testRemote.setName("aven");
        testRemote.setId(1002);
        
        FunctionTime time = new FunctionTime();
        try
        {
            testOutput.testNull();
        }
        finally
        {
            time.print();
        }
    }
    
    static void testThrow() throws Exception
    {
        FunctionTime time = new FunctionTime();
        try
        {
            testOutput.testThrow(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            time.print();
        }
    }
    
    private static void test()
    {
        try
        {
            testSetName();
            testMap();
            testSimpleList();
            testList();
            testObject();
            testNull();
            testThrow();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            // TODO: handle exception
        }
    }
}
