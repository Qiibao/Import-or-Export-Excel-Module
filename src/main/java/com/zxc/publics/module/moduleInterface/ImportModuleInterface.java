package com.zxc.publics.module.moduleInterface;

import java.util.Objects;

@FunctionalInterface
public interface ImportModuleInterface<T> {

    /**
     * 核心业务方法：导入Excel数据及后续业务逻辑（在创建组件时，必须重写此方法）
     *
     * @param data
     * @throws Exception
     */
    void importExcelData(T data) throws Exception;

    /**
     * 核心校验方法：校验Excel数据是否合法
     * 注：如果不需要校验导入的数据，重写时直接返回true即可
     *
     * @param data
     * @return
     * @throws Exception
     */
    default Boolean checkExcelData(T data) throws Exception {
        return Objects.nonNull(data);
    }

    /**
     * 核心方法：导入完成之后，自定义操作
     * 注：如果不需要后续操作，不需要重写此方法
     */
    default void afterImport() throws Exception {

    }

    /**
     * 核心方法：导入开始之前，自定义操作
     * 注：如果不需要后续操作，不需要重写此方法
     */
    default void beforeImport() throws Exception {

    }

}
