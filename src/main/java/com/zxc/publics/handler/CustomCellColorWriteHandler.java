package com.zxc.publics.handler;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.util.StyleUtil;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.AbstractCellStyleStrategy;
import org.apache.poi.ss.usermodel.*;

public class CustomCellColorWriteHandler extends AbstractCellStyleStrategy {

    private CellStyle headCellStyle;
    private CellStyle contentCellStyle;
    private CellStyle contentErrCellStyle;
    private CellStyle contentErrRepeateCellStyle;

    @Override
    protected void initCellStyle(Workbook workbook) {

        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        // 背景色
        headWriteCellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 12);
        headWriteCellStyle.setWriteFont(headWriteFont);
        headCellStyle = StyleUtil.buildHeadCellStyle(workbook, headWriteCellStyle);

        //设置内容样式
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setWrapText(false);
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        cellStyle.setFont(font);
        contentCellStyle = cellStyle;

        contentErrCellStyle = workbook.createCellStyle();
        contentErrCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentErrCellStyle.setAlignment(HorizontalAlignment.CENTER);
        contentErrCellStyle.setWrapText(false);
        contentErrCellStyle.setFont(font);
        contentErrCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        contentErrCellStyle.setFillForegroundColor(IndexedColors.RED.index);

        contentErrRepeateCellStyle = workbook.createCellStyle();
        contentErrRepeateCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentErrRepeateCellStyle.setAlignment(HorizontalAlignment.CENTER);
        contentErrRepeateCellStyle.setWrapText(false);
        contentErrRepeateCellStyle.setFont(font);
        contentErrRepeateCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        contentErrRepeateCellStyle.setFillForegroundColor(IndexedColors.GREEN.index);

    }

    @Override
    protected void setHeadCellStyle(Cell cell, Head head, Integer integer) {
        cell.setCellStyle(headCellStyle);
    }

    //根据不同的错误变换单元格样式
    @Override
    protected void setContentCellStyle(Cell cell, Head head, Integer integer) {

        // 设置单元格样式
        if (cell.getCellTypeEnum() == CellType.STRING && cell.getStringCellValue() != null
                && cell.getStringCellValue().contains("@|@")) {
            cell.setCellValue(cell.getStringCellValue().replace("@|@", "").replace("null", ""));
            cell.setCellStyle(contentErrCellStyle);
        } else if (cell.getCellTypeEnum() == CellType.STRING && cell.getStringCellValue() != null
                && cell.getStringCellValue().contains("@#|#@")) {
            cell.setCellValue(cell.getStringCellValue().replace("@#|#@", "").replace("null", ""));
            cell.setCellStyle(contentErrRepeateCellStyle);
        } else {
            cell.setCellStyle(contentCellStyle);
        }

    }

}
