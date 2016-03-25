/*
 * From JBoss, Apache License Apache License, Version 2.0
 */
package com.examples.with.different.packagename.jee.injection.wildfly;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity(name="KVPair_0")
@Table(name = "BMT_KVPair")
public class KVPair implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(unique = true)
    private String key;

    @Column
    private String value;

    public KVPair() {
    }

    public KVPair(String key, String value) {
        setKey(key);
        setValue(value);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString() {
        return key + "=" + value;
    }
}
