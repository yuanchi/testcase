package com.jerrylin.erp.schedule.quartz;

import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzSample {
	public void startSample(){
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler scheduler = null;
		try{
			scheduler = sf.getScheduler();
			scheduler.start();
			JobDetail job = JobBuilder.newJob(HelloJob.class)
						.withIdentity("job1", "group1")
						.usingJobData("jobSays", "Hello World!!")// using JobDataMap note: http://quartz-scheduler.org/generated/2.2.2/html/qtz-all/#page/quartz-scheduler-webhelp%2Fco-job_and_job_details.html%23 
						.usingJobData("myFloatValue", 3.141f)
						.build();
			// SimepleTrigger
			// executing immediately, repeating per 2 second forever
			Trigger trigger1 = TriggerBuilder.newTrigger()// two most common types are simple triggers and cron triggers
				.withIdentity("trigger1", "group1")
				.startNow()
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(2).repeatForever())
				.forJob("job1", "group1") // identify job with name, group strings 
				.build();
			scheduler.scheduleJob(job, trigger1);
			Date someTime = new Date();
			Trigger trigger2 = TriggerBuilder.newTrigger()
				.withIdentity("trigger2", "group1")
				.startAt(someTime)
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(10).withRepeatCount(10))
				.build();
			// fire once, five minutes in the future
			Trigger trigger3 = TriggerBuilder.newTrigger()
				.withIdentity("trigger3", "group1")
				.startAt(DateBuilder.futureDate(5, IntervalUnit.MINUTE))
				.forJob(job)
				.build();
			// will fire now, then repeat every five minutes, util the hour 22:00
			Trigger trigger4 = TriggerBuilder.newTrigger()
				.withIdentity("trigger4", "group1")
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(5).repeatForever())
				.endAt(DateBuilder.dateOf(22, 0, 0))
				.build();
			// build a trigger that will fire at the top of the next hour, then repeat every 2 hours, forever
			Trigger trigger5 = TriggerBuilder.newTrigger()
				.withIdentity("trigger5")// because group is not specified, "trigger5" will be in the default group
				.startAt(DateBuilder.evenHourDate(null)) // get the next even hour
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(2).repeatForever().withMisfireHandlingInstructionFireNow()) // specify misfire instruction as part of simple schedule
				.build();
					
			// CronTrigger
			// will fire every other minute, between 8am and 5pm, every day
			Trigger trigger6 = TriggerBuilder.newTrigger()
				.withIdentity("trigger6", "group1")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0/2 8-17 * * ?"))
				.forJob("job1", "group1")
				.build();
			// will fire daily at 10:42 am
			Trigger trigger7 = TriggerBuilder.newTrigger()
				.withIdentity("trigger7")
				.withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(10, 42))
				.build();
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	public static class HelloJob implements Job{

		@Override
		public void execute(JobExecutionContext arg0)
				throws JobExecutionException {
			try{
				System.out.println("This is HelloJob");
			}catch(Throwable e){
				throw new JobExecutionException(e, true); // 可以指定是否失敗後立即重新執行，這裡設定是
			}
		}
	}
	
	private static void testStartSample(){
		QuartzSample qs = new QuartzSample();
		qs.startSample();
	}
	
	public static void main(String[]args){
		testStartSample();
	}
}
