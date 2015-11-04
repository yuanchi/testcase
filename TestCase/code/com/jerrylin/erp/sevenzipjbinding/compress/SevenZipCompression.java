package com.jerrylin.erp.sevenzipjbinding.compress;

import java.io.File;
import java.util.List;

import net.sf.sevenzipjbinding.IOutCreateArchive7z;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItem7z;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.util.ByteArrayStream;

public class SevenZipCompression extends BaseCompression<IOutItem7z, IOutCreateArchive7z> {
	public SevenZipCompression(String srcFolder){
		super(srcFolder);
	}

	@Override
	public IOutCreateArchive7z openArchive()throws Throwable{
		IOutCreateArchive7z outArchive = SevenZip.openOutArchive7z();
		return outArchive;
	}

	@Override
	public IOutCreateCallback<IOutItem7z> outCreateProgress() {
		return new IOutCreateCallback<IOutItem7z>(){
			@Override
			public void setCompleted(long complete) throws SevenZipException {
				// Track operation progress here
			}
			@Override
			public void setTotal(long total) throws SevenZipException {
				// Track operation progress here
			}
			@Override
			public void setOperationResult(boolean operationResultOk)
					throws SevenZipException {
				// Track operation result here
			}
			@Override
			public IOutItem7z getItemInformation(int idx,
					OutItemFactory<IOutItem7z> outItemFactory) throws SevenZipException {
				IOutItem7z item = outItemFactory.createOutItem();
				if(getItems().get(idx).getContent() == null){
					item.setPropertyIsDir(true);
				}else{
					item.setDataSize((long)getItems().get(idx).getContent().length);
				}
				item.setPropertyPath(getItems().get(idx).getPath());
				return item;
			}

			@Override
			public ISequentialInStream getStream(int idx)
					throws SevenZipException {
				if(getItems().get(idx).getContent() == null){
					return null;
				}
				return new ByteArrayStream(getItems().get(idx).getContent(), true);
			}
		};
	}

	@Override
	public void config(IOutCreateArchive7z outArchive)throws Throwable{
		outArchive.setLevel(COMPRESS_LEVEL_FAST);
		outArchive.setSolid(true);
	}
	
	/**
	 * 取得程式執行專案根目錄位置
	 * ex: C:\Users\JerryLin\Documents\GitHub\angrycat-app\TestWeb
	 * @return
	 */
	static String getProjectRoot(){
		File file = new File("xxx.txt");
		String path = file.getAbsolutePath();
		System.out.println("project root: " + path);
		return path;
	}
	/**
	 * 取得程式執行專案編譯檔根目錄位置
	 * ex: /C:/Users/JerryLin/Documents/GitHub/angrycat-app/TestWeb/target/classes/
	 * @return
	 */
	static String getProjectCompileRoot(){
		String path = SevenZipCompression.class.getClassLoader().getResource("").getPath();
		System.out.println("project compile root: " + path);
		return path;
	}
	private static void testCompress(){
		String pic = "C:"+SEP+"Users"+SEP+"JerryLin"+SEP+"Desktop"+SEP+"pic"+SEP;
		String pic7z = "C:"+SEP+"Users"+SEP+"JerryLin"+SEP+"Desktop"+SEP+"pic.7z";
		String onpos = "C:"+SEP+"ONE-POS DB"+SEP+"onepos.dat";
		String onepos7z = "C:"+SEP+"ONE-POS DB"+SEP+"onepos.7z";
		SevenZipCompression szc = new SevenZipCompression(onpos);
		List<CompressableItem> items = szc.getItems();
		System.out.println("item count:" + items.size());
		szc.compress(onepos7z);
	}
	public static void main(String[]args){
		testCompress();
//		getProjectRoot();
//		getProjectCompileRoot();
	}
}
