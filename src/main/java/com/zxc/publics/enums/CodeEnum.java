package com.zxc.publics.enums;

public enum CodeEnum {

    CODE_10000(10000, "返回成功"),

    CODE_10001(10001, "返回失败");

    final int code;

    final String message;

    CodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static CodeEnum getByCode(int code) {
        for (CodeEnum codeEnum : CodeEnum.values()) {
            if (codeEnum.getCode() == code) {
                return codeEnum;
            }
        }
        return null;
    }

    public static String getMessageByCode(int code) {
        CodeEnum codeEnum = getByCode(code);
        return codeEnum == null ? null : codeEnum.getMessage();
    }

}
