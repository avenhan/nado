package av.test.nado;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import av.nado.annotation.Remote;
import av.nado.util.CompareKey;
import av.util.exception.AException;

public class TestOutput implements TestOutputInter
{
    @Remote
    public List<Integer> testNull()
    {
        return null;
    }
    
    @Remote
    public List<Integer> testMap(Map<CompareKey, Object> mapCondition, long pos, int limit)
    {
        List<Integer> lstRet = new ArrayList<Integer>();
        lstRet.add(100);
        lstRet.add(200);
        lstRet.add(303);
        
        return lstRet;
    }
    
    @Remote
    public List<String> testSimpleList(List<String> lst)
    {
        if (lst == null)
        {
            return null;
        }
        
        Random random = new Random(System.currentTimeMillis());
        lst.add("added..." + random.nextInt() % 1000);
        
        return lst;
    }
    
    @Remote
    public List<TestRemote> testList(List<TestRemote> lst)
    {
        if (lst == null)
        {
            return null;
        }
        
        Random random = new Random(System.currentTimeMillis());
        TestRemote remote = new TestRemote();
        remote.setId(random.nextInt() % 1000);
        remote.setName("add : " + remote.getId());
        
        lst.add(remote);
        return lst;
    }
    
    @Remote
    public TestRemote testObject(TestRemote remote)
    {
        if (remote == null)
        {
            return null;
        }
        
        Random random = new Random(System.currentTimeMillis());
        
        remote.setName("name has changed..." + random.nextInt() % 1000);
        return remote;
    }
    
    @Remote
    public int testThrow(boolean isThrow) throws AException
    {
        if (isThrow)
        {
            throw new AException(AException.ERR_SERVER, "want to throw exception");
        }
        
        Random random = new Random(System.currentTimeMillis());
        return random.nextInt() % 10000;
    }
}
