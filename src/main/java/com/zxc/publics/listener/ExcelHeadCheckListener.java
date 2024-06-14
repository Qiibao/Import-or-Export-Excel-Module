package com.zxc.publics.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.CellExtra;
import com.alibaba.excel.read.listener.ReadListener;
import com.zxc.publics.util.StringUtil;
import com.zxc.publics.annotation.ImportExcelField;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class ExcelHeadCheckListener implements ReadListener {

    // 实体类类型
    private Class<?> clazz;

    // 校验状态
    private boolean isSuccess = true;

    // 文件数据总条数
    @Getter
    private int fileSzie;

    public boolean isSuccess() {
        return isSuccess;
    }

    public ExcelHeadCheckListener(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void onException(Exception e, AnalysisContext analysisContext) throws Exception {

        log.error("ExcelHeadCheckListener onException", e);

    }

    @Override
    public void invoke(Object o, AnalysisContext analysisContext) {

        System.out.println("invoke>>> " + o);

    }

    @Override
    public void extra(CellExtra cellExtra, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    @Override
    public boolean hasNext(AnalysisContext analysisContext) {

        return false;

    }

    @Override
    public void invokeHead(Map map, AnalysisContext analysisContext) {

        List<String> allExcelFieldNames = getAllExcelFieldNames(clazz).stream().map(ImportExcelField::name).collect(Collectors.toList());
        for (int i = 0; i < allExcelFieldNames.size(); i++) {
            if (!StringUtil.valueToStr(map.get(i)).equals(allExcelFieldNames.get(i))) {
                isSuccess = false;
            }
        }
        fileSzie = analysisContext.getTotalCount() == null ? 0 : analysisContext.getTotalCount();

    }

    /**
     * 获得实体类中所有加了@Excelfield注解的字段名
     *
     * @param clazz
     * @return
     */
    private List<ImportExcelField> getAllExcelFieldNames(Class<?> clazz) {

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ImportExcelField.class))
                .map(field -> field.getAnnotation(ImportExcelField.class))
                .sorted(Comparator.comparingInt(ImportExcelField::order))
                .collect(Collectors.toList());

    }

}
