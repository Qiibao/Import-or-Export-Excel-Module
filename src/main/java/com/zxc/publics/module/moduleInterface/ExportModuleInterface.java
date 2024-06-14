package com.zxc.publics.module.moduleInterface;

import java.util.Collections;
import java.util.List;
import com.zxc.publics.entity.PageEntity;

public interface ExportModuleInterface<T> {

    /**
     * 核心方法：获取导出数据结果集
     * 注：此方法为循环调用，直到该方法查不到数据为止（请使用 pageDto 中的 indexStart 和 pageSize 进行分页）
     *
     * @return
     * @throws Exception
     */
    default List<T> getExportData(PageEntity pageEntity) throws Exception {
        return Collections.emptyList();
    }

    /**
     * 核心方法：获取导出数据结果之后，对数据进行后续处理
     * 注：如果不需要对数据进行后续处理，不需要重写此方法
     *
     * @return
     * @throws Exception
     */
    default T dataSubsequentProcessing(T t) throws Exception {
        return t;
    }

    /**
     * 核心方法：导出完成之后，自定义操作
     * 注：如果不需要后续操作，不需要重写此方法
     */
    default void afterExport() throws Exception {

    }

    /**
     * 核心方法：导出开始之前，自定义操作
     * 注：如果不需要后续操作，不需要重写此方法
     */
    default void beforeExport() throws Exception {

    }

}
