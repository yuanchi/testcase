package com.jerrylin.erp.util;

import java.security.MessageDigest;

public class EncrypUtils {
	// Java支援加密類型可參考:
	// https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#MessageDigest
	public static final String ENC_TYPE_MD2 = "MD2";
	public static final String ENC_TYPE_MD5 = "MD5";
	public static final String ENC_TYPE_SHA_1 = "SHA-1";
	public static final String ENC_TYPE_SHA_224 = "SHA-224";
	public static final String ENC_TYPE_SHA_256 = "SHA-256";
	public static final String ENC_TYPE_SHA_384 = "SHA-384";
	public static final String ENC_TYPE_SHA_512 = "SHA-512";
	
	public static String encryptAsMD5(String str){
		return encrypt(str, ENC_TYPE_MD5);
	}
	
	public static String encrypt(String str, String encType){
		String result = "";
		try{
			MessageDigest md = MessageDigest.getInstance(encType);
			md.update(str.getBytes());
			result = toHexString(md.digest());
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return result;
	}
	
	private static String toHexString(byte[] in){
		StringBuilder hexString = new StringBuilder();
		for(int i = 0; i < in.length; i++){
			String hex = Integer.toHexString(0xFF & in[i]);
			if(hex.length() == 1){
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
