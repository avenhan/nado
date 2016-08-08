package av.test.nado;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import av.nado.proxy.ProxyManager;
import av.nado.remote.NadoRemote;
import av.nado.util.CompareKey;
import av.nado.util.CompareType;
import av.nado.util.JsonUtil;
import av.timer.QuartzManager;
import av.util.exception.AException;
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
    
    public static void main(String[] args) throws Exception
    {
        Trace.setLog(true);
        TestServer.startServer();
        
        NadoRemote.instance().loadConfig("conf/nado.xml");
        
        ProxyManager.instance().setTypes(TestRemoteInter.class, "av.nado.test.TestRemote", TestOutputInter.class, "av.nado.test.TestOutput");
        
        testTimer();
        
        m_schedule = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        m_schedule.scheduleWithFixedDelay(new Runnable()
        {
            
            public void run()
            {
                try
                {
                    if (count % 2 == 0)
                    {
                        // test();
                    }
                    else
                    {
                        // testInterface();
                    }
                    
                    count++;
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                }
            }
        }, 100, 5000, TimeUnit.MILLISECONDS);
        
        while (true)
        {
            Thread.sleep(5000);
        }
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
            Object ret = NadoRemote.instance().invoke("av.nado.test.TestRemote", "setName", "aven");
            
            if (ret != null)
            {
                System.out.println(ret.toString());
            }
            
            String outputClass = "av.nado.test.TestOutput";
            // test map
            Map<CompareKey, Object> mapCondition = new HashMap<CompareKey, Object>();
            mapCondition.put(new CompareKey("id", CompareType.CT_BIGGER), 200);
            ret = NadoRemote.instance().invoke(outputClass, "testMap", mapCondition, 30L, 20);
            
            // test list
            List<String> lst = new ArrayList<String>();
            lst.add("1");
            ret = NadoRemote.instance().invoke(outputClass, "testSimpleList", lst);
            System.out.println(JsonUtil.toJson(ret));
            
            List<TestRemote> lst2 = new ArrayList<TestRemote>();
            lst2.add(testRemote);
            ret = NadoRemote.instance().invoke(outputClass, "testList", lst2);
            System.out.println(JsonUtil.toJson(ret));
            
            // test object
            ret = NadoRemote.instance().invoke(outputClass, "testObject", testRemote);
            System.out.println(JsonUtil.toJson(ret));
            
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
            e.printStackTrace();
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
            System.out.println(JsonUtil.toJson(ret));
            
            List<TestRemote> lst2 = new ArrayList<TestRemote>();
            lst2.add(testRemote);
            ret = testOutput.testList(lst2);
            System.out.println(JsonUtil.toJson(ret));
            
            // test object
            ret = testOutput.testObject(testRemote);
            System.out.println(JsonUtil.toJson(ret));
            
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
            e.printStackTrace();
            // TODO: handle exception
        }
    }
}
