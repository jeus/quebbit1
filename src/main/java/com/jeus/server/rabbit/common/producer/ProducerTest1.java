/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jeus.server.rabbit.common.producer;

import com.jeus.server.rabbit.common.quebbit.RabbitConfig;
import com.jeus.server.rabbit.common.quebbit.RabbitProducer;
import java.nio.charset.Charset;

public class ProducerTest1 implements Runnable {

    final RabbitConfig config;
    final RabbitProducer producer;

    public ProducerTest1(RabbitConfig config) {
        this.config = config;
        producer = new RabbitProducer(config);
    }

    @Override
    public void run() {
        int pro = 10;
        for (int i = 0; i < pro; i++) {
            String msg = "*" + i + "*";
            byte[] bytes = msg.getBytes(Charset.forName("UTF-8"));
            producer.submit(bytes);
            System.out.println(">>PRODUCE " + msg);
        }

        for (int i = 0; i < pro; i++) {
            String msg1 = "*" + i + "*";
            byte[] bytes1 = msg1.getBytes(Charset.forName("UTF-8"));
            producer.submit(bytes1);
            System.out.println(">>PRODUCE " + msg1);
        }
    }
}
