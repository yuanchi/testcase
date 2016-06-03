package com.jerrylin.erp.test;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;


public class GoogleAuthTests {

	@Test
	public void testExpired(){		
        GoogleAuthenticatorConfigBuilder gacb =
                new GoogleAuthenticatorConfigBuilder()
                        .setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30)) // 每30秒變更一次密碼，但並非以啟動時間點計算時間間隔
                        .setWindowSize(5)
                        .setCodeDigits(6);
		
		GoogleAuthenticator gAuth = new GoogleAuthenticator(gacb.build());
		final GoogleAuthenticatorKey key = gAuth.createCredentials();
		
		int count = 0;
		final int MAX = 20;
		int firstPwd = 0;
		while(count < MAX){
			int pwd = gAuth.getTotpPassword(key.getKey());
			if(count == 0){
				firstPwd = pwd;
			}
			System.out.println((count * 5) + "sec pwd: " + pwd + ", authorized: " + gAuth.authorize(key.getKey(), firstPwd));
			count++;
			try{
				Thread.sleep(5000);
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
		}
	}
		
	public static void main(String[]args){
		GoogleAuthTests t = new GoogleAuthTests();
		t.testExpired();
	}
}
