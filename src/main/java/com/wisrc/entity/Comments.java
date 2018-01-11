package com.wisrc.entity;

public class Comments {
    private String key;
    private String value;

    public Comments(String key,String value) {
        this.key = key;
        this.value = value;
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

    @Override
    public String toString() {
        return "Comments{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
