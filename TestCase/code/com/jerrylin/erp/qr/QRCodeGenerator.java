package com.jerrylin.erp.qr;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * use third party lib 'qrgen'(based on zxing) generating QR Code
 * @author JerryLin
 *
 */
public class QRCodeGenerator {
	private static String extensionAsType(String path){
		int lastIdx = path.lastIndexOf(".");
		String extension = path.substring(lastIdx+1, path.length());
		return extension.toUpperCase();
	}
	public static void zxingEncode(String content, String path){
		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		final int QRCODE_IMAGE_HEIGHT = 250;
		final int QRCODE_IMAGE_WIDTH = 250;
		QRCodeWriter qrWriter = new QRCodeWriter();
		try {
			BitMatrix matrix = qrWriter.encode(content, BarcodeFormat.QR_CODE, QRCODE_IMAGE_WIDTH, QRCODE_IMAGE_HEIGHT, hints);
			BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
			File imageFile = new File(path);
			ImageIO.write(image, extensionAsType(path), imageFile);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	private static void testZxingEncode(){
//		zxingEncode("今天是好天氣啊", "C:\\Users\\JerryLin\\Desktop\\pic\\qrsample.jpg");
		zxingEncode("我愛你", "C:\\Users\\JerryLin\\Desktop\\pic\\iloveyou.jpg");
	}
	/**
	 * generate qrcode with logo picture
	 * ref. https://skrymerdev.wordpress.com/2012/09/22/qr-code-generation-with-zxing/
	 * logo image should be as possible as small
	 * @param content
	 * @param path
	 * @param overlayPath
	 */
	public static void zxingEncodeOverlay(String content, String path, String overlayPath){
		Map<EncodeHintType, Object> hints = new HashMap<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		
		final int QRCODE_IMAGE_HEIGHT = 300;
		final int QRCODE_IMAGE_WIDTH = 300;
		QRCodeWriter qrWriter = new QRCodeWriter();
		try {
			BitMatrix matrix = qrWriter.encode(
									content, 
									BarcodeFormat.QR_CODE, 
									QRCODE_IMAGE_WIDTH, 
									QRCODE_IMAGE_HEIGHT,
									hints);
			BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);
			BufferedImage overlay = ImageIO.read(new File(overlayPath));
			int deltaHeight = image.getHeight() - overlay.getHeight();
			int deltaWidth = image.getWidth() - overlay.getWidth();
			BufferedImage combined = new BufferedImage(QRCODE_IMAGE_HEIGHT, QRCODE_IMAGE_WIDTH, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D)combined.getGraphics();
			g.drawImage(image, 0, 0, null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			g.drawImage(overlay, (int)Math.round(deltaWidth/2), (int)Math.round(deltaHeight/2), null);
			File imageFile = new File(path);
			ImageIO.write(combined, extensionAsType(path), imageFile);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	private static void testZxingEncodeOverlay(){
		zxingEncodeOverlay("我愛你", "C:\\Users\\JerryLin\\Desktop\\pic\\iloveyouoverlay.jpg", "C:\\Users\\JerryLin\\Desktop\\pic\\ohm_logo_slowly.jpg");
	}
	public static void simple(String content, String path){
		File file = new File(path);
		try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))){
			QRCode.from(content).to(ImageType.JPG).withCharset("UTF-8").writeTo(bos); // 加上編碼輸出，中文字才會正常
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	private static void testSimple(){
		String content1 = "這是洪遠製作的QR Code，網址是: https://github.com/kenglxn/QRGen";
		String content2 = "http://www.amazon.com/Kindle-Wireless-Reading-Display-Globally/dp/B003FSUDM4/ref=amb_link_353259562_2?pf_rd_m=ATVPDKIKX0DER &pf_rd_s=center-10&pf_rd_r=11EYKTN682A79T370AM3&pf_rd_t=201&pf_rd_p=1270985982&pf_rd_i=B002Y27P3M";
		String content3 = "javascript:(function(){alert('This is yours');})()"; // 這只能在書籤或網頁連結使用
		String content4 = "mailto:foo@example.com?cc=bar@example.com&subject=Greetings%20from%20Cupertino!&body=Wish%20you%20were%20here!";
		String path = "C:\\Users\\JerryLin\\Desktop\\pic\\qrsample.jpg";
		simple(content4, path);
	}
	public static void main(String[]args){
//		testSimple();
//		testZxingEncode();
		testZxingEncodeOverlay();
	}
}
