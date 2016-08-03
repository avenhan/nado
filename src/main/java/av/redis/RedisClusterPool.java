package av.redis;

import java.util.HashMap;
import java.util.Map;

import av.nado.remote.RemoteIp;
import av.util.exception.AException;

public class RedisClusterPool
{
    private String                   name;
    private RedisClusterType         type;
    private Map<RemoteIp, RedisPool> mapPool = new HashMap<RemoteIp, RedisPool>();
    private RedisPool                imagePool = null;
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public RedisClusterType getType()
    {
        return type;
    }
    
    public void setType(RedisClusterType type)
    {
        this.type = type;
    }
    
    public Map<RemoteIp, RedisPool> getMapPool()
    {
        return mapPool;
    }
    
    public void setMapPool(Map<RemoteIp, RedisPool> mapPool)
    {
        this.mapPool = mapPool;
    }
    
    private RedisPool getOnePool() throws AException
    {
        if (imagePool != null)
        {
            return imagePool;
        }
        
        for (Map.Entry<RemoteIp, RedisPool> entry : mapPool.entrySet())
        {
            RedisPool pool = entry.getValue();
            if (pool == null)
            {
                continue;
            }
            
            imagePool = pool;
            break;
        }
        
        if (imagePool == null)
        {
            throw new AException(AException.ERR_SERVER, "invalid pool");
        }
        
        return imagePool;
    }
    
    /***
     * 
     * @param type
     * @param key
     * @return
     * @throws AException
     */
    public boolean exists(String type, String key) throws AException
    {
        if (this.type == RedisClusterType.CLUSTER_TYPE_IMAGE)
        {
            if (mapPool.isEmpty())
            {
                throw new AException(AException.ERR_SERVER, "empty redis pool");
            }
            
            RedisPool pool = getOnePool();
            return pool.exists(type, key);
        }
        
        return false;
    }
    
    public void delete(String type, String key) throws AException
    {
    }
    
    public String get(String type, String key) throws AException
    {
        
        return null;
    }
    
    public void set(String type, String key, String value, long expireTime) throws AException
    {
    }
    
    public boolean lock(String type, String key) throws AException
    {
        
        return false;
    }
    
    public void unlock(String type, String key) throws AException
    {
        
    }
}
