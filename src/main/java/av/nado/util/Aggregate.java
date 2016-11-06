package av.nado.util;

import java.io.Serializable;

public class Aggregate<A1, A2> implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private A1                first;
    private A2                second;
    
    public Aggregate()
    {
        
    }
    
    public Aggregate(A1 a1, A2 a2)
    {
        put(a1, a2);
    }
    
    public void put(A1 a1, A2 a2)
    {
        this.first = a1;
        this.second = a2;
    }
    
    public void putFirst(A1 a1)
    {
        this.first = a1;
    }
    
    public void putSecond(A2 a2)
    {
        this.second = a2;
    }
    
    public A1 getFirst()
    {
        return this.first;
    }
    
    public A2 getSecond()
    {
        return this.second;
    }
    
    @Override
    public String toString()
    {
        return new StringBuilder().append(first).append(":").append(second).toString();
    }
}
