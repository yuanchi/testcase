package com.jerrylin.erp.qr;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;


public class QRCodeGenerator {
	
	public static void simple(String content, String path){
		File file = new File(path);
		try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))){
			QRCode.from(content).to(ImageType.JPG).withCharset("UTF-8").writeTo(bos); // 加上編碼輸出，中文字才會正常
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	private static void testSimple(){
		String content = "這是洪遠製作的QR Code，網址是: https://github.com/kenglxn/QRGen";
		String path = "C:\\Users\\JerryLin\\Desktop\\pic\\qrsample.jpg";
		simple(content, path);
	}
	public static void main(String[]args){
		testSimple();
	}
}
