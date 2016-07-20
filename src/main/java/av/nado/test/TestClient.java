package av.nado.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import av.nado.remote.NadoRemote;
import av.nado.util.CompareKey;
import av.nado.util.CompareType;
import av.nado.util.JsonUtil;

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
    
    public static void main(String[] args) throws Exception
    {
        TestServer.main1(args);
        
        NadoRemote.instance().loadConfig("conf/nado.xml");
        
        test();
        
        m_schedule = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        
        m_schedule.scheduleWithFixedDelay(new Runnable()
        {
            
            public void run()
            {
                try
                {
                    test();
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                }
            }
        }, 100, 5000, TimeUnit.MILLISECONDS);
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
            
            // test map
            Map<CompareKey, Object> mapCondition = new HashMap<CompareKey, Object>();
            mapCondition.put(new CompareKey("id", CompareType.CT_BIGGER), 200);
            ret = NadoRemote.instance().invoke("av.nado.test.TestOutput", "testMap", mapCondition, 30L, 20);
            
            // test list
            List<String> lst = new ArrayList<String>();
            lst.add("1");
            ret = NadoRemote.instance().invoke("av.nado.test.TestOutput", "testSimpleList", lst);
            System.out.println(JsonUtil.toJson(ret));
            
            List<TestRemote> lst2 = new ArrayList<TestRemote>();
            lst2.add(testRemote);
            ret = NadoRemote.instance().invoke("av.nado.test.TestOutput", "testList", lst2);
            System.out.println(JsonUtil.toJson(ret));
            
            // test object
            ret = NadoRemote.instance().invoke("av.nado.test.TestOutput", "testObject", testRemote);
            System.out.println(JsonUtil.toJson(ret));
            

            ret = NadoRemote.instance().invoke("av.nado.test.TestOutput", "testNull");
            
            try
            {
                // test throw
                ret = NadoRemote.instance().invoke("av.nado.test.TestOutput", "testThrow", true);
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
