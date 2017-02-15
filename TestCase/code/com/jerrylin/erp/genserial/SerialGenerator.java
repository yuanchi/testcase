package com.jerrylin.erp.genserial;

import java.text.NumberFormat;

public abstract class SerialGenerator {
	protected abstract String getId();
	protected abstract String getNext() throws Throwable;
	/**
	 * 將傳入數字加1，左補0
	 * @param no
	 * @return
	 */
	protected String increaseNo(String no){
		long num = Long.parseLong(no) + 1l;
		int len = no.length();
		return formatNo(num, len);
	}
	/**
	 * 給一個數字並指定長度，長度不足左補0
	 * @param num
	 * @param len
	 * @return
	 */
	protected String formatNo(long num, int len){
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMaximumIntegerDigits(len);
		nf.setMinimumIntegerDigits(len);
		nf.setGroupingUsed(false);
		return nf.format(num);
	}
}
