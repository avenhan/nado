package av.timer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//在运行时执行
@Retention(RetentionPolicy.RUNTIME)
// 注解适用地方(字段和方法)
// @Target({ ElementType.FIELD, ElementType.TYPE, ElementType.METHOD })
@Target({ ElementType.METHOD })
public @interface Timer
{
    /***
     * cron time
     * 
     * @return
     */
    public String cron() default "";
    
    /***
     * cronTime
     * 
     * @return
     */
    public long time() default 0;
    
    /***
     * once
     * 
     * @return
     */
    public int count() default -1;
    
    /***
     * exclusive
     * 
     * @return
     */
    public boolean exclusive() default false;
}
