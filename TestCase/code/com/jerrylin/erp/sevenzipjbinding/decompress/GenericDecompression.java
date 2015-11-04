package com.jerrylin.erp.sevenzipjbinding.decompress;

import java.io.File;
import java.io.RandomAccessFile;

import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import com.jerrylin.erp.function.ConsumerThrowable;
import com.jerrylin.erp.sevenzipjbinding.compress.BaseCompression;
/**
 * ref. http://sevenzipjbind.sourceforge.net/extraction_snippets.html#extracting-single-file-std-int
 * ref. http://sourceforge.net/p/sevenzipjbind/discussion/757964/thread/b64a36fb/
 * @author JerryLin
 *
 */
public class GenericDecompression {
	public static final String SEP = File.separator;
	public void openInArchive(String path, ConsumerThrowable<IInArchive> executeLogic){
		try(RandomAccessFile raf = new RandomAccessFile(path, "r");
			IInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(raf));){
			
			if(executeLogic!=null){
				executeLogic.accept(inArchive);
			}
			
		}catch(Throwable e){
			if(e instanceof SevenZipException){
				((SevenZipException)e).printStackTraceExtended();
			}
			throw new RuntimeException(e);
		}
	}
	/**
	 * 顯示壓縮檔中各項目的資訊，實測壓縮後檔案大小(PropID.PACKED_SIZE)這項資料不準確
	 * @param path
	 */
	public void showItemInfo(String path){
		openInArchive(path, inArchive->{
			int itemCount = inArchive.getNumberOfItems();
			System.out.println("Total Count: " + itemCount);
			System.out.println("  Size  |  Compr.Sz.  |  Filename");
			System.out.println("--------+-------------+----------");
			for(int i = 0; i < itemCount; i++){
				System.out.println(String.format("%9s | %9s | %s",
						inArchive.getProperty(i, PropID.SIZE),
						inArchive.getProperty(i, PropID.PACKED_SIZE),
						inArchive.getProperty(i, PropID.PATH)));
			}
		});
	}
	
	public void extract(String path){
		openInArchive(path, inArchive->{
			// 假如不需要全部解壓縮，可以在in當中指定實際要解壓縮的檔(index)，這樣可以提升效能。
			int[] in = new int[inArchive.getNumberOfItems()];
			for(int i = 0; i < in.length; i++){
				in[i] = i;
			}
			String extractRoot = BaseCompression.getParentDir(path);
			System.out.println("extractRoot: " + extractRoot);
			inArchive.extract(in, false, new GenericExtractCallback(inArchive, extractRoot));
		});
	}
	
	private static void testShowItemInfo(){
		String pic7z = "C:"+SEP+"Users"+SEP+"JerryLin"+SEP+"Desktop"+SEP+"pic.7z";
		GenericDecompression gd = new GenericDecompression();
		gd.showItemInfo(pic7z);
	}
	
	private static void testExtract(){
		String pic7z = "C:"+SEP+"Users"+SEP+"JerryLin"+SEP+"Desktop"+SEP+"ebook"+SEP+"pic.7z";
		GenericDecompression gd = new GenericDecompression();
		gd.extract(pic7z);
	}
	public static void main(String[]args){
//		testShowItemInfo();
		testExtract();
	}
}
