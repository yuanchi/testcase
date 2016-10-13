package com.jerrylin.gentest;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.Test;

public class TestValuUtils {
	private static final String LOWER_LETTERS = "abcdefghiklmnopqrstuvwxyz";
	private static final String NUMBERS = "0123456789";
	private static final List<String> ATTRIBUTES = Arrays.asList("gov", "edu", "mil", "com", "net", "org", "idv");
	private static final List<String> COUNTRIES = 
		Arrays.asList(Locale.getAvailableLocales()).stream()
			.filter(l->l.getCountry()!=null && !"".equals(l.getCountry().trim()))
			.map(l->l.getCountry().toLowerCase()).collect(Collectors.toList());
	
	
	public static String randomString(int strLen){
		String range = LOWER_LETTERS + NUMBERS;
		int len = range.length();
		Random rand = new Random();
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<strLen; i++){
			int idx = rand.nextInt(len-1);
			String result = range.substring(idx, idx+1);
			sb.append(result);
		}
		return sb.toString();
	}
	public static String randomEmail(){
		Random rand = new Random();
		String user = randomString(5);
		String organization = randomString(4);
		String attr = ATTRIBUTES.get(rand.nextInt(ATTRIBUTES.size()-1));
		String region = COUNTRIES.get(rand.nextInt(COUNTRIES.size()-1));
		String email = user + "@" + organization + "." + attr + "." + region;
		return email;
	}
	public static LocalDate randomLocalDate(LocalDate start, LocalDate end){
		int minDay = (int)start.toEpochDay();
		int maxDay = (int)end.toEpochDay();
		long randomDay = minDay + new Random().nextInt(maxDay-minDay);
		LocalDate randomDate = LocalDate.ofEpochDay(randomDay);
		return randomDate;
	}
	public static Date randomSqlDate(LocalDate start, LocalDate end){
		return Date.valueOf(randomLocalDate(start, end));
	}
	@Test
	public void testRandomEmail(){
		for(int i=0; i<10; i++){
			System.out.println(randomEmail());
		}
	}
	@Test
	public void randomString(){
		for(int i=0; i<10; i++){
			System.out.println(randomString(8));
		}
	}
	@Test
	public void randomLocalDate(){
		LocalDate start = LocalDate.of(1979, 1, 1);
		LocalDate end = LocalDate.of(2000, 12, 31);
		for(int i=0; i<10; i++){
			System.out.println(randomLocalDate(start, end).toString());
		}
	}
	@Test
	public void randomSqlDate(){
		LocalDate start = LocalDate.of(1979, 1, 1);
		LocalDate end = LocalDate.of(2000, 12, 31);
		for(int i=0; i<10; i++){
			System.out.println(randomSqlDate(start, end));
		}
	
	}
}
