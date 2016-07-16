package av.nado.util;

import java.io.Serializable;

public class Aggregate<A1, A2> implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private A1 a1;
    private A2 a2;
    
    public Aggregate()
    {
        
    }
    
    public Aggregate(A1 a1, A2 a2)
    {
        put(a1, a2);
    }
    
    public void put(A1 a1, A2 a2)
    {
        this.a1 = a1;
        this.a2 = a2;
    }
    
    public void putFirst(A1 a1)
    {
        this.a1 = a1;
    }
    
    public void putSecond(A2 a2)
    {
        this.a2 = a2;
    }
    
    public A1 getFirst()
    {
        return this.a1;
    }
    
    public A2 getSecond()
    {
        return this.a2;
    }
    
    @Override
    public String toString()
    {
        return new StringBuilder().append(a1).append(":").append(a2).toString();
    }
}
