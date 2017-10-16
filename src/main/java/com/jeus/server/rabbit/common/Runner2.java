/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeus.server.rabbit.common;

import com.jeus.server.rabbit.common.consumer.ConsumerTest2;
import org.quartz.JobDetail;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.JobBuilder.*;
import static org.quartz.DateBuilder.*;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import static org.quartz.SimpleScheduleBuilder.*;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author jeus
 */
public class Runner2 {

    private static final Logger LOGGER = Logger.getLogger(Runner2.class);

    public static void main(String[] args) {
        try {

            JobDetail job = newJob(ConsumerTest2.class).withIdentity("myJob", "myGroup")
                    .storeDurably().requestRecovery().usingJobData("someKey", "someValue").build();

            //Trigger every 3 second start from 2 second after start
            Trigger trg1 = newTrigger().withIdentity("myTrigger").startAt(futureDate(2, IntervalUnit.SECOND))
                    .withPriority(6).forJob(job).withSchedule(simpleSchedule().withIntervalInSeconds(3)
                    .repeatForever()).build();

            //Trigger trg2 = newTrigger().withSchedule()build();
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.scheduleJob(job, trg1);
            //Thread.sleep(5000);
            if (scheduler.isStarted()) {
                System.out.println(">>> IS STARTED <<<<<<<<<<<<<<<<<<<<<<<<<<");
            } else {
                System.out.println(">>> STARTING <<<<<<<<<<<<<<<<<<<<<<<<<<");
                scheduler.start();
                System.out.println("----------------------> LOGGER.....");
            }
            //scheduler.shutdown();
        } catch (SchedulerException ex) {
            LOGGER.info("-----------------------> EXCEPTION " + ex.getMessage());
        }

    }

}
