/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeus.server.rabbit.common.consumer;

import com.jeus.server.rabbit.common.quebbit.RabbitConsumer;
import java.util.logging.Level;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 *
 * @author jeus
 */
public class ConsumerTest2 implements Job {

    private static final Logger LOGGER = Logger.getLogger(ConsumerTest2.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        while (true) {
            try {
                LOGGER.info("RUNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN");
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(ConsumerTest2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
