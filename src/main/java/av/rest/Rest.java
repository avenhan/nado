package av.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//在运行时执行
@Retention(RetentionPolicy.RUNTIME)
// 注解适用地方(字段和方法)
// @Target({ ElementType.FIELD, ElementType.TYPE, ElementType.METHOD })
@Target({ ElementType.METHOD })
public @interface Rest
{
    public String uri();
    
    /***
     * get; post; delete; put; head
     * 
     * can be one more, such as: get,head
     * 
     * @return
     */
    public String method() default "get";
}