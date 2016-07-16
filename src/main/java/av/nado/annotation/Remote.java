package av.nado.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * 
 * @author av
 *
 *         当定义在在方法上时，类的方法可以被远端调用使用
 *
 */

// 在运行时执行
@Retention(RetentionPolicy.RUNTIME)
// 注解适用地方(字段和方法)
// @Target({ ElementType.FIELD, ElementType.TYPE, ElementType.METHOD })
@Target({ ElementType.METHOD })
public @interface Remote
{
    
}
