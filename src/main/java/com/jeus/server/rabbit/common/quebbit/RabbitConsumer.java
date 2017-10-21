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
                consumerTag = channel.basicConsume(endPointName, false, this);
            } catch (IOException e) {
                LOGGER.error("RabbitConsumer Problem[endpoint=" + endPointName + "] : " + e.getMessage());
            }
        }
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
        LOGGER.info("STATUS:consume_ok CONSTAG:" + consumerTag);
    }

    @Override
    public void handleCancelOk(String consumerTag) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        deliveryTag = envelope.getDeliveryTag();
        if (timeConsume() && doConsume) {
            System.out.print("...");
            consume(body);
            channel.basicAck(deliveryTag, false);
            System.out.println("<<<|" + (new String(body, "UTF-8")) + "|");
        } else {
            channel.basicRecover();
            waitForCondition();
        }
    }

    public boolean timeConsume() {
        final Calendar now = Calendar.getInstance();
        int hourOfDay = now.get(Calendar.HOUR_OF_DAY);
        return (hourOfDay >= startTime && hourOfDay < endTime);
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
            LOGGER.info("[S] START CONSUMING ");
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
            LOGGER.error("error in ack channel " + ex.getMessage());
        }
    }

}
