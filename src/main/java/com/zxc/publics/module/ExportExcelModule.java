package com.zxc.publics.module;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.zxc.publics.functionalInterface.ThrowingFunction;
import com.zxc.publics.handler.CustomCellWriteHandler;
import com.zxc.publics.handler.ExcelColumnMergeHandler;
import com.zxc.publics.entity.ExportExcelEntity;
import com.zxc.publics.entity.ExportResult;
import com.zxc.publics.entity.PageEntity;
import com.zxc.publics.entity.Result;
import com.zxc.publics.enums.CodeEnum;
import com.zxc.publics.module.moduleInterface.ExportModuleInterface;
import com.zxc.publics.util.EasyExcelUtil;
import com.zxc.publics.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.zxc.publics.annotation.ExportExcelField;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.zxc.publics.constant.ExcelExportConstant.*;

/**
 * @param: <T>
 * @Author: omnipotentQIQI
 * @Date: 2024/2/6 10:00
 * @Description: 简单Excel导出数据组件（1实体类 <--> 1excel文件 / 1zip文件（包含多个excel文件））
 * @Remarks: 注意：使用之前请在对应实体类中对需要导出的属性添加 @ExportExcelField 注解；如果使用构造器自定义 maxExportCount，everyQueryDataNum，需要保证 everyQueryDataNum <= maxExportCount && maxExportCount % everyQueryDataNum == 0
 * @Remarks: 支持：空字段赋予默认值、实时获取导出进度、获取导出结果、获取导出文件下载地址等等
 * @Version: 1.0
 */
@Data
@Slf4j
@AllArgsConstructor
public class ExportExcelModule<T> implements ExportModuleInterface<T> {

    // 一些初始化导出组件的必要参数
    private ExportExcelEntity exportExcelEntity;

    // 单个excel文件最大导出数据量
    private Integer maxExportCount;

    // 单次查询数据量
    private Integer everyQueryDataNum;

    // 导出的实体类类型
    private Class<?> clazz;

    // excel文件写入器
    private ExcelWriter excelWriter;

    // excel文件写入sheet
    private WriteSheet writeSheet;

    // 产生的excel文件数量
    private int fileCount;

    // 记录最后一个excel文件名
    private String lastFileName;

    // 导出开始时间
    private Long startTime;

    // 导出结束时间
    private Long endTime;

    // 导出完成标志
    private Boolean exportFinishFlag = false;

    // 当前导出数据总数
    private AtomicInteger exportCount = new AtomicInteger(0);

    // 当前查询数据总数
    private AtomicInteger queryCount = new AtomicInteger(0);

    // 最终导出文件是否为zip文件
    private Boolean zipFlag = false;

    // 最终导出文件是否为excel文件
    private Boolean excelFlag = false;

    // 时间格式化工具
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public ExportExcelModule(Class<?> clazz, ExportExcelEntity exportExcelEntity) {
        this.clazz = clazz;
        this.exportExcelEntity = checkFileName(exportExcelEntity);
        this.maxExportCount = EXCEL_MAX_EXPORT_COUNT;
        this.everyQueryDataNum = EXCEL_EVERY_QUERY_DATA_NUM;
    }

    public ExportExcelModule(Class<?> clazz, ExportExcelEntity exportExcelEntity, Integer maxExportCount, Integer everyQueryDataNum) {
        this.clazz = clazz;
        this.exportExcelEntity = checkFileName(exportExcelEntity);
        this.maxExportCount = maxExportCount;
        this.everyQueryDataNum = everyQueryDataNum;
    }

    /**
     * 获取导出耗时（在导入结束前调用返回null）
     */
    public Long getExportTime() {
        return isExportFinish() && startTime != null && endTime != null ? endTime - startTime : null;
    }

    /**
     * 获取导出进度（注意：导出进度100%不能作为导出完成的依据）
     */
    public String getExportProcess() {
        return isExportFinish() ? "100%" : (queryCount.get() > 0 ? String.format("%.0f", Math.ceil((exportCount.get() * 1.0 / queryCount.get()) * 100)) + "%" : "0%");
    }

    /**
     * 判断导出是否完成
     *
     * @return
     */
    public Boolean isExportFinish() {
        return exportFinishFlag;
    }

    /**
     * 获取导出文件的下载地址
     */
    public String getDownloadUrl() {
        return !excelFlag && !zipFlag ? null : (excelFlag ? exportExcelEntity.getExcelDownloadUrl() : exportExcelEntity.getZipDownloadUrl());
    }

    /**
     * 获取导出结果
     */
    public Result<ExportResult> getExportResult() {
        ExportResult exportResult = new ExportResult()
                .setTimeConsuming(getExportTime())
                .setProgress(getExportProcess())
                .setEndFlag(isExportFinish())
                .setDownloadUrl(getDownloadUrl())
                .setExportCount(exportCount.get())
                .setSourceCount(queryCount.get());
        return !isExportFinish() ? new Result<>(CodeEnum.CODE_10000.getCode(), "导出未完成", exportResult) : new Result<>(CodeEnum.CODE_10000.getCode(), "导出已完成", exportResult);
    }

    private List<T> getExportExcelData(PageEntity pageEntity) {

        try {
            return getExportData(pageEntity);
        } catch (Exception e) {
            log.error("getExportData error", e);
            return null;
        }

    }

    private T excelDataSubsequentProcessing(T t) {

        try {
            return dataSubsequentProcessing(t);
        } catch (Exception e) {
            log.error("dataSubsequentProcessing error", e);
            return null;
        }

    }

    private void afterExportExcel() {

        try {
            afterExport();
        } catch (Exception e) {
            log.error("afterExportExcel error", e);
        }

    }

    private void beforeExportExcel() {

        try {
            beforeExport();
        } catch (Exception e) {
            log.error("beforeExportExcel error", e);
        }

    }

    /**
     * 将生成的excel文件写入到响应流中（需等待导出完成）
     *
     * @param response
     */
    public void writeExcelToResponse(HttpServletResponse response) {

        try {
            String fileName = excelFlag ? exportExcelEntity.getExcelFileName() : exportExcelEntity.getZipFileName();
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + URLEncoder.encode(fileName, "UTF-8") + "\"");
            File file = new File(exportExcelEntity.getFilePath() + fileName);
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
            OutputStream os = response.getOutputStream();
            os.write(buffer);
            os.flush();
            os.close();
        } catch (Exception e) {
            log.error("writeExcelToResponse error", e);
        }

    }

    /**
     * 开始导出Excel数据（同步）
     *
     * @throws Exception
     */
    public void startExport() throws Exception {

        if (!checkNecessaryParams()) throw new Exception("everyQueryDataNum or maxExportCount error");
        beforeExportExcel();
        exportExcelData();
        afterExportExcel();

    }

    /**
     * 校验必要参数
     *
     * @return
     */
    private Boolean checkNecessaryParams() {

        return everyQueryDataNum > 0 && maxExportCount > 0 && everyQueryDataNum <= maxExportCount && maxExportCount % everyQueryDataNum == 0;

    }

    /**
     * 开始导出Excel数据（异步）
     */
    public void startExportAsync() {

        CompletableFuture.runAsync(() -> {
            try {
                startExport();
            } catch (Exception e) {
                log.error("startExportAsync error", e);
            }
        });

    }

    /**
     * 导出Excel数据
     *
     * @throws Exception
     */
    private void exportExcelData() throws Exception {

        // 导出开始时间
        startTime = System.currentTimeMillis();
        // 初始化数据量
        int dataCount = maxExportCount;
        // 计算单个文件的循环次数
        int loopCount = maxExportCount / everyQueryDataNum;
        // 初始化文件样式
        this.writeSheet = initWriteSheet();
        while (dataCount >= maxExportCount) {
            // 创建excel文件并写入表头
            if (!overwriteFileName() || !initExcelFile()) throw new Exception("创建excel文件失败！");
            // 置空数据量
            dataCount = 0;
            for (int i = fileCount * loopCount; i < (fileCount + 1) * loopCount; i++) {
                // 获取数据
                List<T> dataList = getExportExcelData(new PageEntity(everyQueryDataNum, (long) i * everyQueryDataNum));
                if (CollectionUtil.isEmpty(dataList)) {
                    if (i != 0 && i == fileCount * loopCount) {
                        if (fileCount == 1) excelFlag = true;
                        // 删除空文件
                        File file = new File(exportExcelEntity.getFilePath() + exportExcelEntity.getExcelFileName());
                        if (file.exists()) file.delete();
                        // 文件计数减一
                        fileCount--;
                        // 重置文件名
                        resetFileName();
                    }
                    break;
                }
                if (fileCount != 0) zipFlag = true;
                // 记录已查询的总数据量
                queryCount.addAndGet(dataList.size());
                // 写入数据
                writeDataToExcel(dataList);
                // 记录数据量
                dataCount += dataList.size();
                // 记录已导出的总数据量
                exportCount.addAndGet(dataList.size());
            }
            if (fileCount == 0 && dataCount < maxExportCount) excelFlag = true;
            // 文件计数
            fileCount++;
            log.info("export excel data count: {}", dataCount);
            // 关闭流
            excelWriter.finish();
        }
        // 压缩所有文件
        compressAllFile();
        // 导出结束时间
        endTime = System.currentTimeMillis();
        // 导出完成标识
        exportFinishFlag = true;

    }

    /**
     * 压缩所有excel文件
     *
     * @throws Exception
     */
    private void compressAllFile() throws Exception {

        if (fileCount == 1) return;
        File pathFile = new File(exportExcelEntity.getFilePath());
        File zipFile = new File(exportExcelEntity.getFilePath() + exportExcelEntity.getZipFileName());
        if (!(pathFile.exists() || pathFile.mkdirs()) || !(zipFile.exists() ? zipFile.delete() && zipFile.createNewFile() : zipFile.createNewFile()))
            throw new Exception("创建zip文件失败！");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            for (int i = 0; i < fileCount; i++) {
                File file = new File(exportExcelEntity.getFilePath() + exportExcelEntity.getExcelFileName().substring(0, exportExcelEntity.getExcelFileName().lastIndexOf("-")) + (i == 0 ? "" : "-" + i) + EXCEL_FILE_SUFFIX);
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zos.putNextEntry(zipEntry);
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int r;
                    while ((r = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, r);
                    }
                }
                zos.flush();
                // 删除此文件
                if (file.exists()) file.delete();
                log.info("zipAllExcelFile success, filePath:{}", file.getPath());
            }
        } catch (Exception e) {
            log.error("compressAllFile error", e);
        }

    }

    private void resetFileName() {

        exportExcelEntity.setExcelFileName(exportExcelEntity.getExcelFileName().substring(0, exportExcelEntity.getExcelFileName().lastIndexOf("-")) + (fileCount == 0 ? "" : "-" + fileCount) + EXCEL_FILE_SUFFIX);

    }

    private Boolean overwriteFileName() {

        exportExcelEntity.setExcelFileName((fileCount < 2 ? exportExcelEntity.getExcelFileName().substring(0, exportExcelEntity.getExcelFileName().lastIndexOf(".")) + (fileCount == 0 ? "" : "-" + fileCount) : exportExcelEntity.getExcelFileName().substring(0, exportExcelEntity.getExcelFileName().lastIndexOf("-")) + "-" + fileCount) + EXCEL_FILE_SUFFIX);
        return true;

    }

    private void writeDataToExcel(List<T> dataList) {

        List<List<Object>> dataSource = reloadDataSource(dataList);
        excelWriter.write(dataSource, writeSheet);

    }

    private List<List<Object>> reloadDataSource(List<T> dataList) {

        return dataList.stream().map(this::excelDataSubsequentProcessing).map(this::getDataList).collect(Collectors.toList());

    }

    private List<Object> getDataList(T t) {

        List<Object> dataList = new ArrayList<>();
        // 拿到对象的所有属性
        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ExportExcelField.class))
                .sorted(Comparator.comparingInt(field -> field.getAnnotation(ExportExcelField.class).order()))
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        Class<? extends ThrowingFunction<?, ?, ?>> converter = field.getAnnotation(ExportExcelField.class).converter();
                        Method applyMethod = !Objects.equals(converter, ExportExcelField.DefaultConverter.class) ? converter.getDeclaredMethod("apply", field.getType()) : null;
                        Object invokeResult;
                        if (applyMethod != null) {
                            applyMethod.setAccessible(true);
                            Constructor<? extends ThrowingFunction<?, ?, ?>> declaredConstructor = converter.getDeclaredConstructor();
                            declaredConstructor.setAccessible(true);
                            invokeResult = applyMethod.invoke(declaredConstructor.newInstance(), field.get(t));
                        } else {
                            invokeResult = field.get(t);
                        }
                        dataList.add(invokeResult == null ? field.getAnnotation(ExportExcelField.class).defaultValue() : invokeResult);
                    } catch (Exception e) {
                        log.error("getDataList error", e);
                    }
                });
        return dataList;

    }

    private Boolean initExcelFile() throws Exception {

        if (exportExcelEntity.getExcelFileName().equals(lastFileName) && excelWriter != null && writeSheet != null)
            return true;
        File pathFile = new File(exportExcelEntity.getFilePath());
        String fileFullPath = exportExcelEntity.getFilePath() + exportExcelEntity.getExcelFileName();
        File file = new File(fileFullPath);
        if (!(pathFile.exists() || pathFile.mkdirs()) || !(file.exists() ? file.delete() && file.createNewFile() : file.createNewFile()))
            return false;
        this.excelWriter = initExcelWriter(fileFullPath);
        excelWriter.write(getExcelFieldHead(), writeSheet);
        this.lastFileName = exportExcelEntity.getExcelFileName();
        return true;

    }

    private List<List<String>> getExcelFieldHead() {

        return Collections.singletonList(getAllExcelFieldNames().stream().map(ExportExcelField::name).collect(Collectors.toList()));

    }

    private List<ExportExcelField> getAllExcelFieldNames() {

        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(ExportExcelField.class))
                .map(field -> field.getAnnotation(ExportExcelField.class))
                .sorted(Comparator.comparingInt(ExportExcelField::order))
                .collect(Collectors.toList());

    }

    private WriteSheet initWriteSheet() {

        return !StringUtil.isEmpty(exportExcelEntity.getExcelHeader()) ? EasyExcelFactory.writerSheet(exportExcelEntity.getSheetName()).registerWriteHandler(new CustomCellWriteHandler()).registerWriteHandler(new HorizontalCellStyleStrategy(EasyExcelUtil.getHeadStyle(), EasyExcelUtil.getContentStyle())).registerWriteHandler(new ExcelColumnMergeHandler(getMergeArray(), getMergeRowIndex())).build() : EasyExcelFactory.writerSheet(exportExcelEntity.getSheetName()).registerWriteHandler(new CustomCellWriteHandler()).registerWriteHandler(new HorizontalCellStyleStrategy(EasyExcelUtil.getHeadStyle(), EasyExcelUtil.getContentStyle())).build();

    }

    private int getMergeRowIndex() {

        return maxExportCount + (StringUtil.isEmpty(exportExcelEntity.getExcelHeader()) ? 1 : 2);

    }

    private int[] getMergeArray() {

        return fieldOrder().stream().mapToInt(field -> field.getAnnotation(ExportExcelField.class).order()).toArray();

    }

    private ExcelWriter initExcelWriter(String fileFullPath) {

        return !StringUtil.isEmpty(exportExcelEntity.getExcelHeader()) ? EasyExcelFactory.write(fileFullPath).head(getExcelHead()).build() : EasyExcelFactory.write(fileFullPath).build();

    }

    private List<List<String>> getExcelHead() {

        return fieldOrder().stream().map(field -> Collections.singletonList(exportExcelEntity.getExcelHeader())).collect(Collectors.toList());

    }

    private List<Field> fieldOrder() {

        return Arrays.stream(clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(ExportExcelField.class)).sorted(Comparator.comparingInt(o -> o.getAnnotation(ExportExcelField.class).order())).collect(Collectors.toList());

    }

    /**
     * 必要文件名校验
     *
     * @param exportExcelEntity
     * @return
     */
    private ExportExcelEntity checkFileName(ExportExcelEntity exportExcelEntity) {

        return exportExcelEntity.setExcelFileName(StringUtil.isEmpty(exportExcelEntity.getExcelFileName()) ? EXCEL_FILE_PREFIX + sdf.format(new Date()) + EXCEL_FILE_SUFFIX : exportExcelEntity.getExcelFileName().endsWith(EXCEL_FILE_SUFFIX) ? exportExcelEntity.getExcelFileName() : exportExcelEntity.getExcelFileName() + EXCEL_FILE_SUFFIX)
                .setZipFileName(StringUtil.isEmpty(exportExcelEntity.getZipFileName()) ? exportExcelEntity.getExcelFileName().substring(0, exportExcelEntity.getExcelFileName().lastIndexOf(".")) + ZIP_FILE_SUFFIX : exportExcelEntity.getZipFileName().endsWith(ZIP_FILE_SUFFIX) ? exportExcelEntity.getZipFileName() : exportExcelEntity.getZipFileName() + ZIP_FILE_SUFFIX);

    }

}
