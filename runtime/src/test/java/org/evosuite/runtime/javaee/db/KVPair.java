package org.evosuite.runtime.javaee.db;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/*
    Simple example taken from JBoss a tutorial (BMT)
 */


/**
 * JPA Entity for storing key value pairs into a database.
 *
 * @author Mike Musgrove
 */
@Entity
@Table(name = "BMT_KVPair")
public class KVPair implements Serializable {
    /** Default value included to remove warning. **/
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
