package av.nado.test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class TestWait
{
    static long printTime = 0;
    public static void main(String[] arg) throws Exception
    {
        Map<Long, Boolean> map = new ConcurrentHashMap<Long, Boolean>();
        
        map.put(30L, true);
        map.put(32L, true);
        map.put(20L, true);
        map.put(19L, true);
        map.put(18L, true);
        map.put(17L, true);
        map.put(16L, true);
        map.put(10L, true);
        map.put(6L, true);
        map.put(3L, true);
        
        for (Map.Entry<Long, Boolean> entry : map.entrySet())
        {
            System.out.println("key: " + entry.getKey() + " value: " + entry.getValue());
        }
        
        final Object object = new Object();
        
        final Semaphore semp = new Semaphore(5);


        Runnable runA = new Runnable() {
            @Override
            public void run() {
                while (true)
                {
                    try
                    {
                        synchronized (object)
                        {
                            object.wait(5000);
                        }
                    }
                    catch (InterruptedException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                    System.out.println("thread A print...." + (System.currentTimeMillis() - printTime) + "ms");
                    printTime = System.currentTimeMillis();
                }
            }
         };

         Runnable runB = new Runnable() {
            @Override
            public void run() {

                while (true)
                {
                    synchronized (object)
                    {
                        object.notifyAll();
                    }
                    
                    System.out.println("thread B notify...");
                    try
                    {
                        Thread.sleep(300);
                    }
                    catch (InterruptedException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        
        Thread threadA = new Thread(runA, "A");
        threadA.start();
        
        Thread.sleep(100);
        
        Thread threadB = new Thread(runB, "B");
        threadB.start();
    }
}
