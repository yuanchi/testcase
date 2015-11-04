package com.jerrylin.erp.sevenzipjbinding.compress;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
/**
 * 可壓縮的檔案項目，代表要被壓縮的每個檔案，目錄也算一個，但目錄沒有檔案內容。
 * 此類別的結構是根據sevenzipjbinding的(壓縮)需求建立
 * @author JerryLin
 *
 */
public class CompressableItem {
	private String path;
	private byte[] content;
	public CompressableItem(String path, byte[]content){
		this.path = path;
		this.content = content;
	}
	/**
	 * 此處的路徑為壓縮路徑，不能以來源檔案絕對位置的角度思考，而是要以被壓縮的檔案位置去思考。
	 * ex: 如果設為C:\Users\JerryLin\Documents\aaa.txt，程式就會根據這個結構建立壓縮檔。
	 * 所以會變成:壓縮檔根目錄\C\Users\JerryLin\Documents\aaa.txt
	 * @return
	 */
	public String getPath() {
		return path;
	}
	/**
	 * see the method {@link #getPath()}.
	 */
	public void setPath(String path) {
		this.path = path;
	}
	public byte[] getContent() {
		return content;
	}
	public void setContent(byte[] content) {
		this.content = content;
	}
	/**
	 * 
	 * @param file: 要轉成可壓縮檔案的項目
	 * @param parentDir: 檔案來源的上層目錄結構，在同一批被壓縮的檔案中，一定會有一個一致的上層目錄。
	 * @return
	 */
	public static CompressableItem toItem(File file, String parentDir){
		String path = file.getAbsolutePath();
		String simplePath = path.replace(parentDir, "");
		if(file.isDirectory()){
			return new CompressableItem(simplePath, null);
		}
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));){
			byte[] content = IOUtils.toByteArray(bis);
			return new CompressableItem(simplePath, content);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
}
