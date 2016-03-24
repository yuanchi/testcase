package com.jerrylin.erp.service;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class TimeService {
	public static final DateFormat DF_yyyyMMdd_DASHED = new SimpleDateFormat("yyyy-MM-dd");
	public static final DateFormat DF_MMddyyyy_DASHED = new SimpleDateFormat("MM-dd-yyyy");
	public static final DateFormat DF_yyyyMMdd_SLASHED = new SimpleDateFormat("yyyy/MM/dd");
	public static final DateFormat DF_MMddyyyy_SLASHED = new SimpleDateFormat("MM/dd/yyyy");
	public static final DateFormat DF_yyyyMMdd_DASHED_EXTEND_TO_SEC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private ZoneId zoneId = ZoneId.systemDefault();
	
	public Date atStartOfToday(){
		LocalDate localDate = LocalDate.now();
		Date date = new Date(localDate.atStartOfDay(zoneId).toInstant().toEpochMilli());
		return date;
	}
	public int differFromUTC(){
		int diff = ZoneOffset.of(zoneId.getId()).compareTo(ZoneOffset.UTC);
		return diff;
	}
	private static void testDifferFromUTC(){
		TimeService ts = new TimeService();
		System.out.println("diff: " + ts.differFromUTC());
	}
	public static Set<String> getAvailableZoneIds(){
		TreeSet<String> sortedZones = new TreeSet<>(ZoneId.getAvailableZoneIds());
		return sortedZones;
	}
	private static void testGetAvailableZoneIds(){
		Set<String> sortedZones = getAvailableZoneIds();
		for(String zone : sortedZones){
			System.out.println(zone);
		}
		System.out.println("");
		System.out.println("Number of zones: " + sortedZones.size());
	}
	public static void main(String[]args){
//		testGetAvailableZoneIds();
		testDifferFromUTC();
	}
}
