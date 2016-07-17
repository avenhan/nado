package av.nado.remote;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.nado.base.NadoSetting;
import av.nado.network.NetworkManager;
import av.nado.network.NetworkStatus;
import av.nado.register.RegisterManager;
import av.nado.util.Aggregate;
import av.nado.util.Check;
import av.nado.util.XmlUtil;
import av.util.exception.AException;
import av.util.trace.FunctionTime;
import av.util.trace.Trace;

public class NadoRemote
{
    private static Logger          logger     = LogManager.getLogger(NadoRemote.class);
    private static NadoRemote      m_pThis;
    private static NetworkManager  m_network  = NetworkManager.instance();
    
    private Map<String, NadoProxy> m_mapProxy = new ConcurrentHashMap<String, NadoProxy>();
    
    private NadoRemote()
    {
    }
    
    public static NadoRemote instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new NadoRemote();
        }
        
        return m_pThis;
    }
    
    public void loadConfig(String xmlFile) throws AException
    {
        try
        {
            Map<String, Object> map = XmlUtil.toMap(xmlFile);
            NadoSetting setting = NadoSetting.load(map);
            
            RegisterManager.instance().setType(setting.getRegister().get(NadoSetting.KEY_PROTOCOL));
            RegisterManager.instance().setAddress(setting.getRegister().get(NadoSetting.KEY_ADDRESS));
            
            m_network.setNetworkType(setting.getBootstrap());
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_FATAL, e);
        }
    }
    
    public Object invoke(String type, String methodName, Object... params) throws Throwable
    {
        FunctionTime functionTime = new FunctionTime();
        try
        {
            if (Check.IfOneEmpty(type, methodName))
            {
                throw new AException(AException.ERR_SERVER, "invalid parameters ...");
            }
            
            StringBuilder b = new StringBuilder(type).append(".").append(methodName);
            NadoProxy proxy = findProxy(b.toString(), true);
            functionTime.addCurrentTime("proxy.{}", b.toString());
            if (proxy == null)
            {
                throw new AException(AException.ERR_SERVER, "not found remote class: {} method: {}", type, methodName);
            }
            
            return invoke(proxy, type, methodName, params);
        }
        finally
        {
            functionTime.print();
        }
    }
    
    private Object invoke(NadoProxy proxy, String type, String method, Object... params) throws Throwable
    {
        FunctionTime functionTime = new FunctionTime();
        try
        {
            functionTime.addCurrentTime("{}.{}.{}", type, method, params.length);
            RemoteIp ip = getUsefulRemoteIp(proxy);
            if (ip == null)
            {
                throw new AException(AException.ERR_SERVER, "no remote class: {} method: {} can be useful", type, method);
            }
            
            functionTime.addCurrentTime("useful proxy");
            
            NadoWrap wrap = new NadoWrap();
            wrap.setType(type);
            wrap.setMethod(method);
            
            for (Object param : params)
            {
                wrap.addParam(param);
            }
            
            Aggregate<NetworkStatus, NadoResponse> aggregate = m_network.send(NadoResponse.class, ip, wrap);
            if (aggregate.getFirst() != NetworkStatus.NETWORK_STATUS_SUCCESS)
            {
                throw new AException(AException.ERR_SERVER, "no remote class: {} method: {} can be useful", type, method);
            }
            
            String retExplain = aggregate.getSecond().getBody();
            functionTime.addCurrentTime("decode");
            Trace.debug("nado ret: {}", retExplain);
            
            Object objRet = NadoParam.fromExplain(retExplain);
            if (objRet instanceof Throwable)
            {
                Throwable throwed = (Throwable) objRet;
                throw throwed;
            }
            
            return objRet;
        }
        finally
        {
            functionTime.print();
        }
    }
    
    private RemoteIp getUsefulRemoteIp(NadoProxy proxy)
    {
        List<RemoteIp> lstIps = proxy.getLstRemoteIps();
        if (lstIps.isEmpty())
        {
            return null;
        }
        
        return lstIps.get(0);
    }
    
    private NadoProxy findProxy(String key, boolean openAlways) throws AException
    {
        NadoProxy proxy = m_mapProxy.get(key);
        if (proxy != null)
        {
            return proxy;
        }
        
        if (!openAlways)
        {
            return null;
        }
        
        proxy = RegisterManager.instance().findProxy(key);
        List<RemoteIp> lstClient = proxy.getLstRemoteIps();
        for (RemoteIp remoteIp : lstClient)
        {
            m_network.startClient(remoteIp);
            int count = 0;
            while (!m_network.isValidClient(remoteIp))
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                count++;
                if (count > 10)
                {
                    break;
                }
            }
        }
        
        return proxy;
    }
}
