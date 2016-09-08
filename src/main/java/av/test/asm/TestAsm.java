package av.test.asm;

import java.io.File;
import java.io.FileOutputStream;

import org.springframework.asm.ClassAdapter;

public class TestAsm
{
    public static void main(String[] arg) throws Exception
    {
        // 使用全限定名，创建一个ClassReader对象
        ClassReader classReader = new ClassReader("com.study.asm.Person");
        
        // 构建一个ClassWriter对象，并设置让系统自动计算栈和本地变量大小
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        
        ClassAdapter classAdapter = new GeneralClassAdapter(classWriter);
        
        classReader.accept(classAdapter, ClassReader.SKIP_DEBUG);
        
        byte[] classFile = classWriter.toByteArray();
        
        File file = new File("D://Person.class");
        
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(classFile);
        fos.close();
    }
}
