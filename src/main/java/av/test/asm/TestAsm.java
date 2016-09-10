package av.test.asm;

import java.io.FileOutputStream;
import java.lang.reflect.Method;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class TestAsm
{
    public static void main(String[] arg) throws Exception
    {
        ClassReader cr = new ClassReader(Person.class.getName());
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new MethodChangeClassAdapter(cw);
        cr.accept(cv, Opcodes.ASM4);
        
        cr.accept(cv, ClassReader.SKIP_DEBUG); 
        // gets the bytecode of the Example class, and loads it dynamically
        byte[] code = cw.toByteArray();
        
        AsmLoadClass loader = new AsmLoadClass();
        Class<?> exampleClass = loader.defineClassFromBytes(Person.class.getName(), code);
        
        for (Method method : exampleClass.getMethods())
        {
            System.out.println(method);
        }
        
        exampleClass.getMethods()[0].invoke(null, null); // 調用execute，修改方法內容
        
        // gets the bytecode of the Example class, and loads it dynamically
        
        FileOutputStream fos = new FileOutputStream("output.class");
        fos.write(code);
        fos.close();
    }
}
