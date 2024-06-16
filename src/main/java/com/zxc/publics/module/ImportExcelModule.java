package com.zxc.publics.module;

import com.alibaba.excel.EasyExcel;
import com.zxc.publics.entity.ImportResult;
import com.zxc.publics.entity.Result;
import com.zxc.publics.enums.CodeEnum;
import com.zxc.publics.listener.ExcelHeadCheckListener;
import com.zxc.publics.listener.ExcelImportDataListener;
import com.zxc.publics.module.moduleInterface.ImportModuleInterface;
import com.zxc.publics.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

import static com.zxc.publics.constant.ExcelImportConstant.*;

/**
 * @param: <T>
 * @Author: omnipotentQIQI
 * @Date: 2024/2/2 14:00
 * @Description: 简单Excel导入数据组件（1excel文件 <--> 1实体类）
 * @Remarks: 注意：使用之前请在对应实体类中对需要导入的属性添加 @ImportExcelField 注解
 * @Remarks: 支持：空字段赋予默认值、数据合法性校验、数据量校验、导入 / 校验 后的业务逻辑重写、数据导入失败数据导出、实时获取导入进度、获取导入结果等等
 * @Version: 1.0
 */
@Data
@Slf4j
@AllArgsConstructor
public abstract class ImportExcelModule<T> implements ImportModuleInterface<T> {

    private MultipartFile file;

    private Class<?> clazz;

    private int excelHeadRowIndex;

    private int excelDataNum;

    private int maxAllowDataNum;

    private int everyAddDataNum;

    private String illegalDataExportFileName;

    private ExcelImportDataListener<T> importDataListener;

    private long startTime;

    public ImportExcelModule(MultipartFile file, Class<?> clazz) {
        this.file = file;
        this.clazz = clazz;
        this.excelHeadRowIndex = EXCEL_HEAD_ROW_INDEX;
        this.maxAllowDataNum = EXCEL_MAX_ALLOW_DATA_NUM;
        this.everyAddDataNum = EXCEL_EVERY_ADD_DATA_NUM;
        this.illegalDataExportFileName = EXCEL_ILLEGAL_DATA_EXPORT_NAME;
    }

    public ImportExcelModule(MultipartFile file, Class<?> clazz, int excelMaxAllowDataNum, int excelEveryAddDataNum) {
        this.file = file;
        this.clazz = clazz;
        this.excelHeadRowIndex = EXCEL_HEAD_ROW_INDEX;
        this.maxAllowDataNum = excelMaxAllowDataNum;
        this.everyAddDataNum = excelEveryAddDataNum;
        this.illegalDataExportFileName = EXCEL_ILLEGAL_DATA_EXPORT_NAME;
    }

    public ImportExcelModule(MultipartFile file, Class<?> clazz, String illegalDataExportFileName) {
        this.file = file;
        this.clazz = clazz;
        this.illegalDataExportFileName = illegalDataExportFileName;
        this.excelHeadRowIndex = EXCEL_HEAD_ROW_INDEX;
        this.maxAllowDataNum = EXCEL_MAX_ALLOW_DATA_NUM;
        this.everyAddDataNum = EXCEL_EVERY_ADD_DATA_NUM;
    }

    public ImportExcelModule(MultipartFile file, Class<?> clazz, int excelHeadRowIndex) {
        this.file = file;
        this.clazz = clazz;
        this.excelHeadRowIndex = excelHeadRowIndex;
        this.maxAllowDataNum = EXCEL_MAX_ALLOW_DATA_NUM;
        this.everyAddDataNum = EXCEL_EVERY_ADD_DATA_NUM;
        this.illegalDataExportFileName = EXCEL_ILLEGAL_DATA_EXPORT_NAME;
    }

    public ImportExcelModule(MultipartFile file, Class<?> clazz, int excelHeadRowIndex, String illegalDataExportFileName) {
        this.file = file;
        this.clazz = clazz;
        this.excelHeadRowIndex = excelHeadRowIndex;
        this.illegalDataExportFileName = !StringUtil.isEmpty(illegalDataExportFileName) ? (illegalDataExportFileName + (illegalDataExportFileName.endsWith(EXCEL_FILE_SUFFIX) ? "" : EXCEL_FILE_SUFFIX)) : EXCEL_ILLEGAL_DATA_EXPORT_NAME;
        this.maxAllowDataNum = EXCEL_MAX_ALLOW_DATA_NUM;
        this.everyAddDataNum = EXCEL_EVERY_ADD_DATA_NUM;
    }

    /**
     * 获取导入耗时（在导入结束前调用返回null）
     */
    public Long getImportTime() {
        return importDataListener != null && importDataListener.getImportTime() != null ? importDataListener.getImportTime() : null;
    }

    /**
     * 获取导入进度
     */
    public String getImportProcess() {
        return importDataListener == null || excelDataNum == 0 ? "0%" : Math.round((double) importDataListener.getTotalNum().get() / excelDataNum * 100) + "%";
    }

    /**
     * 判断导入是否完成
     *
     * @return
     */
    public Boolean isImportFinish() {
        return importDataListener != null ? importDataListener.isFinish() : false;
    }

    /**
     * 获取导出的导入失败数据文件地址
     */
    public String getIllegalDataExportFilePath() {
        return EXCEL_ILLEGAL_DATA_EXPORT_PATH + illegalDataExportFileName;
    }

    /**
     * 获取导入结果
     */
    public Result<ImportResult> getImportResult() {
        ImportResult importResult = new ImportResult()
                .setProgress(getImportProcess())
                .setTimeConsuming(getImportTime())
                .setFailDataFileUrl(getIllegalDataExportFilePath())
                .setSourceCount(excelDataNum);
        if (importDataListener != null) {
            importResult.setSuccessCount(importDataListener.getAddNum().get())
                    .setFailCount(importDataListener.getErrorNum().get())
                    .setTotalCount(importDataListener.getTotalNum().get());
        }
        return !isImportFinish() ? new Result<>(CodeEnum.CODE_10001.getCode(), "导入未完成", importResult) : new Result<>(CodeEnum.CODE_10000.getCode(), "导入已完成", importResult);
    }

    /**
     * 开始导入Excel数据（同步）
     *
     * @throws Exception
     */
    public void startImport() throws Exception {

        // 记录开始时间
        startTime = System.currentTimeMillis();
        // 1.校验文件是否为合法的Excel文件
        checkExcelFile();
        // 2.校验Excel文件字段名是否合法
        checkExcelHead();
        // 3.校验数据条数是否合法
        checkExcelDataNum();
        // 4.数据处理
        handleExcelData();

    }

    /**
     * 开始导入Excel数据（异步）
     *
     * @throws Exception
     */
    public void startImportAsync() throws Exception {

        CompletableFuture.runAsync(() -> {
            try {
                startImport();
            } catch (Exception e) {
                log.error("startImport error", e);
            }
        });

    }

    /**
     * 处理Excel数据（同步）
     */
    private void handleExcelData() throws Exception {

        log.info("handleExcelData start >>> class: {}", clazz);
        importDataListener = new ExcelImportDataListener<>(excelHeadRowIndex, clazz, everyAddDataNum, this::beforeImport, this::afterImport, this::importExcelData, this::checkExcelData, illegalDataExportFileName, startTime);
        EasyExcel.read(file.getInputStream(), importDataListener).headRowNumber(1).sheet(0).doReadSync();

    }

    /**
     * 校验Excel文件字段名是否合法
     *
     * @throws Exception
     */
    private void checkExcelHead() throws Exception {

        // 创建监听器
        ExcelHeadCheckListener excelHeadCheckListener = new ExcelHeadCheckListener(clazz);
        // 文件表头读取
        EasyExcel.read(file.getInputStream(), excelHeadCheckListener).headRowNumber(1).sheet(0).doReadSync();
        // 设置文件条数(不包括表头)
        excelDataNum = excelHeadCheckListener.getFileSzie() - excelHeadRowIndex;
        log.info("导入的数据总行数为 {}", excelDataNum);
        // 从监听器中获取校验结果
        if (excelHeadRowIndex == 1 && !excelHeadCheckListener.isSuccess()) {
            throw new Exception("导入的Excel文件表头字段不合法！");
        }

    }

    /**
     * 校验文件是否为合法的Excel文件
     *
     * @throws Exception
     */
    private void checkExcelFile() throws Exception {

        if (file == null || file.getOriginalFilename() == null || !file.getOriginalFilename().endsWith(".xlsx")) {
            throw new Exception("excel文件不合法！");
        }

    }

    /**
     * 校验数据条数是否合法
     *
     * @throws Exception
     */
    private void checkExcelDataNum() throws Exception {

        if (excelDataNum < 0 || excelDataNum > maxAllowDataNum) {
            throw new Exception("导入的数据条数不合法！");
        }

    }

}
