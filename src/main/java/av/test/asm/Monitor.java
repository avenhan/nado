package av.test.asm;

public class Monitor
{
    static long start = 0;
    
    public static void start()
    {
        start = System.currentTimeMillis();
    }
    
    public static void end()
    {
        long end = System.currentTimeMillis();
        System.out.println("execute method use time :" + (end - start));
    }
}
