package com.jeus.server.rabbit.common.quebbit;

/**
 * Created by milad on 8/19/17.
 */
import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Logger;

/**
 * Represents a connection with a queue
 *
 * @author syntx
 */
public abstract class RabbitDao {

    protected final String endPointName;
    protected final boolean batch;
    protected final int batchSize;
    protected Channel channel;
    protected Connection connection;
    protected ConnectionFactory factory;
    String host = "localhost";
    int port = 5672;
    String username = "guest";
    String password = "guest";
    int rabbitQOS = 1;

    public RabbitDao(RabbitConfig rabbitConfig) {
        this.endPointName = rabbitConfig.getEndPointName();

        host = rabbitConfig.getHost();
        port = rabbitConfig.getPort();
        username = rabbitConfig.getUsername();
        password = rabbitConfig.getPassword();
        batch = rabbitConfig.isBatch();
        batchSize = rabbitConfig.getBatchSize();
        //Create a connection factory
        factory = new ConnectionFactory();

        //hostname of your rabbitmq server
        //set Rabbitmq connection settings
        factory.setHost(host);
        factory.setPort(port);
        factory.setAutomaticRecoveryEnabled(false);

        factory.setUsername(username);
        factory.setPassword(password);

        //getting a connection
        try {
            connection = factory.newConnection();
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
            System.out.println(">>GETEXCEPTION " + e.getMessage());
        }

        //creating a channel
        try {
            channel = connection.createChannel();
        } catch (IOException e) {
            System.out.println(">>GETEXCEPTION " + e.getMessage());
        }

        //declaring a queue for this channel. If queue does not exist,
        //it will be created on the server.
        Map<String, Object> args = new HashMap<String, Object>();

        // Define sets of priority
        args.put("x-max-priority", 7);

        try {
            channel.queueDeclare(this.endPointName, true, false, false, args);
            channel.basicQos(rabbitQOS,false);

        } catch (IOException e) {
            System.out.println(">>GETEXCEPTION " + e.getMessage());
        }
    }

 
    
    /**
     * Close channel and connection. Not necessary as it happens implicitly any
     * way.
     */
    public void close() {

        try {
            System.out.println(">>CLOSING ");
            this.channel.close();
        } catch (IOException e) {
            System.out.println(">>GETEXCEPTION " + e.getMessage());
        } catch (TimeoutException e) {
            System.out.println(">>GETEXCEPTION " + e.getMessage());
        }

        try {
            System.out.println(">>CONNECTION CLOSING ");
            this.connection.close();
        } catch (IOException e) {
            System.out.println(">>GETEXCEPTION " + e.getMessage());
        }
    }
}
