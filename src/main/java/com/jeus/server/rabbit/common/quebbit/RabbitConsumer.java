package com.jeus.server.rabbit.common.quebbit;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.apache.log4j.Logger;

public abstract class RabbitConsumer extends RabbitDao implements Runnable, Consumer {

    private static final Logger LOGGER = Logger.getLogger(RabbitConsumer.class);

    List<Object> msgList = new ArrayList<>();
    private RabbitConfig config;
    volatile boolean stopped = false;
    protected volatile boolean doConsume = true;
    volatile int startTime;
    volatile long deliveryTag;
    volatile int endTime;
    volatile String consumerTag;
    RabbitConsumer consumer;
    private AtomicInteger count = new AtomicInteger();
    private AtomicInteger _consumersPerThread = new AtomicInteger();

    public RabbitConsumer(RabbitConfig config, int workersCount, int consumersPerThread) {
        super(config);
        this.config = config;
        LOGGER.info(config.getEndPointName() + " Workers count:" + workersCount);
        LOGGER.info(config.getEndPointName() + " ConsumersPerThread count:" + consumersPerThread);
        this._consumersPerThread.set(consumersPerThread);

        for (int workerId = 0; workerId < workersCount; workerId++) {
            LOGGER.info(config.getEndPointName() + " Workers Id run" + workerId);
            new Thread(this).start();
        }
    }

    public RabbitConsumer(RabbitConfig config, int consumersPerThread, int workersCount, int startTime, int endTime) {
        super(config);
        this.startTime = startTime;
        this.endTime = endTime;
        LOGGER.info(config.getEndPointName() + " Workers count" + workersCount);
        LOGGER.info(config.getEndPointName() + " ConsumersPerThread count" + consumersPerThread);
        this._consumersPerThread.set(consumersPerThread);

        for (int workerId = 0; workerId < workersCount; workerId++) {
            LOGGER.info(config.getEndPointName() + " Workers Id run" + workerId);
            new Thread(this).start();
        }
    }

    @Override
    public void run() {
        final List<String> messages = new ArrayList<String>();
        if (!batch) {
            try {
                //TODO: have to returtn if command
//                for (int consumerId = 0; consumerId < _consumersPerThread.get(); ++consumerId) {
                System.out.println("-----------------------------> PER THREAD" + _consumersPerThread.get());
                consumerTag = channel.basicConsume(endPointName, false, this);
                LOGGER.info(">>>>>>>>>" + consumerTag + "<<<<<<<<<");
//                }
            } catch (IOException e) {
                LOGGER.error("RabbitConsumer Problem[endpoint=" + endPointName + "] : " + e.getMessage());
            }
        }
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        LOGGER.info("|||||||||||HANDLE_ConsumeOk" + consumerTag + "|||||||||||");
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        LOGGER.info("|||||||||||HANDLE_CancelOk " + consumerTag + "||||||||||||");
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        LOGGER.info("|||||||||||HANDLE_Cancel " + consumerTag + "||||||||||||");
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        LOGGER.info("|||||||||||HANDLE_ShutdownSignal " + consumerTag + "||||||||||||||???????????????????????????");
        RabbitProducer producer;
        producer = new RabbitProducer(config);
        for (Object object : msgList) {
            String msg = object.toString();
            byte[] bytes = msg.getBytes(Charset.forName("UTF-8"));
            producer.submit(bytes);
            System.out.println(">>PRODUCE " + msg);
        }

    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        LOGGER.info("|||||||||||HANDLE_RecoverOk " + consumerTag + "||||||||||||||");
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        String routingKey = envelope.getRoutingKey();
        String contentType = properties.getContentType();
        deliveryTag = envelope.getDeliveryTag();
        String msg = new String(body, "UTF-8");
        if (timeConsume() && doConsume) {
            System.out.print("...");
            consume(body);
            channel.basicAck(deliveryTag, false);
            System.out.println("<<<|" + (new String(body, "UTF-8")) + "|");
        } else {
            waitForCondition();
        }
    }

    public boolean timeConsume() {
        final Calendar now = Calendar.getInstance();
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY); //TODO: have to uncomment 
        System.out.println("[hourOfDay:" + hourOfDay + "] >= [STARTTIME:" + startTime + "] && [hourOfDay:" + hourOfDay + "] < [ENDTIME:" + endTime + "]");
        return (hourOfDay >= startTime && hourOfDay < endTime);//TODO: have to uncommnt
    }

    public abstract void consume(byte[] data) throws IOException;

    public abstract void consume(List<String> data) throws IOException;

    public void pause() {
        doConsume = false;
    }

    private void waitForCondition() {
        try {
            while (!timeConsume()) {
                Thread.sleep(5000);
            }
            channel.basicAck(deliveryTag, false);
        } catch (InterruptedException | IOException ex) {
            LOGGER.error("error in thread sleep " + ex.getMessage());
        }
    }

    public void resume() {
        try {
            doConsume = true;
            channel.basicAck(deliveryTag, false);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(RabbitConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
