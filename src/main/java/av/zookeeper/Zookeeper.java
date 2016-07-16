package av.zookeeper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import av.nado.util.Check;
import av.nado.util.JsonUtil;
import av.util.exception.AException;
import av.util.trace.Trace;

public class Zookeeper implements Watcher
{
    private static Zookeeper               m_pThis        = null;
    private org.apache.zookeeper.ZooKeeper m_zk;
    private ZookeeperSetting               m_setting;
    
    private CountDownLatch                 countDownLatch = new CountDownLatch(1);
    
    public static Zookeeper instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new Zookeeper();
        }
        
        return m_pThis;
    }
    
    public void loadConfig(ZookeeperSetting setting) throws AException
    {
        m_setting = setting;
    }
    
    public void process(WatchedEvent event)
    {
        Trace.print("zk watch : {}", event.getType().toString());
        if (event.getState() == KeeperState.SyncConnected)
        {
            countDownLatch.countDown();
        }
    }
    
    public byte[] toByte(String path) throws AException
    {
        if (Check.IfOneEmpty(path))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        ZooKeeper zk = getZK();
        try
        {
            if (zk.exists(path, true) == null)
            {
                return null;
            }
            
            return zk.getData(path, true, null);
        }
        catch (KeeperException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
        catch (InterruptedException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
    }
    
    public byte[] toByte(String path, String digest) throws AException
    {
        if (Check.IfOneEmpty(path, digest))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        ZooKeeper zk = getZK();
        try
        {
            zk.addAuthInfo("digest", digest.getBytes());
            if (zk.exists(path, true) == null)
            {
                return null;
            }
            
            return zk.getData(path, true, null);
        }
        catch (KeeperException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
        catch (InterruptedException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
    }
    
    public String toString(String path) throws AException
    {
        byte[] ret = toByte(path);
        if (ret == null)
        {
            return "";
        }
        
        try
        {
            return new String(ret, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AException(AException.ERR_SERVER, "string encode error", e);
        }
    }
    
    public String toString(String path, String digest) throws AException
    {
        byte[] ret = toByte(path, digest);
        if (ret == null)
        {
            return "";
        }
        
        try
        {
            return new String(ret, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AException(AException.ERR_SERVER, "string encode error", e);
        }
    }
    
    public <T> T toObject(Class<T> type, String path) throws AException
    {
        if (Check.IfOneEmpty(type, path))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        String ret = toString(path);
        if (Check.IfOneEmpty(ret))
        {
            return null;
        }
        
        return JsonUtil.toObject(type, ret);
    }
    
    public <T> T toObject(Class<T> type, String path, String digest) throws AException
    {
        if (Check.IfOneEmpty(type, path))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        String ret = toString(path, digest);
        if (Check.IfOneEmpty(ret))
        {
            return null;
        }
        
        return JsonUtil.toObject(type, ret);
    }
    
    public void set(String path, Object obj) throws AException
    {
        if (Check.IfOneEmpty(path, obj))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        setString(path, JsonUtil.toJson(obj), null);
    }
    
    public void set(String path, Object obj, String digest) throws AException
    {
        if (Check.IfOneEmpty(path, obj, digest))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        setString(path, JsonUtil.toJson(obj), digest);
    }
    
    public void delete(String path) throws AException
    {
        if (Check.IfOneEmpty(path))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        ZooKeeper zKeeper = getZK();
        try
        {
            if (zKeeper.exists(path, true) == null)
            {
                return;
            }
            
            deleteDeepPath(zKeeper, path);
        }
        catch (InterruptedException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
        catch (KeeperException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
    }
    
    public void delete(String path, String digest) throws AException
    {
        if (Check.IfOneEmpty(path, digest))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        ZooKeeper zKeeper = getZK();
        try
        {
            zKeeper.addAuthInfo("digest", digest.getBytes());
            if (zKeeper.exists(path, true) == null)
            {
                return;
            }
            
            deleteDeepPath(zKeeper, path);
        }
        catch (InterruptedException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
        catch (KeeperException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
    }
    
    public List<String> children(String path) throws AException
    {
        if (Check.IfOneEmpty(path))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        ZooKeeper zKeeper = getZK();
        try
        {
            if (zKeeper.exists(path, true) == null)
            {
                return new ArrayList<String>();
            }
            
            return zKeeper.getChildren(path, true);
        }
        catch (InterruptedException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
        catch (KeeperException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
    }
    
    public List<String> children(String path, String digest) throws AException
    {
        if (Check.IfOneEmpty(path, digest))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        ZooKeeper zKeeper = getZK();
        try
        {
            zKeeper.addAuthInfo("digest", digest.getBytes());
            
            if (zKeeper.exists(path, true) == null)
            {
                return new ArrayList<String>();
            }
            
            return zKeeper.getChildren(path, true);
        }
        catch (InterruptedException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
        catch (KeeperException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
    }
    
    /**
     * 
     * create("/root", "hello", "admin:123", ZooDefs.Perms.ALL,
     * ZooDefs.Perms.READ, "guest:456"); create("/root", "hello", "admin:123",
     * ZooDefs.Perms.ALL, "guest:456", ZooDefs.Perms.READ);
     * 
     * 
     * @param path
     * @param value
     * @param objs
     * @throws AException
     */
    public void create(String path, String value, CreateMode mode, Object... objs) throws AException
    {
        if (Check.IfOneEmpty(path) || (objs.length > 0 && (objs.length % 2 != 0 || objs.length < 1)))
        {
            throw new AException(AException.ERR_SERVER, "invalid parameter");
        }
        
        List<ACL> lstACL = new ArrayList<ACL>();
        
        for (int i = 0; i < objs.length; i += 2)
        {
            if (objs[i] instanceof String && objs[i + 1] instanceof Integer)
            {
                String digest = (String) objs[i];
                int perm = (Integer) objs[i + 1];
                
                try
                {
                    Id id = new Id("digest", DigestAuthenticationProvider.generateDigest(digest));
                    ACL acl = new ACL(perm, id);
                    lstACL.add(acl);
                }
                catch (NoSuchAlgorithmException e)
                {
                    throw new AException(AException.ERR_SERVER, e);
                }
            }
            else if (objs[i] instanceof Integer && objs[i + 1] instanceof String)
            {
                String digest = (String) objs[i + 1];
                int perm = (Integer) objs[i];
                
                try
                {
                    Id id = new Id("digest", DigestAuthenticationProvider.generateDigest(digest));
                    ACL acl = new ACL(perm, id);
                    lstACL.add(acl);
                }
                catch (NoSuchAlgorithmException e)
                {
                    throw new AException(AException.ERR_SERVER, e);
                }
            }
            else
            {
                throw new AException(AException.ERR_SERVER, "invalid parameter");
            }
        }
        
        if (lstACL.isEmpty())
        {
            lstACL = Ids.OPEN_ACL_UNSAFE;
        }
        
        ZooKeeper zKeeper = getZK();
        try
        {
            if (zKeeper.exists(path, true) == null)
            {
                zKeeper.create(path, value.getBytes(), lstACL, mode);
            }
        }
        catch (InterruptedException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
        catch (KeeperException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
    }
    
    public void create(String path, String value, Object... objs) throws AException
    {
        create(path, value, CreateMode.PERSISTENT, objs);
    }
    
    public ACL makeACL(String digest, int perm) throws AException
    {
        try
        {
            Id id = new Id("digest", DigestAuthenticationProvider.generateDigest(digest));
            ACL acl = new ACL(perm, id);
            return acl;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new AException(AException.ERR_SERVER, e);
        }
    }
    
    private void deleteDeepPath(ZooKeeper zKeeper, String path) throws AException
    {
        try
        {
            List<String> lstChildren = zKeeper.getChildren(path, true);
            if (Check.IfOneEmpty(lstChildren))
            {
                zKeeper.delete(path, -1);
                return;
            }
            
            for (String childPath : lstChildren)
            {
                StringBuilder b = new StringBuilder(path).append("/").append(childPath);
                deleteDeepPath(zKeeper, b.toString());
            }
            
            zKeeper.delete(path, -1);
        }
        catch (InterruptedException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
        catch (KeeperException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
        
    }
    
    private void setString(String path, String value, String digest) throws AException
    {
        ZooKeeper zKeeper = getZK();
        try
        {
            if (!Check.IfOneEmpty(digest))
            {
                zKeeper.addAuthInfo("digest", digest.getBytes());
            }
            
            if (zKeeper.exists(path, true) == null)
            {
                zKeeper.create(path, value.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            else
            {
                zKeeper.setData(path, value.getBytes(), -1);
            }
        }
        catch (InterruptedException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
        catch (KeeperException e)
        {
            throw new AException(AException.ERR_SERVER, "invalid zk opt", e);
        }
    }
    
    private synchronized ZooKeeper getZK() throws AException
    {
        if (m_zk != null && m_zk.getState().isConnected() && m_zk.getState().isAlive())
        {
            return m_zk;
        }
        
        try
        {
            m_zk = new org.apache.zookeeper.ZooKeeper(m_setting.getIpAddr(), m_setting.getTimeOut(), this);
            try
            {
                countDownLatch.await();
                Trace.print("zk is connected...");
            }
            catch (InterruptedException e1)
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            if (m_zk.getState().isConnected())
            {
                return m_zk;
            }
            
            while (true)
            {
                if (m_zk.getState().isConnected())
                {
                    break;
                }
                
                try
                {
                    Thread.sleep(5);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            throw new AException(AException.ERR_SERVER, "zk initialized failed", e);
        }
        
        return m_zk;
    }
}
