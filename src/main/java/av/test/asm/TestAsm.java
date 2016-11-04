package av.test.asm;

import java.io.FileOutputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class TestAsm
{
    public static void main(String[] arg) throws Exception
    {
        Person pp = new Person();
        System.out.println(pp.getName());
        ClassReader cr = new ClassReader(Person.class.getName());
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new MethodChangeClassAdapter(cw);
        cr.accept(cv, Opcodes.ASM4);
        
        cr.accept(cv, ClassReader.SKIP_DEBUG);
        // gets the bytecode of the Example class, and loads it dynamically
        byte[] code = cw.toByteArray();
        
        FileOutputStream fos = new FileOutputStream("output.class");
        fos.write(code);
        fos.close();
        
        AsmLoadClass loader = new AsmLoadClass();
        Class<?> exampleClass = loader.getClassFromBytes(Person.class.getName(), code);
        
        Person person = (Person) exampleClass.newInstance();
        person.print();
    }
}
