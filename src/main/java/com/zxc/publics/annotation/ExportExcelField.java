package com.zxc.publics.annotation;

import com.zxc.publics.functionalInterface.ThrowingFunction;

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

    // 字段值转换器
    Class<? extends ThrowingFunction<?, ?, ?>> converter() default DefaultConverter.class;

    // 需要导出的字段但在实体类中没有值时，可设置此默认值
    String defaultValue() default "";

    class DefaultConverter implements ThrowingFunction<Object, Object, Exception> {
        @Override
        public Object apply(Object o) throws Exception {
            return o;
        }
    }

}
