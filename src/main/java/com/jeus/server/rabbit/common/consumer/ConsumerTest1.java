/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeus.server.rabbit.common.consumer;

import com.jeus.server.rabbit.common.ShutdownAware;
import com.jeus.server.rabbit.common.quebbit.Priority;
import com.jeus.server.rabbit.common.quebbit.RabbitConfig;
import com.jeus.server.rabbit.common.quebbit.RabbitConsumer;
import com.jeus.server.rabbit.common.quebbit.RabbitProducer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
 

/**
 * TODO Explain this class
 */
enum MessageType {
    MO(1), MT(2);
    public int code;

    MessageType(int code) {
        this.code = code;
    }
}

enum DataCode {
    English(0), Binary(4), Farsi(8), UTF8(12), GB18030(15), Undefined(255);
    public int code;

    DataCode(int code) {
        this.code = code;
    }
}

enum Channel {
    SMS(1), USSD(2), MMS(3), IVR(4), THREE_G(5);

    public int code;

    Channel(int code) {
        this.code = code;
    }
}

public class ConsumerTest1 extends RabbitConsumer implements ShutdownAware {

    //private static final Logger LOGGER = Logger.getLogger(ConsumerTest1.class);
    //final ExecutorService wsExecutorService;
    final int startTime;
    final int endTime;
    final RabbitProducer self;

    volatile int runningJobs = 0;
    volatile boolean shutdown = false;

    public ConsumerTest1(RabbitConfig config, int workersCount, RabbitProducer self, int startTime, int endTime) {
        super(config, workersCount, 0, startTime, endTime);
        this.self = self;
        this.startTime = startTime;
        this.endTime = endTime;
//        wsExecutorService = Executors.newFixedThreadPool(workersCount + (workersCount / 2));
        final long checkPeriodMilliSec = 5 * 60 * 1000;
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(">>RUN SCHEDULE");
            }
        }, checkPeriodMilliSec, checkPeriodMilliSec);
    }

    public void callRouteCallBack(String reference) {

    }

    @Override
    public void consume(byte[] bytes) {
        try {
            if (shutdown) {
                System.out.println(">>SHUTDOWN");
                this.self.submit(bytes, Priority.HIGH);
                return;
            }
            String msg = new String(bytes, "UTF-8");
            System.out.println(">>CONSUME " + msg);
            if (msg.equals("STOP")) {
                System.out.println("CLOSING <==========================");
                stop();
            }
        } catch (UnsupportedEncodingException ex) {
            System.out.println(">>CONSUMER EXCEPTION " + ex.getMessage());
        }
    }

    @Override
    public void onShutdown() {
        super.stop();
        shutdown = true;
        final long shutdownWaitStartTime = System.currentTimeMillis();
        //TODO: shutdown have to implement.
//        wsExecutorService.shutdown();
//        while ((System.currentTimeMillis() - shutdownWaitStartTime < (90 * 1000)) && !wsExecutorService.isTerminated()) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                System.out.println(">>CONSUMER EXCEPTION " + e.getMessage());
//            }
//        }

        /**
         * do this while to all running job done or 1:30 Minute
         */
        while ((System.currentTimeMillis() - shutdownWaitStartTime < (90 * 1000)) && runningJobs > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

    }

    @Override
    public void consume(List<String> data) throws IOException {
        for (String msg : data) {
            System.out.println(">>CONSUME LIST " + msg);
        }
    }

}
