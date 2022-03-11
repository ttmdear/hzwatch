package com.example.hzwatch.domain;

import java.util.Date;

public class WatcherAlarm extends Entity {
    private Date planedAt;

    public WatcherAlarm() {

    }

    public Date getPlanedAt() {
        return planedAt;
    }

    public void setPlanedAt(Date planedAt) {
        this.planedAt = planedAt;
        notifyChange();
    }
}
