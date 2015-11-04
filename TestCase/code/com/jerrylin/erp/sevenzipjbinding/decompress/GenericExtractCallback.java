package com.jerrylin.erp.sevenzipjbinding.decompress;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;
/**
 * 解壓縮相關設定，此處把解壓縮檔案放在與來源檔一樣的地方(透過extractPath)
 * @author JerryLin
 *
 */
public class GenericExtractCallback implements IArchiveExtractCallback {
	private int hash = 0;
	private int size = 0;
	private int index;
	private boolean skipExtraction;
	private IInArchive inArchive;
	private String extractPath;
	private int callCount;

	public GenericExtractCallback(IInArchive inArchive, String extractPath){
		this.inArchive = inArchive;
		this.extractPath = extractPath;
	}
	
	@Override
	public void setCompleted(long complete) throws SevenZipException {}

	@Override
	public void setTotal(long total) throws SevenZipException {}

	@Override
	public ISequentialOutStream getStream(int idx, ExtractAskMode extractAskMode)
			throws SevenZipException {
		callCount++;
		System.out.println("callCount: " + callCount);
		this.index = idx;
		skipExtraction = (Boolean)inArchive.getProperty(idx, PropID.IS_FOLDER);
		if(skipExtraction || extractAskMode != ExtractAskMode.EXTRACT){
			return null;
		}
		return new ISequentialOutStream(){
			@Override
			public int write(byte[] data) throws SevenZipException {
				int dataLen = data.length;
				hash ^= Arrays.hashCode(data);
				size += dataLen;
				
				String filePath = inArchive.getStringProperty(index, PropID.PATH);
				File dir = new File(extractPath);
//				System.out.println("extract path: " + extractPath+filePath);
				File path = new File(extractPath+filePath);
				if(!dir.exists()){
					dir.mkdirs();
				}
				try{
					if(!path.exists()){
						path.getParentFile().mkdirs();
						path.createNewFile();
					}
				}catch(Throwable e){
					throw new SevenZipException("Error making dir", e);
				}
				try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path))){
					bos.write(data);
				}catch(Throwable e){
					throw new SevenZipException("Error writing data", e);
				}
				return dataLen;
			}
		};
	}

	@Override
	public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {}

	@Override
	public void setOperationResult(ExtractOperationResult extractOperationResult)
			throws SevenZipException {
		if(skipExtraction){
			return;
		}
		if(extractOperationResult != ExtractOperationResult.OK){
			System.out.println("Extraction err");
		}else{
			System.out.println(String.format("%9X  |  %10s  |  %s",
					hash,
					size,
					inArchive.getProperty(index, PropID.PATH)));
			hash = 0;
			size = 0;
		}
	}

}
