package av.sequence;

import java.util.concurrent.atomic.AtomicLong;

public class Sequence
{
    public static final long  KEY_START_SEQ = 10;
    private static AtomicLong m_seq         = new AtomicLong(KEY_START_SEQ);
    private static int        KEY_THRESHOLD = 10000;
    
    public static long getSequence()
    {
        long ret = m_seq.incrementAndGet();
        long hit = ret + KEY_THRESHOLD;
        if (hit < 0)
        {
            m_seq.set(KEY_START_SEQ);
        }
        
        return ret;
    }
}
