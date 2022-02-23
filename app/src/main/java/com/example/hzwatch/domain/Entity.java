package com.example.hzwatch.domain;

import com.example.hzwatch.service.Storage;
import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class Entity {
    private Integer id;
    private Integer order;

    @JsonIgnore
    private Storage storage;

    public Entity() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
        notifyChange();
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
        notifyChange();
    }

    protected void notifyChange() {
        if (storage != null) {
            storage.notifyChange();
        }
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}
