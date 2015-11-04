package com.jerrylin.erp.sevenzipjbinding.compress;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.sf.sevenzipjbinding.IOutCreateArchive;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItemBase;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
/**
 * 根據sevenzipjbinding官方範例程式，把建立壓縮檔的共同邏輯抽取出來，並利用JDK 8的特色實作
 * ref. http://sevenzipjbind.sourceforge.net/compression_snippets.html
 * 7-Zip-JBinding Release: 9.20-2.00beta-->7zip 9.20
 * @author JerryLin
 *
 * @param <T> IOutItemBase代表每個特定壓縮格式的檔案項目
 * @param <S> IOutCreateArchive封裝產生特定格式的壓縮檔的邏輯
 */
public abstract class BaseCompression<T extends IOutItemBase, S extends IOutCreateArchive<T>>{
	public static final int COMPRESS_LEVEL_COPY = 1;
	public static final int COMPRESS_LEVEL_FASTEST = 3;
	public static final int COMPRESS_LEVEL_FAST = 5;
	public static final int COMPRESS_LEVEL_NORMAL = 7;
	public static final int COMPRESS_LEVEL_MAXIMUM = 9;
	public static final String SEP = File.separator;
	private List<CompressableItem> items;
	public BaseCompression(){}
	public BaseCompression(String srcFolder){
		this.items = initCompressableItems(srcFolder);
	}
	public void setItems(List<CompressableItem> items) {
		this.items = items;
	}
	public List<CompressableItem> getItems() {
		return this.items;
	}
	public static String getParentDir(String path){
		if(StringUtils.isBlank(path)){
			throw new RuntimeException("path is blank");
		}
		int lastIdx = path.lastIndexOf(SEP);
		if(lastIdx == path.length()-1){
			path = path.substring(0, lastIdx);
		}
		String parentDir = path.substring(0, path.lastIndexOf(SEP)+1);
		return parentDir;
	}
	
	/**
	 * 遞迴掃描要壓縮的檔案，並轉換成sevenzipjbinding所需的型式
	 * @param folderPath: 可以是檔案，也可以是資料夾路徑
	 * @return
	 */
	List<CompressableItem> initCompressableItems(String folderPath){
		File folder = new File(folderPath);
		if(!folder.exists()){
			return Collections.emptyList();
		}
		String parentDir = getParentDir(folder.getAbsolutePath());
		List<CompressableItem> items = new ArrayList<>();
		items.add(CompressableItem.toItem(folder, parentDir));
		addCompressableItems(folder, items, parentDir);
		return items;
	}
	
	private static void addCompressableItems(File folder, List<CompressableItem> items, String parentDir){
		if(folder.isFile()){
			return;
		}
		for(File file : folder.listFiles()){
			items.add(CompressableItem.toItem(file, parentDir));
			if(file.isDirectory()){
				addCompressableItems(file, items, parentDir);
			}
		}
	}
	
	public void compress(String compressFile){
		boolean success = false;
		try(RandomAccessFile raf = new RandomAccessFile(compressFile, "rw");
			S outArchive = openArchive();){
			outArchive.setTrace(true); // Activate tracing
			config(outArchive);
//			System.out.println("file pointer: " + raf.getFilePointer());
			RandomAccessFileOutStream rafos = new RandomAccessFileOutStream(raf);
			outArchive.createArchive(rafos, getItems().size(), outCreateProgress());
			success = true;
		}catch(Throwable e){
			success = false;
			if(e instanceof SevenZipException){
				((SevenZipException)e).printStackTraceExtended();
			}
			throw new RuntimeException(e);
		}
		if(success){
			System.out.println("Compression operation succeeded!!");
		}
	}
	/**
	 * 開啟特定格式的壓縮支援
	 * @return
	 * @throws Throwable
	 */
	abstract S openArchive()throws Throwable;
	/**
	 * 壓縮個別檔案時，不同的處理方式
	 * @return
	 */
	abstract IOutCreateCallback<T> outCreateProgress();
	/**
	 * 壓縮設定
	 * @param outArchive
	 * @throws Throwable
	 */
	abstract void config(S outArchive)throws Throwable;
}
