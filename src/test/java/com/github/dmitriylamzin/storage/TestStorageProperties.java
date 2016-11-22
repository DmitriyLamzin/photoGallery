package com.github.dmitriylamzin.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("testStorage")
public class TestStorageProperties extends StorageProperties {
    /**
     * Folder location for storing files
     */
    private String location = "src/test/data";


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}


