package av.redis;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.nado.remote.RemoteIp;
import av.nado.util.Check;
import av.util.exception.AException;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis
{
    public static final int                      KEY_MAX_TOTAL             = 20;
    public static final long                     KEY_MAX_WAIT_TIME         = 1000L;
    
    public static final String                   KEY_CLUSTER_TYPE_IMAGE    = "image";
    public static final String                   KEY_CLUSTER_TYPE_SHARDING = "sharding";
    
    private static Logger                        logger                    = LogManager.getLogger(Redis.class);
    
    private static Map<String, RedisClusterType> m_mapClusterType          = new HashMap<String, RedisClusterType>();
    private static Map<RemoteIp, RedisPool>      m_mapRedisPool            = new ConcurrentHashMap<RemoteIp, RedisPool>();
    private static Map<String, RedisClusterPool> m_mapClusterPool          = new HashMap<String, RedisClusterPool>();
    
    static
    {
        m_mapClusterType.put(KEY_CLUSTER_TYPE_IMAGE, RedisClusterType.CLUSTER_TYPE_IMAGE);
        m_mapClusterType.put(KEY_CLUSTER_TYPE_SHARDING, RedisClusterType.CLUSTER_TYPE_SHARDING);
    }
    
    public static void main(String[] args) throws Exception
    {
        initialize("test", KEY_CLUSTER_TYPE_SHARDING, RemoteIp.getRemoteIp("127.0.0.1:6379"));
        
    }
    
    public static void initialize(String name, String type, RemoteIp... ips) throws AException
    {
        Set<RemoteIp> setIps = new LinkedHashSet<RemoteIp>();
        for (RemoteIp remoteIp : ips)
        {
            if (remoteIp == null)
            {
                continue;
            }
            
            setIps.add(remoteIp);
        }
        
        initialize(name, type, setIps);
    }
    
    public static void initialize(String name, String type, Collection<RemoteIp> lstIps) throws AException
    {
        if (Check.IfOneEmpty(lstIps))
        {
            return;
        }
        
        RedisClusterType clusterType = m_mapClusterType.get(type);
        if (clusterType == null)
        {
            throw new AException(AException.ERR_SERVER, "invalid cluster name:{} type: {}", name, type);
        }
        
        Map<RemoteIp, RedisPool> mapRedisPool = new ConcurrentHashMap<RemoteIp, RedisPool>();
        for (RemoteIp remoteIp : lstIps)
        {
            if (Check.IfOneEmpty(remoteIp))
            {
                continue;
            }
            
            RedisPool redisPool = m_mapRedisPool.get(remoteIp);
            if (redisPool != null)
            {
                mapRedisPool.put(remoteIp, redisPool);
                continue;
            }
            
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(KEY_MAX_TOTAL);
            config.setMaxWaitMillis(KEY_MAX_WAIT_TIME);
            
            JedisPool pool = new JedisPool(config, remoteIp.getIp(), remoteIp.getPort());
            redisPool = new RedisPool();
            redisPool.setIp(remoteIp);
            redisPool.setPool(pool);
            
            mapRedisPool.put(remoteIp, redisPool);
        }
        
        if (Check.IfOneEmpty(mapRedisPool))
        {
            return;
        }
        
        RedisClusterPool clusterPool = new RedisClusterPool();
        clusterPool.setName(name);
        clusterPool.setType(clusterType);
        clusterPool.setMapPool(mapRedisPool);
        m_mapClusterPool.put(name, clusterPool);
    }
    
    public static Set<String> keys(String type, String pattern) throws AException
    {
        RedisClusterPool clusterPool = m_mapClusterPool.get(type);
        if (clusterPool == null)
        {
            return new HashSet<String>();
        }
        
        return clusterPool.keys(type, pattern);
    }
    
    public static boolean exists(String type, String key) throws AException
    {
        RedisClusterPool clusterPool = m_mapClusterPool.get(type);
        if (clusterPool == null)
        {
            return false;
        }
        
        return clusterPool.exists(type, key);
    }
    
    public static void delete(String type, String key) throws AException
    {
        RedisClusterPool clusterPool = m_mapClusterPool.get(type);
        if (clusterPool == null)
        {
            return;
        }
        
        clusterPool.delete(type, key);
    }
    
    public static String get(String type, String key) throws AException
    {
        RedisClusterPool clusterPool = m_mapClusterPool.get(type);
        if (clusterPool == null)
        {
            return null;
        }
        
        return clusterPool.get(type, key);
    }
    
    public static void set(String type, String key, String value) throws AException
    {
        set(type, key, value, 0);
    }
    
    public static void set(String type, String key, String value, long expireTime) throws AException
    {
        RedisClusterPool clusterPool = m_mapClusterPool.get(type);
        if (clusterPool == null)
        {
            return;
        }
        
        clusterPool.set(type, key, value, expireTime);
    }
    
    public static boolean lock(String type, String key) throws AException
    {
        RedisClusterPool clusterPool = m_mapClusterPool.get(type);
        if (clusterPool == null)
        {
            throw new AException(AException.ERR_SERVER, "not existed type: {}", type);
        }
        
        return clusterPool.lock(type, key);
    }
    
    public static void unlock(String type, String key) throws AException
    {
        RedisClusterPool clusterPool = m_mapClusterPool.get(type);
        if (clusterPool == null)
        {
            throw new AException(AException.ERR_SERVER, "not existed type: {}", type);
        }
        
        clusterPool.unlock(type, key);
    }
}
