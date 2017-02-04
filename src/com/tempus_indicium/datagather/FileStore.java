package com.tempus_indicium.datagather;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by peterzen on 2017-02-04.
 * Part of the datagather project.
 */
public class FileStore {
    public static Date currentDate;
    public static File currentFile;
    public static FileOutputStream fileOutputStream;


    public static void storeInputStream(InputStream filteredDataStream) throws IOException {
        System.out.println("you reached the storeInputStream method");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[15600];
        int byteCounter = 0;
        byte[] secondMark = new byte[]{ (byte) 0xFF, (byte) 0xFF };

        while ((nRead = filteredDataStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
            if (byteCounter >= 15600) {
                buffer.write(secondMark); // mark the start of a new second
                byteCounter = 0;
            }
            buffer.flush();

            byte[] bufferBytes = buffer.toByteArray();
            byteCounter += bufferBytes.length;

            FileStore.writeToFile(bufferBytes);

            buffer.reset();
        }
    }

    private static synchronized void updateDateUpdateFileIfNeeded() {
        if (FileStore.dateComparedToCurrent() == 0) {
            return;
        }

        // dates are not identical anymore; update the date
        FileStore.updateCurrentDate();

        // date updated so: create new current file
        String fileName = FileStore.generateFileName(FileStore.currentDate);
        FileStore.newCurrentFile(fileName);

        // update output stream
        try {
            FileStore.fileOutputStream.close();
            FileStore.fileOutputStream = new FileOutputStream(FileStore.currentFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean initializeFileStore() {
        try {
            FileStore.updateCurrentDate();

            List<Path> existingFilePaths = FileStore.filesForFolder();
            String fileName = generateFileName(currentDate); // fileName for today's date

            if (FileStore.needToCreateFile(existingFilePaths, fileName)) {
                // create a file for today
                FileStore.newCurrentFile(fileName);
            } else {
                existingFilePaths.forEach(filePath -> {
                    if (filePath.getFileName().toString().equals(fileName)) {
                        FileStore.currentFile = new File(filePath.toString());
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void updateCurrentDate() {
        FileStore.currentDate = new Date();
    }

    // check if current date is equal to the variable FileStore.currentDate
    private static int dateComparedToCurrent() {
        // @TODO: properly implement this function, do not use deprecated methods.
        // its almost 1am and i cba to figure this Java jibber dabber out.
        Date d1 = new Date();
        Date d2 = FileStore.currentDate;
        if (d1.getYear() != d2.getYear())
            return d1.getYear() - d2.getYear();
        if (d1.getMonth() != d2.getMonth())
            return d1.getMonth() - d2.getMonth();
        if (d1.getHours() != d2.getHours())
            return d1.getHours() - d2.getHours();
        return d1.getDate() - d2.getDate();
    }

    private static void newCurrentFile(String fileName) {
        File file = new File(App.config.getProperty("FILE_STORE_DIR") + fileName);

        try {
            if (file.createNewFile()) {
                FileStore.currentFile = file;
            } else {
                System.out.println("File already exists. But error should be given earlier..");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Path> filesForFolder() throws IOException {
        List<Path> filePaths = new LinkedList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(App.config.getProperty("FILE_STORE_DIR")))) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    filePaths.add(filePath);
                    System.out.println("fileExists: " + filePath.toString());
                }
            });
        }
        return filePaths;
    }

    private static String generateFileName(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(App.config.getProperty("FILE_DATE_FORMAT"));
        return dateFormat.format(date);
    }

    private static boolean needToCreateFile(List<Path> existingFilePaths, String fileNameToday) {
        final boolean[] fileMissing = {true};
        existingFilePaths.forEach(filePath -> {
            if (filePath.getFileName().toString().equals(fileNameToday)) {
                fileMissing[0] = false;
            }
        });
        return fileMissing[0];
    }

    public static synchronized void writeToFile(byte[] bytes) {
        System.out.println("PERFORMING FILE WRITE, bytes: "+bytes.length);
        try {
            FileStore.fileOutputStream.write(bytes); // NOTE: encoding might cause reading fails
            FileStore.fileOutputStream.flush(); // zou hier nog wat kunnen zijn
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileStore.updateDateUpdateFileIfNeeded();
    }
}
