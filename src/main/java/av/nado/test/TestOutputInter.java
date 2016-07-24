package av.nado.test;

import java.util.List;
import java.util.Map;

import av.nado.util.CompareKey;
import av.util.exception.AException;

public interface TestOutputInter
{
    public List<Integer> testNull();
    
    public List<Integer> testMap(Map<CompareKey, Object> mapCondition, long pos, int limit);
    
    public List<String> testSimpleList(List<String> lst);
    
    public List<TestRemote> testList(List<TestRemote> lst);
    
    public TestRemote testObject(TestRemote remote);
    
    public int testThrow(boolean isThrow) throws AException;
}
