package com.zxc.publics.annotation;

import java.lang.annotation.*;

/**
 * @Description: 用于标注excel文件导出字段的注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExportExcelField {

    // 实体类中字段在文件中的顺序（例如：0,1,2...）
    int order();

    // 实体类中字段在文件中的名称
    String name();

    // 需要导出的字段但在实体类中没有值时，可设置此默认值
    String defaultValue() default "";

}
