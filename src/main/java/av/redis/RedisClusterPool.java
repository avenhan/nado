package av.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import av.nado.remote.RemoteIp;
import av.util.exception.AException;

public class RedisClusterPool
{
    private String                   name;
    private RedisClusterType         type;
    private Map<RemoteIp, RedisPool> mapPool    = new HashMap<RemoteIp, RedisPool>();
    private RedisPool                imagePool  = null;
    private RedisPool                masterPool = null;
    
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
    
    public RedisPool getMasterPool()
    {
        return masterPool;
    }
    
    public void setMasterPool(RedisPool masterPool)
    {
        this.masterPool = masterPool;
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
        if (imagePool != null && imagePool.isConnected())
        {
            return imagePool;
        }
        
        for (Map.Entry<RemoteIp, RedisPool> entry : mapPool.entrySet())
        {
            RedisPool pool = entry.getValue();
            if (pool == null || !pool.isConnected())
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
    
    public Set<String> keys(String type, String pattern) throws AException
    {
        if (this.type == RedisClusterType.CLUSTER_TYPE_IMAGE)
        {
            if (mapPool.isEmpty())
            {
                throw new AException(AException.ERR_SERVER, "empty redis pool");
            }
            
            RedisPool pool = getOnePool();
            return pool.keys(type, pattern);
        }
        
        return new HashSet<String>();
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
        if (this.type == RedisClusterType.CLUSTER_TYPE_IMAGE)
        {
            if (mapPool.isEmpty())
            {
                throw new AException(AException.ERR_SERVER, "empty redis pool");
            }
            
            for (Map.Entry<RemoteIp, RedisPool> entry : mapPool.entrySet())
            {
                RedisPool pool = entry.getValue();
                if (pool == null)
                {
                    continue;
                }
                
                pool.delete(type, key);
            }
            
            return;
        }
    }
    
    public String get(String type, String key) throws AException
    {
        if (this.type == RedisClusterType.CLUSTER_TYPE_IMAGE)
        {
            RedisPool pool = getOnePool();
            return pool.get(type, key);
        }
        
        return null;
    }
    
    public void set(String type, String key, String value, long expireTime) throws AException
    {
        if (this.type == RedisClusterType.CLUSTER_TYPE_IMAGE)
        {
            if (mapPool.isEmpty())
            {
                throw new AException(AException.ERR_SERVER, "empty redis pool");
            }
            
            for (Map.Entry<RemoteIp, RedisPool> entry : mapPool.entrySet())
            {
                RedisPool pool = entry.getValue();
                if (pool == null)
                {
                    continue;
                }
                
                pool.set(type, key, value, expireTime);
            }
            
            return;
        }
    }
    
    public boolean lock(String type, String key) throws AException
    {
        if (this.type == RedisClusterType.CLUSTER_TYPE_IMAGE)
        {
            RedisPool pool = getOnePool();
            return pool.lock(type, key);
        }
        
        return false;
    }
    
    public void unlock(String type, String key) throws AException
    {
        if (this.type == RedisClusterType.CLUSTER_TYPE_IMAGE)
        {
            RedisPool pool = getOnePool();
            pool.unlock(type, key);
            
            return;
        }
    }
}
