package av.redis;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.nado.remote.RemoteIp;
import av.nado.util.Check;
import av.util.exception.AException;
import av.util.trace.FunctionTime;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class Redis
{
    public static final int                 KEY_MAX_TOTAL     = 20;
    public static final long                KEY_MAX_WAIT_TIME = 1000L;
    
    private static Logger                   logger            = LogManager.getLogger(Redis.class);
    
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
    
    public Set<String> keys(JedisPool pool, String type, String pattern) throws AException
    {
        if (Check.IfOneEmpty(pool, pattern))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameters");
        }
        
        FunctionTime functionTime = new FunctionTime();
        String jedisPattern = key(type, pattern);
        Set<String> result = Collections.emptySet();
        
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            functionTime.addCurrentTime("get jedis");
            result = jedis.keys(jedisPattern);
            return result;
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e, "type:{}, pattern: {}", type, pattern);
        }
        finally
        {
            close(pool, jedis);
            functionTime.print();
        }
    }
    
    public boolean exists(JedisPool pool, String type, String key) throws AException
    {
        FunctionTime functionTime = new FunctionTime();
        Jedis jedis = null;
        String jedisKey = key(type, key);
        boolean result = false;
        try
        {
            jedis = pool.getResource();
            functionTime.addCurrentTime("get jedis");
            
            result = jedis.exists(jedisKey);
            return result;
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e, "type:{}, key: {}", type, key);
        }
        finally
        {
            close(pool, jedis);
            functionTime.print();
        }
    }
    
    public void delete(JedisPool pool, String type, String key) throws AException
    {
        FunctionTime functionTime = new FunctionTime();
        Jedis jedis = null;
        String jedisKey = key(type, key);
        try
        {
            jedis = pool.getResource();
            functionTime.addCurrentTime("get jedis");
            
            jedis.del(jedisKey);
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e, "type:{}, key: {}", type, key);
        }
        finally
        {
            close(pool, jedis);
            functionTime.print();
        }
    }
    
    public String get(JedisPool pool, String type, String key) throws AException
    {
        FunctionTime functionTime = new FunctionTime();
        Jedis jedis = null;
        String jedisKey = key(type, key);
        
        try
        {
            jedis = pool.getResource();
            functionTime.addCurrentTime("get jedis");
            
            return jedis.get(jedisKey);
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e, "type:{}, key: {}", type, key);
        }
        finally
        {
            close(pool, jedis);
            functionTime.print();
        }
    }
    
    public void set(JedisPool pool, String type, String key, String value) throws AException
    {
        set(pool, type, key, value, 0);
    }
    
    public void set(JedisPool pool, String type, String key, String value, long expireTime) throws AException
    {
        String valueTemp = value;
        if (Check.IfOneEmpty(value))
        {
            valueTemp = "";
        }
        
        FunctionTime functionTime = new FunctionTime();
        Jedis jedis = null;
        String jedisKey = key(type, key);
        
        try
        {
            jedis = pool.getResource();
            functionTime.addCurrentTime("get jedis");
            
            if (expireTime <= 0)
            {
                value = jedis.set(jedisKey, value);
            }
            else
            {
                if (!jedis.exists(jedisKey))
                {
                    valueTemp = jedis.set(jedisKey, valueTemp, "NX", "PX", expireTime);
                }
                else
                {
                    valueTemp = jedis.set(jedisKey, valueTemp, "XX", "PX", expireTime);
                }
                
                if (Check.IfOneEmpty(valueTemp))
                {
                    throw new AException(AException.ERR_SERVER, "set redis failed");
                }
            }
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e, "type:{}, key: {}", type, key);
        }
        finally
        {
            close(pool, jedis);
            functionTime.print();
        }
    }
    
    private String key(String type, String key) throws AException
    {
        if (Check.IfOneEmpty(type))
        {
            throw new AException(AException.ERR_SERVER, "invalid redis type");
        }
        
        StringBuilder b = new StringBuilder(type);
        if (!Check.IfOneEmpty(key))
        {
            b.append(".").append(key);
        }
        
        return b.toString();
    }
    
    @SuppressWarnings("deprecation")
    private void close(JedisPool pool, Jedis jedis)
    {
        if (Check.IfOneEmpty(pool, jedis))
        {
            return;
        }
        pool.returnResource(jedis);
    }
}
