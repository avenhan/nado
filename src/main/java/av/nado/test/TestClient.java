package av.nado.test;

import java.math.BigDecimal;

import av.nado.remote.NadoRemote;

/**
 * 
 * @author aven han
 * 
 *         fjfhanjian@163.com
 *
 */
public class TestClient
{
    public static void main(String[] args) throws Exception
    {
        TestServer.main1(args);
        
        try
        {
            NadoRemote.instance().loadConfig("conf/nado.xml");
            
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
            
            ret = NadoRemote.instance().invoke("av.nado.test.TestOutput", "testThrow", true);
            
            // test map
            // Map<CompareKey, Object> mapCondition = new HashMap<CompareKey,
            // Object>();
            // mapCondition.put(new CompareKey("id", CompareType.CT_BIGGER),
            // 200);
            // ret = NadoRemote.instance().invoke("av.nado.test.TestOutput",
            // "testMap", mapCondition, 30L, 20);
            //
            // // test list
            // List<String> lst = new ArrayList<String>();
            // lst.add("1");
            // ret = NadoRemote.instance().invoke("av.nado.test.TestOutput",
            // "testSimpleList", lst);
            // System.out.println(JsonUtil.toJson(ret));
            //
            // List<TestRemote> lst2 = new ArrayList<TestRemote>();
            // lst2.add(testRemote);
            // ret = NadoRemote.instance().invoke("av.nado.test.TestOutput",
            // "testList", lst2);
            // System.out.println(JsonUtil.toJson(ret));
            //
            // // test object
            // ret = NadoRemote.instance().invoke("av.nado.test.TestOutput",
            // "testObject", testRemote);
            // System.out.println(JsonUtil.toJson(ret));
            //
            // // test throw
            // ret = NadoRemote.instance().invoke("av.nado.test.TestOutput",
            // "testThrow", false);
            
            ret = NadoRemote.instance().invoke("av.nado.test.TestOutput", "testThrow", true);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            // TODO: handle exception
        }
        
    }
}
