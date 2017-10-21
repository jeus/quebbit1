package com.jeus.server.rabbit.common;

import com.jeus.server.rabbit.common.consumer.ConsumerTest1;
import com.jeus.server.rabbit.common.producer.ProducerTest1;
import com.jeus.server.rabbit.common.quebbit.RabbitConfig;
import com.jeus.server.rabbit.common.quebbit.RabbitProducer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Runner {

    public static void main(String[] arg) {

        try {
            //This producer.
            RabbitConfig config = new RabbitConfig("TestQueue", "guest", "guest");
            Thread tProducer = new Thread(new ProducerTest1(config));
            tProducer.start();
            Thread.sleep(3000);
            //Start Consumer.
            
            RabbitProducer producer2 = new RabbitProducer(config);
            ConsumerTest1 a = new ConsumerTest1(config, 3, producer2, 10, 13);
            Thread tConsumer = new Thread(a);
            tConsumer.start();
            Thread.sleep(10000);
            //es.shutdownNow();
            System.out.println("S T O P I N G");
        } catch (InterruptedException ex) {
            Logger.getLogger(Runner.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
