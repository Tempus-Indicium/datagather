package com.tempus_indicium.datagather.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by peterzen on 2017-02-04.
 * Part of the datagather project.
 */
public class ConfigManager {

    public static Properties getProperties(String propFileName) {
        InputStream inputStream;
        Properties prop = new Properties();

        try {
            inputStream = ConfigManager.class.getClassLoader().getResourceAsStream(propFileName);

            prop.load(inputStream);
        } catch (IOException e) {
            System.out.println("Cannot read properties file: \n"+e.getMessage());
        }
        return prop;
    }


}