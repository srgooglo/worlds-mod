package com.worlds;

import java.io.File;

import com.worlds.config.ConfigurationFile;

public class ModConfigs {
    static File mod_config_dir = new File("config", "worlds");
    static File dimensions_dir = new File(mod_config_dir, "dimensions");

    public static File getModConfigDir() {
        return mod_config_dir;
    }

    public static int initialize() {
        // check if config dir exists
        if (!mod_config_dir.exists()) {
            mod_config_dir.mkdirs();
        }

        if (!dimensions_dir.exists()) {
            dimensions_dir.mkdirs();
        }

        return 0;
    }

    public static ConfigurationFile getWorldConfig(String namespace, String path) {
        // dimensions_dir, namespace + path + ".json";
        String finalDir = dimensions_dir + "/" + namespace;
        String finalPathname = finalDir + "/" + path + ".json";

        if (!new File(finalDir).exists()) {
            new File(finalDir).mkdirs();
        }

        File file = new File(finalPathname);

        ConfigurationFile config = new ConfigurationFile(file);

        return config;
    }
}
