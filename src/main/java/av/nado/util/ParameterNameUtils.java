package av.nado.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ParameterNameUtils
{
    /**
     * 获取指定类指定方法的参数名
     * 
     * @param clazz
     *            要获取参数名的方法所属的类
     * @param method
     *            要获取参数名的方法
     * @return 按参数顺序排列的参数名列表，如果没有参数，则返回null
     */
    public static List<String> getMethodParameterNamesByAsm4(Class<?> clazz, final Method method)
    {
        final List<String> lstParamName = new ArrayList<String>();
        
        final Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes == null || parameterTypes.length == 0)
        {
            return lstParamName;
        }
        
        final Type[] types = new Type[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
        {
            types[i] = Type.getType(parameterTypes[i]);
        }
        
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(".");
        className = className.substring(lastDotIndex + 1) + ".class";
        InputStream is = clazz.getResourceAsStream(className);
        try
        {
            ClassReader classReader = new ClassReader(is);
            classReader.accept(new ClassVisitor(Opcodes.ASM4)
            {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions)
                {
                    // 只处理指定的方法
                    Type[] argumentTypes = Type.getArgumentTypes(desc);
                    if (!method.getName().equals(name) || !Arrays.equals(argumentTypes, types))
                    {
                        return null;
                    }
                    
                    return new MethodVisitor(Opcodes.ASM4)
                    {
                        @Override
                        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index)
                        {
                            lstParamName.add(name);
                            
                            // System.out.println("method: " + method.getName()
                            // + " index: " + index + " name: " + name);
                            // 静态方法第一个参数就是方法的参数，如果是实例方法，第一个参数是this
                        }
                    };
                    
                }
            }, 0);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        List<String> lstNames = new ArrayList<String>();
        
        if (Modifier.isStatic(method.getModifiers()))
        {
            for (int i = 0; i < types.length; i++)
            {
                lstNames.add(lstParamName.get(i));
            }
        }
        else
        {
            boolean canAdd = false;
            for (int i = 0; i < lstParamName.size(); i++)
            {
                if (canAdd)
                {
                    lstNames.add(lstParamName.get(i));
                    if (lstNames.size() >= types.length)
                    {
                        break;
                    }
                    
                    continue;
                }
                
                if (lstParamName.get(i).equals("this"))
                {
                    canAdd = true;
                    continue;
                }
            }
        }
        
        return lstNames;
    }
}