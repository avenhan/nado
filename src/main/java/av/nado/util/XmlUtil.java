package av.nado.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import av.util.exception.AException;

public class XmlUtil
{
    /**
     * 
     * @param xmlFile
     * @return
     * @throws Exception
     * 
     */
    public static Map<String, Object> toMap(String xmlFile) throws Exception
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(new File(xmlFile));
        
        doc.getDocumentElement().normalize();
        
        NodeList lstNodes = doc.getChildNodes();
        Map<String, Object> map = getValues(lstNodes);
        if (map == null || map.isEmpty())
        {
            return null;
        }
        
        Object obj = null;
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            obj = entry.getValue();
            if (obj instanceof Map<?, ?>)
            {
                return (Map<String, Object>) obj;
            }
        }
        
        return map;
    }
    
    public static void toXml(String xmlFile, Map<String, ?> map) throws Exception
    {
        
    }
    
    private static Map<String, Object> getValues(NodeList lstNodes) throws Exception
    {
        if (lstNodes == null || lstNodes.getLength() < 1)
        {
            return null;
        }
        
        Map<String, Object> mapRet = new HashMap<String, Object>();
        for (int i = 0; i < lstNodes.getLength(); i++)
        {
            Node nNode = lstNodes.item(i);
            if (nNode.getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }
            
            Element elem = (Element) nNode;
            String name = elem.getTagName();
            
            if (Check.IfOneEmpty(name))
            {
                throw new AException(AException.ERR_FATAL, "const name or value can not be empty");
            }
            
            NodeList lstChilden = nNode.getChildNodes();
            if (isEndBreak(lstChilden))
            {
                String value = elem.getTextContent();
                if (value == null)
                {
                    value = "";
                }
                
                Object object = mapRet.get(name);
                if (object == null)
                {
                    mapRet.put(name, value);
                }
                else if (object instanceof String)
                {
                    List<String> lst = new ArrayList<String>();
                    mapRet.put(name, lst);
                    lst.add((String) object);
                    lst.add(value);
                }
                else if (object instanceof List<?>)
                {
                    List<String> lst = (List<String>) object;
                    lst.add(value);
                }
                else if (object instanceof Map<?, ?>)
                {
                    Map<String, String> map = (Map<String, String>) object;
                    map.put(name, value);
                }
                else
                {
                    throw new AException(AException.ERR_FATAL, "invalid type: {}", object.getClass().getName());
                }
                
                continue;
            }
            
            Map<String, Object> mapChild = getValues(lstChilden);
            
            Object object = mapRet.get(name);
            if (object == null)
            {
                mapRet.put(name, mapChild);
            }
            else if (object instanceof List<?>)
            {
                List<Map<String, Object>> lst = (List<Map<String, Object>>) object;
                lst.add(mapChild);
            }
            else if (object instanceof Map<?, ?>)
            {
                List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
                mapRet.put(name, lst);
                lst.add((Map<String, Object>) object);
                lst.add(mapChild);
            }
            else
            {
                throw new AException(AException.ERR_FATAL, "invalid type: {}", object.getClass().getName());
            }
        }
        
        return mapRet;
    }
    
    private static boolean isEndBreak(NodeList lstNodes) throws Exception
    {
        if (lstNodes == null || lstNodes.getLength() < 1)
        {
            return true;
        }
        
        boolean isEnd = true;
        for (int i = 0; i < lstNodes.getLength(); i++)
        {
            Node nNode = lstNodes.item(i);
            if (nNode.getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }
            
            isEnd = false;
            break;
        }
        
        return isEnd;
    }
}
