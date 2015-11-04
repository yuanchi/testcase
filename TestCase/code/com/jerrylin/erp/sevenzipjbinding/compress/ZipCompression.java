package com.jerrylin.erp.sevenzipjbinding.compress;

import net.sf.sevenzipjbinding.IOutCreateArchiveZip;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItemZip;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.util.ByteArrayStream;
/**
 * 壓縮ZIP檔，此處的壓縮檔必須透過7zip才能解壓縮
 * @author JerryLin
 *
 */
public class ZipCompression  extends BaseCompression<IOutItemZip, IOutCreateArchiveZip>{
	public ZipCompression(String srcFolder){
		super(srcFolder);
	}

	@Override
	public IOutCreateArchiveZip openArchive() throws Throwable {
		IOutCreateArchiveZip outArchive = SevenZip.openOutArchiveZip();
		return outArchive;
	}

	@Override
	public IOutCreateCallback<IOutItemZip> outCreateProgress() {
		return new IOutCreateCallback<IOutItemZip>(){
			@Override
			public void setOperationResult(boolean operationResultOk)
					throws SevenZipException {
				// Track each operation result here
			}

			@Override
			public void setCompleted(long complete) throws SevenZipException {
				// Track operation progress here
			}

			@Override
			public void setTotal(long total) throws SevenZipException {
				// Track operation progress here
			}
			@Override
			public IOutItemZip getItemInformation(int idx,
					OutItemFactory<IOutItemZip> outItemFactory) throws SevenZipException {
				int attr = PropID.AttributesBitMask.FILE_ATTRIBUTE_UNIX_EXTENSION;
				IOutItemZip item = outItemFactory.createOutItem();
				if(getItems().get(idx).getContent() == null){
					// Directory
					item.setPropertyIsDir(true);
					attr |= PropID.AttributesBitMask.FILE_ATTRIBUTE_DIRECTORY;
					attr |= 0x81ED << 16; // permissions: drwxr-xr-x
				}else{
					// File
					item.setDataSize((long)getItems().get(idx).getContent().length);
					attr |= 0x81a4 << 16; // permissions: -rw-r--r--
				}
				item.setPropertyPath(getItems().get(idx).getPath());
				item.setPropertyAttributes(attr);
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
	public void config(IOutCreateArchiveZip outArchive) throws Throwable {
		outArchive.setLevel(COMPRESS_LEVEL_FAST);
	}
	
	public static void main(String[]args){
//		String pic = "C:"+File.separator+"Users"+File.separator+"JerryLin"+File.separator+"Desktop"+File.separator+"pic"+File.separator;
//		String pic7z = "C:"+File.separator+"Users"+File.separator+"JerryLin"+File.separator+"Desktop"+File.separator+"pic.zip";
//		String onpos = "C:"+File.separator+"ONE-POS DB"+File.separator+"onepos.dat";
//		String onepos7z = "C:"+File.separator+"ONE-POS DB"+File.separator+"onepos.zip";
//		ZipCompression szc = new ZipCompression(pic);
//		List<CompressableItem> items = szc.getItems();
//		System.out.println("item count:" + items.size());
//		szc.compress(pic7z);
	}
}
