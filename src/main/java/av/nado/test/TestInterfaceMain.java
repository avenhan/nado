package av.nado.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import av.nado.proxy.ProxyManager;
import av.util.trace.Trace;

public class TestInterfaceMain implements InvocationHandler // extends
                                                            // ClassLoader
{
    public void test()
    {
        System.out.println("hello....");
    }
    
    public static void main(final String args[]) throws Exception
    {
        ProxyManager.instance().setTypes(TestRemoteInter.class, TestRemote.class.getName(), TestOutputInter.class, TestOutput.class.getName());
        ProxyManager proxyManager = ProxyManager.instance();
        TestRemoteInter oiInterface = ProxyManager.instance().get(TestRemoteInter.class);
        TestOutputInter oInterface2 = ProxyManager.instance().get(TestOutputInter.class);
        
        int count = 100000;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++)
        {
            oiInterface.setName("avenhan");
            // proxyManager.call(TestInterface.class, "hello");
        }
        startTime = System.currentTimeMillis() - startTime;
        
        Trace.print("end .... time waste: {}", startTime);
    }
    
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        String methodName = method.getName();
        // System.out.println("current method: " + methodName);
        return null;
    }
}