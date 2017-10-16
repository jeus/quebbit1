package com.jeus.server.rabbit.common.quebbit;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import org.apache.log4j.Logger;

public abstract class RabbitConsumer extends RabbitDao implements Runnable, Consumer {

    private static final Logger LOGGER = Logger.getLogger(RabbitConsumer.class);

    private RabbitConfig config;
    volatile boolean stopped = false;
    protected volatile boolean doConsume = true;
    volatile int startTime;
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
                consumerTag = channel.basicConsume(endPointName, true, this);
                LOGGER.info(">>>>>>>>>" + consumerTag + "<<<<<<<<<");
//                }
            } catch (IOException e) {
                LOGGER.error("RabbitConsumer Problem[endpoint=" + endPointName + "] : " + e.getMessage());
            }
        } else {
            try {
                QueueingConsumer consumer = new QueueingConsumer(channel);
                channel.basicConsume(endPointName, false, consumer);
                for (int counter = 0; counter < batchSize; counter++) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery(1000);
//          consumer.
                    if (delivery == null) {
                        break;
                    }
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    LOGGER.info(new String(delivery.getBody(), "UTF-8"));
                    messages.add(new String(delivery.getBody(), "UTF-8"));
                }
                consume(messages);

            } catch (IOException e) {
                LOGGER.error(
                        "RabbitConsumer Batch Problem[endpoint=" + endPointName + "] : " + e.getMessage());
            } catch (InterruptedException e) {
                LOGGER.error(
                        "RabbitConsumer Batch Interrupted[endpoint=" + endPointName + "] :" + e.getMessage());
            }
        }
//        while (true) {
//            try {
//                LOGGER.info(">>CHECK FOR PAUSE");
//                Thread.sleep(5000);
//                checkForPause();
//
//            } catch (InterruptedException ex) {
//                java.util.logging.Logger.getLogger(RabbitConsumer.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//        }
    }

    private void autoStartStop() {

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
        LOGGER.info("|||||||||||HANDLE_ShutdownSignal " + consumerTag + "||||||||||||||");
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
        LOGGER.info("|||||||||||HANDLE_RecoverOk " + consumerTag + "||||||||||||||");
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body) throws IOException {
        String routingKey = envelope.getRoutingKey();
        String contentType = properties.getContentType();
        long deliveryTag = envelope.getDeliveryTag();
        LOGGER.info("|||||||||||HANLDE_DELIVERY " + consumerTag + "||||||||||||||");
        consume(body);
        LOGGER.info("ROUTING_KEY:" + routingKey + " ContentType:" + contentType + " DELIVERYTAG:" + deliveryTag);

        //channel.basicAck(deliveryTag, false);
    }

    public boolean timeForConsume() {
        final Calendar now = Calendar.getInstance();
        int hourOfDay = now.get(Calendar.MINUTE) % 2; //TODO: remove this line 
        //int hourOfDay = now.get(Calendar.HOUR_OF_DAY); //TODO: have to uncomment 
        return hourOfDay == 0;
        //return (hourOfDay >= startTime || hourOfDay < endTime);//TODO: have to uncommnt
    }

    public abstract void consume(byte[] data) throws IOException;

    public abstract void consume(List<String> data) throws IOException;

    public void pause() {
        try {
            LOGGER.info("<<<<<<<<<<<<<<<<<<<<<<<<" + consumerTag + ">>>>>>>>>>>>>>>>>>>>>>>>>>");
            LOGGER.info("STATUS:pause DETAIL:pause queue consumee.");
            channel.basicCancel(consumerTag);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(RabbitConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void checkForPause() {
        if (doConsume != timeForConsume()) {
            doConsume = !doConsume;
            LOGGER.info("METHOD:rabbitConsumer STATUS:run TIME:" + startTime + " DOCONSUME:" + doConsume);
        }
        LOGGER.info(">>>>CHANNEL IS " + (channel.isOpen() ? "OPEN" : "CLOSE ") + " FLOWBLOCKED:" + channel.flowBlocked());
        if (!doConsume && channel.flowBlocked()) {
            System.out.println(">>>>CHANNEL IS OPEN :");
            this.pause();
        }
    }

    public void resume() {
//            System.out.println("=================A=================BASIC RECOVER " + endPointName);
//            connection = factory.newConnection();
//            channel = connection.createChannel();
//            Map<String, Object> args = new HashMap<String, Object>();
//            args.put("x-max-priority", 7);
//            channel.queueDeclare(this.endPointName, true, false, false, args);
//            channel.basicQos(50);
//            System.out.println("----------------> this start");

        

//channel.basicRecover();
//channel = null;
//            consumerTag = channel.basicConsume(endPointName, true, this);
//channel.basicRecover();
    }

    public void stop() {
        try {
            System.out.println("=============================>> CALL STOP");
            doConsume = false;
            connection.close();
            consumer.stop();
//            channel.abort();
//            channel.basicCancel(consumerTag);
//            channel.close();
            System.out.println("CLOSED-----------");
//            Thread.sleep(30000);
//            channel.basicRecover();
//            LOGGER.info(">>>>CHANNEL IS " + (channel.isOpen() ? "OPEN" : "CLOSE ") + " FLOWBLOCKED:" + channel.flowBlocked());
//            LOGGER.info(">>>>>>>>>" + consumerTag + "<<<<<<<<<");
//            System.out.println("=================B=================BASIC RECOVER");

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(RabbitConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
