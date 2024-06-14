package com.zxc.publics.util;

import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

public class EasyExcelUtil {

	public static WriteCellStyle getHeadStyle() {
		// 头的策略
		WriteCellStyle headWriteCellStyle = new WriteCellStyle();
		// 背景色
		headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		// 头的字体大小
		WriteFont headWriteFont = new WriteFont();
		headWriteFont.setFontHeightInPoints((short) 12);
		headWriteCellStyle.setWriteFont(headWriteFont);
		return headWriteCellStyle;
	}

	public static WriteCellStyle getContentStyle() {
		// 内容的策略
		WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
		// 设置 自动换行
		contentWriteCellStyle.setWrapped(false);
		// 设置 垂直居中
		contentWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		// 设置 水平居中
		contentWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
		return contentWriteCellStyle;
	}

}
