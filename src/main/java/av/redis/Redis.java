package av.redis;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import av.nado.remote.RemoteIp;
import av.util.exception.AException;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis
{
    public static final int                 KEY_MAX_TOTAL     = 20;
    public static final long                KEY_MAX_WAIT_TIME = 1000L;
    
    private static Map<RemoteIp, JedisPool> m_mapRedisPool    = new ConcurrentHashMap<RemoteIp, JedisPool>();
    
    public static void initialize(Set<RemoteIp> lstIps) throws AException
    {
        for (RemoteIp remoteIp : lstIps)
        {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(KEY_MAX_TOTAL);
            config.setMaxWaitMillis(KEY_MAX_WAIT_TIME);
            
            JedisPool pool = new JedisPool(config, remoteIp.getIp(), remoteIp.getPort());
            
            m_mapRedisPool.put(remoteIp, pool);
        }
    }
    
}
