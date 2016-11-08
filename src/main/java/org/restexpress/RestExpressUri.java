package org.restexpress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import av.nado.util.Check;

public class RestExpressUri
{
    private String                      key;
    private Object                      attach;
    private RestExpressUri              wildcard;
    private Map<String, RestExpressUri> mapUris = new HashMap<String, RestExpressUri>();
    
    private static String[] pathToArray(String path)
    {
        String[] arr = path.split("/");
        List<String> lst = new ArrayList<String>();
        
        for (String sub : arr)
        {
            if (Check.IfOneEmpty(sub))
            {
                continue;
            }
            
            lst.add(sub);
        }
        
        return lst.toArray(new String[lst.size()]);
    }
    
    private String[] subArray(String[] paths, int fromIndex)
    {
        return subArray(paths, fromIndex, paths.length - 1);
    }
    
    private String[] subArray(String[] paths, int fromIndex, int toIndex)
    {
        int toIndexTemp = Math.min(toIndex, paths.length - 1);
        int fromIndexTemp = Math.max(fromIndex, 0);
        if (toIndexTemp < fromIndexTemp)
        {
            return new String[0];
        }
        
        String[] arrRet = new String[toIndexTemp - fromIndexTemp + 1];
        for (int i = fromIndexTemp; i <= toIndexTemp; i++)
        {
            arrRet[i - fromIndex] = paths[i];
        }
        
        return arrRet;
    }
    
    public void setPaths(String path, Object attach)
    {
        String[] arr = pathToArray(path);
        setPaths(arr, attach);
    }
    
    public void setPaths(String[] paths, Object attach)
    {
        if (paths == null || paths.length < 1)
        {
            this.attach = attach;
            return;
        }
        
        if (paths[0].startsWith("{"))
        {
            if (wildcard == null)
            {
                wildcard = new RestExpressUri();
            }
            
            wildcard.key = paths[0].substring(1, paths[0].length() - 1);
            wildcard.setPaths(subArray(paths, 1), attach);
            return;
        }
        
        RestExpressUri existed = mapUris.get(paths[0]);
        if (existed == null)
        {
            existed = new RestExpressUri();
            mapUris.put(paths[0], existed);
        }
        
        existed.key = paths[0];
        existed.setPaths(subArray(paths, 1), attach);
    }
    
    public Object getPathAttach(String paths)
    {
        String[] arr = pathToArray(paths);
        return getPathAttach(arr, null);
    }
    
    public Object getPathAttach(String paths, Map<String, String> mapWildcard)
    {
        String[] arr = pathToArray(paths);
        return getPathAttach(arr, mapWildcard);
    }
    
    public Object getPathAttach(String[] paths, Map<String, String> mapWildcard)
    {
        if (paths == null || paths.length < 1)
        {
            return this.attach;
        }
        
        RestExpressUri existed = mapUris.get(paths[0]);
        if (existed != null)
        {
            return existed.getPathAttach(subArray(paths, 1), mapWildcard);
        }
        
        if (wildcard != null)
        {
            if (mapWildcard != null)
            {
                mapWildcard.put(wildcard.key, paths[0]);
            }
            return wildcard.getPathAttach(subArray(paths, 1), mapWildcard);
        }
        
        return null;
    }
}
