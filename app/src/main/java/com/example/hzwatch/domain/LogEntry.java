package com.example.hzwatch.domain;

import java.util.Date;

public class LogEntry extends Entity {
    private String msg;
    private Date at;

    public LogEntry() {
    }

    public Date getAt() {
        return at;
    }

    public void setAt(Date at) {
        this.at = at;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}