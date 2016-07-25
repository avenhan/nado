package av.nado.test;

import java.util.concurrent.Semaphore;

public class TestWait
{
    static long printTime = 0;
    public static void main(String[] arg) throws Exception
    {
        final Object object = new Object();
        
        final Semaphore semp = new Semaphore(5);


        Runnable runA = new Runnable() {
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
