package com.jeus.server.rabbit.common.quebbit;

/**
 * configuration of connectivity and specification of producer and consumer.
 */
public class RabbitConfig {

  private String  endPointName; //name of queue
  private String  host;
  private int     port;
  private String  username;
  private String  password;
  private String  priority;
  private boolean batch = false;
  private int     batchSize = 1; // count of batch

  public RabbitConfig(String endPointName, String username, String password) {
    this.endPointName = endPointName;

    this.host = "localhost";
    this.port = 5672;

    this.username = username;
    this.password = password;
  }


  public RabbitConfig(String endPointName, String host, int port, String username, String password,String priority) {
    this.endPointName = endPointName;

    this.host = host;
    this.port = port;

    this.username = username;
    this.password = password;
    this.priority = priority;
  }

  public RabbitConfig(String endPointName, String host, int port, String username,String password, String priority, boolean batch, int batchSize) {
    this.endPointName = endPointName;
    this.host = host;
    this.port = port;
    this.username = username;
    this.password = password;
    this.priority = priority;
    this.batch = batch;
    this.batchSize = batchSize;
  }

  public RabbitConfig(String endPointName, String host, int port, String username,String password) {
    this.endPointName = endPointName;

    this.host = host;
    this.port = port;

    this.username = username;
    this.password = password;
  }

  public RabbitConfig(String endPointName, String username, String password, boolean batch,int batchSize) {
    this.endPointName = endPointName;

    this.host = "localhost";
    this.port = 5672;

    this.username = username;
    this.password = password;

    this.batch = batch;
    this.batchSize = batchSize;
  }

  public RabbitConfig(String endPointName, String username, String password, String priority) {
    this.endPointName = endPointName;

    this.host = "localhost";
    this.port = 5672;

    this.username = username;
    this.password = password;

    this.priority = priority;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public boolean isBatch() {
    return batch;
  }

  public void setBatch(boolean batch) {
    this.batch = batch;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(int batchSize) {
    this.batchSize = batchSize;
  }

  public String getEndPointName() {
    return endPointName;
  }

  public void setEndPointName(String endPointName) {
    this.endPointName = endPointName;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "EndPointName=" + this.endPointName + ";Host=" + this.host + ";Port=" + this.port
        + ";Username=" + this.username + ";Password=" + this.password
        + ";batch=" + this.batch + ";batchSize=" + this.batchSize;
  }
}
