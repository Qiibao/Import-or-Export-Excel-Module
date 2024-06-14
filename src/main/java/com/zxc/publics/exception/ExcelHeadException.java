package com.zxc.publics.exception;

/**
 * @Description: excel字段名称行校验不合法异常（用于excel文件数据导入组件）
 */
public class ExcelHeadException extends Exception {

    public ExcelHeadException(String message) {
        super(message);
    }

}
