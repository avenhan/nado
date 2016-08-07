package av.nado.base;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import av.nado.annotation.Remote;
import av.nado.network.NetworkManager;
import av.nado.register.RegisterManager;
import av.nado.util.Check;
import av.nado.util.XmlUtil;
import av.util.exception.AException;
import av.util.trace.FunctionTime;
import av.util.trace.Trace;

public class NadoManager
{
    public static final String       KEY_FILE_NAME   = "nado.xml";
    
    private static Logger            logger          = LogManager.getLogger(NadoManager.class);
    private static NadoManager       m_pThis;
    private InetAddress              m_addr;
    private NadoSetting              m_setting;
    private Map<String, Integer>     m_mapServerInfo = new HashMap<String, Integer>();
    private Map<String, NadoInfo<?>> m_mapInfo       = new ConcurrentHashMap<String, NadoInfo<?>>();
    
    protected NadoManager()
    {
        // TODO Auto-generated constructor stub
    }
    
    public static NadoManager instance()
    {
        if (m_pThis == null)
        {
            m_pThis = new NadoManager();
        }
        
        return m_pThis;
    }
    
    public void loadConfig(String xmlFile) throws AException
    {
        try
        {
            Map<String, Object> map = XmlUtil.toMap(xmlFile);
            NadoSetting setting = NadoSetting.load(map);
            m_addr = InetAddress.getLocalHost();
            
            RegisterManager.instance().setType(setting.getRegister().get(NadoSetting.KEY_PROTOCOL));
            RegisterManager.instance().setAddress(setting.getRegister().get(NadoSetting.KEY_ADDRESS));
            
            m_setting = setting;
            Map<String, Integer> mapBoostrap = m_setting.getBoostrap();
            for (Map.Entry<String, Integer> entry : mapBoostrap.entrySet())
            {
                try
                {
                    NetworkManager.instance().setNetworkType(entry.getKey());
                    NetworkManager.instance().startServer(entry.getKey(), entry.getValue());
                    m_mapServerInfo.put(entry.getKey(), entry.getValue());
                }
                catch (Exception e)
                {
                    // TODO: handle exception
                }
            }
            
            if (Check.IfOneEmpty(m_mapServerInfo))
            {
                throw new AException(AException.ERR_FATAL, "not boostrap is usefull");
            }
            
            Map<String, NadoInfo<?>> mapInfo = new LinkedHashMap<String, NadoInfo<?>>();
            List<String> lstRemoteClass = setting.getRemote();
            for (String remote : lstRemoteClass)
            {
                Class<?> type = Class.forName(remote);
                NadoInfo<?> info = analysisClass(type);
                mapInfo.put(info.getName(), info);
            }
            
            addLoadedClasses("", mapInfo);
            RegisterManager.instance().setNotify(null);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new AException(AException.ERR_FATAL, e);
        }
    }
    
    public Object invoke(String type, String methodName, Object... params)
    {
        try
        {
            return invokeBase(type, methodName, params);
        }
        catch (Throwable e)
        {
            logger.catching(e);
            return e;
        }
    }
    
    private Object invokeBase(String type, String methodName, Object... params) throws Throwable
    {
        FunctionTime functionTime = new FunctionTime();
        try
        {
            NadoInfo<?> info = m_mapInfo.get(type);
            if (info == null)
            {
                throw new AException(AException.ERR_SERVER, "not found class {}", type);
            }
            
            List<NadoMethodInfo> lstMethods = info.getMapMethodExplains().get(methodName);
            if (lstMethods == null || lstMethods.isEmpty())
            {
                throw new AException(AException.ERR_SERVER, "class {} not found method: {}, params: {}", type, methodName, params.toString());
            }
            
            functionTime.addCurrentTime("{}.{}.{}", type, methodName, lstMethods.size());
            Object object = info.getObject();
            if (lstMethods.size() == 1)
            {
                return info.getMethodAccess().invoke(object, methodName, params);
            }
            
            Object ret = null;
            for (NadoMethodInfo method : lstMethods)
            {
                boolean isThrowed = false;
                try
                {
                    ret = method.getMethod().invoke(object, params);
                }
                catch (IllegalAccessException e)
                {
                    logger.debug("class {} not found method: {}, params: {}", type, methodName, params.toString());
                    logger.catching(e);
                    isThrowed = true;
                    ret = e;
                }
                catch (IllegalArgumentException e)
                {
                    logger.debug("class {} not found method: {}, params: {}", type, methodName, params.toString());
                    logger.catching(e);
                    isThrowed = true;
                    ret = e;
                }
                catch (InvocationTargetException e)
                {
                    logger.debug("class {} not found method: {}, params: {}", type, methodName, params.toString());
                    logger.catching(e);
                    isThrowed = true;
                    ret = e;
                }
                
                if (!isThrowed)
                {
                    break;
                }
            }
            
            if (ret instanceof Throwable)
            {
                Throwable eThrowable = (Throwable) ret;
                throw eThrowable;
            }
            
            return ret;
        }
        finally
        {
            functionTime.print();
        }
    }
    
    protected void addLoadedClasses(String packageName, Map<String, NadoInfo<?>> mapInfo)
    {
        for (Map.Entry<String, NadoInfo<?>> entry : mapInfo.entrySet())
        {
            String type = entry.getKey();
            NadoInfo<?> info = entry.getValue();
            Map<String, List<NadoMethodInfo>> map = info.getMapMethodExplains();
            for (String method : map.keySet())
            {
                Trace.debug("add remote class: {}, method: {}", type, method);
                try
                {
                    addMethodToRegister(type, method);
                }
                catch (AException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        m_mapInfo.putAll(mapInfo);
    }
    
    private void addMethodToRegister(String type, String method) throws AException
    {
        FunctionTime functionTime = new FunctionTime();
        try
        {
            String key = new StringBuilder(type).append(".").append(method).toString();
            
            for (Map.Entry<String, Integer> entry : m_mapServerInfo.entrySet())
            {
                StringBuilder bValue = new StringBuilder(m_addr.getHostAddress()).append(":").append(entry.getValue());
                RegisterManager.instance().registerProxy(key, bValue.toString(), entry.getKey());
                functionTime.addCurrentTime("key: {} register ip: {} type: {}", key, bValue.toString(), entry.getKey());
            }
        }
        finally
        {
            functionTime.print();
        }
    }
    
    protected Set<Class<?>> loadClasses(String pack, NadoFilter filter)
    {
        // 第一个class类的集合
        Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        // 获取包的名字 并进行替换
        String packageName = pack;
        String packageDirName = packageName.replace('.', '/');
        // 定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try
        {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            Trace.debug("search dir: {}", dirs);
            // 循环迭代下去
            while (dirs.hasMoreElements())
            {
                // 获取下一个元素
                URL url = dirs.nextElement();
                // 得到协议的名称
                String protocol = url.getProtocol();
                // 如果是以文件的形式保存在服务器上
                if ("file".equals(protocol))
                {
                    Trace.debug("packe: {} file类型的扫描", pack);
                    // 获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    // 以文件的方式扫描整个包下的文件 并添加到集合中
                    findClassFromFile(packageName, filePath, classes, filter);
                }
                else if ("jar".equals(protocol))
                {
                    // 如果是jar包文件
                    // 定义一个JarFile
                    Trace.debug("packe: {} jar类型的扫描", pack);
                    try
                    {
                        findClassesFromJar(url, packageName, packageDirName, classes, filter);
                    }
                    catch (Exception e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        return classes;
    }
    
    private void findClassesFromJar(URL url, String pack, String packageDirName, Set<Class<?>> classes, NadoFilter filter) throws Exception
    {
        String packageName = pack;
        JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        
        Trace.debug("pack: {}, pack dir: {}, url: {}", pack, packageDirName, url.toString());
        
        // 从此jar包 得到一个枚举类
        Enumeration<JarEntry> entries = jar.entries();
        // 同样的进行循环迭代
        while (entries.hasMoreElements())
        {
            // 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            // 如果是以/开头的
            if (name.charAt(0) == '/')
            {
                // 获取后面的字符串
                name = name.substring(1);
            }
            // 如果前半部分和定义的包名相同
            if (name.startsWith(packageDirName))
            {
                int idx = name.lastIndexOf('/');
                // 如果以"/"结尾 是一个包
                if (idx != -1)
                {
                    // 获取包名 把"/"替换成"."
                    packageName = name.substring(0, idx).replace('/', '.');
                }
                // 如果可以迭代下去 并且是一个包
                if ((idx != -1))
                {
                    // 如果是一个.class文件 而且不是目录
                    if (name.endsWith(".class") && !entry.isDirectory())
                    {
                        // 去掉后面的".class" 获取真正的类名
                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                        try
                        {
                            Trace.debug("jar: " + packageName + '.' + className);
                            Class<?> type = Class.forName(packageName + '.' + className);
                            if (filter == null || (filter != null && filter.accept(type)))
                            {
                                classes.add(type);
                            }
                        }
                        catch (Exception e)
                        {
                            // log
                            // .error("添加用户自定义视图类错误
                            // 找不到此类的.class文件");
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
    
    private void findClassFromFile(String packageName, String packagePath, Set<Class<?>> classes, NadoFilter filter)
    {
        // 获取此包的目录 建立一个File
        File dir = new File(packagePath);
        // 如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory())
        {
            // log.warn("用户定义包名 " + packageName + " 下没有任何文件");
            return;
        }
        // 如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter()
        {
            // 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file)
            {
                return file.isDirectory() || (file.getName().endsWith(".class"));
            }
        });
        // 循环所有文件
        for (File file : dirfiles)
        {
            // 如果是目录 则继续扫描
            if (file.isDirectory())
            {
                String nextPackageName = "";
                if (packageName.length() > 0)
                {
                    nextPackageName = packageName + "." + file.getName();
                }
                else
                {
                    nextPackageName = file.getName();
                }
                
                findClassFromFile(nextPackageName, file.getAbsolutePath(), classes, filter);
            }
            else
            {
                // 如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try
                {
                    // 添加到集合中去
                    // classes.add(Class.forName(packageName + '.' +
                    // className));
                    // 经过回复同学的提醒，这里用forName有一些不好，会触发static方法，没有使用classLoader的load干净
                    
                    // Trace.print(packageName + '.' + className);
                    Class<?> type = Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className);
                    if (filter == null || (filter != null && filter.accept(type)))
                    {
                        classes.add(type);
                    }
                }
                catch (ClassNotFoundException e)
                {
                    // log.error("添加用户自定义视图类错误 找不到此类的.class文件");
                    e.printStackTrace();
                }
            }
        }
    }
    
    protected <T> NadoInfo<T> analysisClass(Class<T> type) throws AException
    {
        NadoInfo<T> info = new NadoInfo<T>();
        info.setType(type);
        
        Method[] methods = type.getDeclaredMethods();
        
        for (Method method : methods)
        {
            if (method.isAnnotationPresent(Remote.class))
            {
                info.addMethod(method);
            }
        }
        
        return info;
    }
}
