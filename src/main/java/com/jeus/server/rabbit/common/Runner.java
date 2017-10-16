package com.jeus.server.rabbit.common;

import com.jeus.server.rabbit.common.consumer.ConsumerTest1;
import com.jeus.server.rabbit.common.producer.ProducerTest1;
import com.jeus.server.rabbit.common.quebbit.RabbitConfig;
import com.jeus.server.rabbit.common.quebbit.RabbitProducer;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jeus
 */
public class Runner {

    public static void main(String[] arg) {

        try {
            
            
            //This producer. 
            RabbitConfig config = new RabbitConfig("TestQueue", "guest", "guest");
            Thread tProducer = new Thread(new ProducerTest1(config));
            tProducer.start();
            
            
            Thread.sleep(5000);
            

            //Start Consumer. 
            RabbitProducer producer2 = new RabbitProducer(config);
            ConsumerTest1 a = new ConsumerTest1(config, 0, producer2, 0, 23);
            Thread tConsumer = new Thread(a);
            tConsumer.start();
            Thread.sleep(10000);

//es.shutdownNow();
            System.out.println("S T O P I N G");
        } catch (InterruptedException ex) {
            System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEX" + ex);
        }

    }
}
