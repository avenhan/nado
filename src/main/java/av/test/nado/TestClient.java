package av.test.nado;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.nado.proxy.ProxyManager;
import av.nado.remote.NadoRemote;
import av.nado.util.CompareKey;
import av.nado.util.CompareType;
import av.nado.util.JsonUtil;
import av.test.dubbo.DubboConsumer;
import av.timer.QuartzManager;
import av.util.exception.AException;
import av.util.trace.FunctionTime;
import av.util.trace.Trace;

/**
 * 
 * @author aven han
 * 
 *         fjfhanjian@163.com
 *
 */
public class TestClient
{
    private static ScheduledExecutorService m_schedule = null;
    private static int                      count      = 0;
    
    private static Logger                   logger     = LogManager.getLogger(TestClient.class);
    
    public static void testss()
    {
        FunctionTime time = new FunctionTime();
        time.addCurrentTime("hello");
        try
        {
            Thread.sleep(200);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        time.print();
    }
    
    public static void main(String[] args) throws Exception
    {
        Trace.initialize(true);
        logger.debug("hello");
        Trace.print("from trace...");
        
        testss();
        
        String test = "nado";
        if (args.length > 0)
        {
            test = args[0];
        }
        
        if (test.equals("nado"))
        {
            testNado(args);
        }
        else
        {
            testDubbo(args);
        }
        
        while (true)
        {
            Thread.sleep(5000);
        }
    }
    
    private static void testDubbo(String[] args) throws Exception
    {
        DubboConsumer.testDubbo();
    }
    
    private static void testNado(String[] args) throws Exception
    {
        TestServer.startServer();
        
        NadoRemote.instance().loadConfig("conf/nado.xml");
        
        ProxyManager.instance().setTypes(TestRemoteInter.class, "av.test.nado.TestRemote", TestOutputInter.class, "av.test.nado.TestOutput");
        
        // testTimer();
        
        m_schedule = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        m_schedule.scheduleWithFixedDelay(new Runnable()
        {
            
            public void run()
            {
                try
                {
                    if (count % 2 == 0)
                    {
                        test();
                    }
                    else
                    {
                        testInterface();
                    }
                    
                    count++;
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                }
            }
        }, 100, 5000, TimeUnit.MILLISECONDS);
    }
    
    private static void testTimer()
    {
        try
        {
            QuartzManager.instance().addJob(2000L, 5, TestRemoteTimer.class.getName(), "testTimer", 1);
        }
        catch (AException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private static void test()
    {
        try
        {
            TestRemote testRemote = new TestRemote();
            testRemote.setName("aven");
            testRemote.setId(1002);
            BigDecimal decimal = new BigDecimal(120.123);
            decimal.setScale(3, BigDecimal.ROUND_HALF_UP);
            Object ret = NadoRemote.instance().invoke("av.test.nado.TestRemote", "setName", "aven");
            
            if (ret != null)
            {
                Trace.print(ret);
            }
            
            String outputClass = "av.test.nado.TestOutput";
            // test map
            Map<CompareKey, Object> mapCondition = new HashMap<CompareKey, Object>();
            mapCondition.put(new CompareKey("id", CompareType.CT_BIGGER), 200);
            ret = NadoRemote.instance().invoke(outputClass, "testMap", mapCondition, 30L, 20);
            
            // test list
            List<String> lst = new ArrayList<String>();
            lst.add("1");
            ret = NadoRemote.instance().invoke(outputClass, "testSimpleList", lst);
            Trace.print(JsonUtil.toJson(ret));
            
            List<TestRemote> lst2 = new ArrayList<TestRemote>();
            lst2.add(testRemote);
            ret = NadoRemote.instance().invoke(outputClass, "testList", lst2);
            Trace.print(JsonUtil.toJson(ret));
            
            // test object
            ret = NadoRemote.instance().invoke(outputClass, "testObject", testRemote);
            Trace.print(JsonUtil.toJson(ret));
            
            ret = NadoRemote.instance().invoke(outputClass, "testNull");
            
            try
            {
                // test throw
                ret = NadoRemote.instance().invoke(outputClass, "testThrow", true);
            }
            catch (Exception e)
            {
                // TODO: handle exception
            }
        }
        catch (Throwable e)
        {
            logger.catching(e);
            // TODO: handle exception
        }
    }
    
    public static void testInterface() throws AException
    {
        TestRemoteInter testRemoteInter = ProxyManager.instance().get(TestRemoteInter.class);
        TestOutputInter testOutput = ProxyManager.instance().get(TestOutputInter.class);
        
        try
        {
            TestRemote testRemote = new TestRemote();
            testRemote.setName("aven");
            testRemote.setId(1002);
            BigDecimal decimal = new BigDecimal(120.123);
            decimal.setScale(3, BigDecimal.ROUND_HALF_UP);
            
            testRemoteInter.setName("aven");
            Object ret = null;
            
            // test map
            Map<CompareKey, Object> mapCondition = new HashMap<CompareKey, Object>();
            mapCondition.put(new CompareKey("id", CompareType.CT_BIGGER), 200);
            ret = testOutput.testMap(mapCondition, 30, 20);
            
            // test list
            List<String> lst = new ArrayList<String>();
            lst.add("1");
            ret = testOutput.testSimpleList(lst);
            Trace.print(JsonUtil.toJson(ret));
            
            List<TestRemote> lst2 = new ArrayList<TestRemote>();
            lst2.add(testRemote);
            ret = testOutput.testList(lst2);
            Trace.print(JsonUtil.toJson(ret));
            
            // test object
            ret = testOutput.testObject(testRemote);
            Trace.print(JsonUtil.toJson(ret));
            
            ret = testOutput.testNull();
            
            try
            {
                // test throw
                ret = testOutput.testThrow(true);
            }
            catch (Exception e)
            {
                // TODO: handle exception
            }
        }
        catch (Throwable e)
        {
            logger.catching(e);
            // TODO: handle exception
        }
    }
}
