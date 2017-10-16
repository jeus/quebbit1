package com.jeus.server.rabbit.common.quebbit;

/**
 * priority of messages that insert into queue
 */
public enum Priority {

    LOW(0),
    MEDIUM(1),
    HIGH(2),
    URGENT(3);

    private final int code;

    Priority(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
