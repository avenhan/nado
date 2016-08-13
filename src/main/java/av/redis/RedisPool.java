package av.redis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.nado.remote.RemoteIp;
import av.nado.util.Check;
import av.util.exception.AException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisPool
{
    public static final long  KEY_TIME_LOCK_TIME_OUT = 15000;
    private static Logger     logger                 = LogManager.getLogger(Redis.class);
    
    private RemoteIp          ip;
    private JedisPool         pool;
    private Map<String, Long> m_mapLockKeys          = new HashMap<String, Long>();
    private AtomicBoolean     m_connected            = new AtomicBoolean(true);
    
    public RemoteIp getIp()
    {
        return ip;
    }
    
    public void setIp(RemoteIp ip)
    {
        this.ip = ip;
    }
    
    public JedisPool getPool()
    {
        return pool;
    }
    
    public void setPool(JedisPool pool)
    {
        this.pool = pool;
    }
    
    public void checkConnect()
    {
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            m_connected.set(true);
        }
        finally
        {
            close(pool, jedis);
        }
    }
    
    public boolean isConnected()
    {
        return m_connected.get();
    }
    
    public Set<String> keys(String type, String pattern) throws AException, JedisConnectionException
    {
        if (Check.IfOneEmpty(pool, pattern))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameters");
        }
        
        String jedisPattern = key(type, pattern);
        Set<String> result = Collections.emptySet();
        
        Jedis jedis = null;
        try
        {
            jedis = pool.getResource();
            result = jedis.keys(jedisPattern);
            return result;
        }
        catch (JedisConnectionException e)
        {
            m_connected.set(false);
            throw e;
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e, "type:{}, pattern: {}", type, pattern);
        }
        finally
        {
            close(pool, jedis);
        }
    }
    
    public boolean exists(String type, String key) throws AException, JedisConnectionException
    {
        Jedis jedis = null;
        String jedisKey = key(type, key);
        boolean result = false;
        try
        {
            jedis = pool.getResource();
            
            result = jedis.exists(jedisKey);
            return result;
        }
        catch (JedisConnectionException e)
        {
            m_connected.set(false);
            throw e;
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e, "type:{}, key: {}", type, key);
        }
        finally
        {
            close(pool, jedis);
        }
    }
    
    public void delete(String type, String key) throws AException, JedisConnectionException
    {
        Jedis jedis = null;
        String jedisKey = key(type, key);
        try
        {
            jedis = pool.getResource();
            
            jedis.del(jedisKey);
        }
        catch (JedisConnectionException e)
        {
            m_connected.set(false);
            throw e;
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e, "type:{}, key: {}", type, key);
        }
        finally
        {
            close(pool, jedis);
        }
    }
    
    public String get(String type, String key) throws AException, JedisConnectionException
    {
        Jedis jedis = null;
        String jedisKey = key(type, key);
        
        try
        {
            jedis = pool.getResource();
            
            return jedis.get(jedisKey);
        }
        catch (JedisConnectionException e)
        {
            m_connected.set(false);
            throw e;
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e, "type:{}, key: {}", type, key);
        }
        finally
        {
            close(pool, jedis);
        }
    }
    
    public void set(String type, String key, String value, long expireTime) throws AException, JedisConnectionException
    {
        String valueTemp = value;
        if (Check.IfOneEmpty(value))
        {
            valueTemp = "";
        }
        
        Jedis jedis = null;
        String jedisKey = key(type, key);
        
        try
        {
            jedis = pool.getResource();
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
        catch (JedisConnectionException e)
        {
            m_connected.set(false);
            throw e;
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e, "type:{}, key: {}", type, key);
        }
        finally
        {
            close(pool, jedis);
        }
    }
    
    @SuppressWarnings("resource")
    public boolean lock(String type, String key) throws AException, JedisConnectionException
    {
        if (Check.IfOneEmpty(pool, type, key))
        {
            return false;
        }
        
        Jedis jedis = null;
        String jedisKey = key(type, key);
        
        try
        {
            jedis = pool.getResource();
            
            long currentTime = System.currentTimeMillis();
            while (true)
            {
                long ret = jedis.setnx(jedisKey, Long.toString(currentTime));
                if (ret == 1)
                {
                    m_mapLockKeys.put(jedisKey, currentTime);
                    return true;
                }
                
                String lastTimeValue = jedis.get(jedisKey);
                if (Check.IfOneEmpty(lastTimeValue))
                {
                    Thread.sleep(100);
                    continue;
                }
                
                long lastTime = Long.parseLong(lastTimeValue);
                currentTime = System.currentTimeMillis();
                if (lastTime + KEY_TIME_LOCK_TIME_OUT > currentTime)
                {
                    Thread.sleep(100);
                    continue;
                }
                
                String oldTimeValue = jedis.getSet(jedisKey, Long.toString(currentTime));
                if (Check.IfOneEmpty(oldTimeValue))
                {
                    throw new AException(AException.ERR_SERVER, "lock key: " + jedisKey + " is not existed....");
                }
                
                if (!oldTimeValue.equals(lastTimeValue))
                {
                    // someone has got the lock, wait and sleep
                    continue;
                }
                
                m_mapLockKeys.put(jedisKey, currentTime);
                jedis.persist(jedisKey.getBytes());
                
                return true;
            }
            
        }
        catch (JedisConnectionException e)
        {
            m_connected.set(false);
            throw e;
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            close(pool, jedis);
        }
    }
    
    public void unlock(String type, String key) throws AException, JedisConnectionException
    {
        if (Check.IfOneEmpty(type, key))
        {
            return;
        }
        
        Jedis jedis = null;
        String jedisKey = key(type, key);
        
        try
        {
            jedis = pool.getResource();
            String lastTimeValue = jedis.get(jedisKey);
            if (Check.IfOneEmpty(lastTimeValue))
            {
                throw new AException(AException.ERR_SERVER, "lock key: " + jedisKey + " is not existed....");
            }
            
            Long getLockTime = m_mapLockKeys.get(jedisKey);
            if (getLockTime == null)
            {
                // can not free other's lock
                return;
            }
            
            long lastTime = Long.parseLong(lastTimeValue);
            if (lastTime != getLockTime)
            {
                // some has got the lock
                m_mapLockKeys.remove(jedisKey);
                return;
            }
            
            long currentTime = System.currentTimeMillis();
            long diffTime = currentTime - lastTime;
            if (diffTime < 0 || diffTime > KEY_TIME_LOCK_TIME_OUT - 200)
            {
                m_mapLockKeys.remove(jedisKey);
                return;
            }
            
            jedis.set(jedisKey, "0");
            jedis.expire(jedisKey.getBytes(), (int) KEY_TIME_LOCK_TIME_OUT / 1000);
            m_mapLockKeys.remove(jedisKey);
        }
        catch (JedisConnectionException e)
        {
            m_connected.set(false);
            throw e;
        }
        catch (Exception e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
        finally
        {
            close(pool, jedis);
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
        try
        {
            pool.returnResource(jedis);
        }
        catch (Exception e)
        {
            logger.catching(e);
        }
    }
}
