package com.worlds.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;

import com.google.gson.Gson;

import com.worlds.WorldsModInitializer;

public class ConfigurationFile {
    private File destinationFile;
    private Gson gson;
    private LinkedHashMap<String, String> json;

    public ConfigurationFile(File destinationFile) {
        this.destinationFile = destinationFile;
        this.gson = new Gson();

        // if file exist read it
        if (destinationFile.exists()) {
            try {
                String data = new String(Files.readAllBytes(destinationFile.toPath()));

                json = gson.fromJson(data, LinkedHashMap.class);
            } catch (IOException e) {
                WorldsModInitializer.LOGGER.error("Failed to read config file > " + destinationFile);

                e.printStackTrace();
                json = new LinkedHashMap<>();
            }
        }

        if (json == null) {
            json = new LinkedHashMap<>();
        }
    }

    public String set(String key, String value) {
        json.put(key, value);

        return value;
    }

    public String get(String key) {
        return json.get(key);
    }

    public String toString() {
        return json.toString();
    }

    public void save() {
        if (!destinationFile.exists()) {
            try {
                destinationFile.createNewFile();
            } catch (IOException e) {
                WorldsModInitializer.LOGGER.error("Failed to create config file > " + destinationFile);

                e.printStackTrace();
            }
        }

        try {
            WorldsModInitializer.LOGGER.info("Saving config file > " + destinationFile);

            Files.write(destinationFile.toPath(), gson.toJson(json).getBytes());
        } catch (IOException e) {
            e.printStackTrace();

            WorldsModInitializer.LOGGER.error("Failed to save config file > " + destinationFile);
        }
    }
}