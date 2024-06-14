package com.zxc.publics.annotation;

import lombok.Getter;

import java.lang.annotation.*;
import java.util.Objects;

import static com.zxc.publics.annotation.ImportExcelField.ColorConstant.GREEN;
import static com.zxc.publics.annotation.ImportExcelField.ColorConstant.RED;

/**
 * @Description: 用于标注excel文件导入字段的注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ImportExcelField {

    // 实体类中字段在文件中的顺序（例如：0,1,2...）
    int order();

    // 实体类中字段在文件中的名称
    String name();

    // 需要导入的字段但在文件中没有值时，可设置此默认值
    String defaultValue() default "";

    // 字段校验不通过时，导出的错误数据excel文件中该字段值的单元格背景色（须使用ColorConstant中的颜色常量）
    String errorColor() default "";

    // 该字段值是否需要保证导入后的唯一性（在文件 及 数据库中已存在时，后续相同的不导入）
    boolean unique() default false;

    // 颜色常量（@ImportExcelField可选的单元格背景色）
    class ColorConstant {

        // 红色
        public static final String RED = "red";

        // 绿色
        public static final String GREEN = "green";

    }

    @Getter
    enum ColorEnum {

        RED_ENUM(RED, "@|@"),
        GREEN_ENUM(GREEN, "@#|#@");

        private final String color;

        private final String suffix;

        ColorEnum(String color, String suffix) {
            this.color = color;
            this.suffix = suffix;
        }

        public static ColorEnum getEnum(String color) {
            for (ColorEnum colorEnum : ColorEnum.values()) {
                if (Objects.equals(colorEnum.getColor(), color)) return colorEnum;
            }
            return null;
        }

        /**
         * 通过颜色获取后缀
         */
        public static String getSuffixByColor(String color) {
            ColorEnum colorEnum = getEnum(color);
            if (colorEnum != null) return colorEnum.getSuffix();
            return "";
        }

    }

}
