package com.tempus_indicium.datagather;

import com.tempus_indicium.datagather.config.ConfigManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

/**
 * Created by peterzen on 2017-02-04.
 * Part of the datagather project.
 */
public class App {

    private static ServerSocket serverSocket;
    public final static Properties config = ConfigManager.getProperties("config.properties");

    public static void main(String[] args) {
        App.setupFileStore();

        App.setupServerSocket(Integer.parseInt(config.getProperty("SERVER_PORT")));

        Socket client = null;
        try {
            System.out.println("now waiting for the datafilter client to connect..");
            client = App.serverSocket.accept();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (client != null) {
            System.out.println("client connected, continueing to storage of the InputStream");
            try {
                FileStore.storeInputStream(client.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void setupServerSocket(int port) {
        try {
            App.serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupFileStore() {
        if(!FileStore.initializeFileStore()) {
            System.out.println("File store could not be initialized.");
            System.exit(1);
        }

        try {
            FileStore.fileOutputStream = new FileOutputStream(FileStore.currentFile, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
