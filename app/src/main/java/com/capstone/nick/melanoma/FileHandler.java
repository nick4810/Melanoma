package com.capstone.nick.melanoma;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class FileHandler {
    final static String TAG = FileHandler.class.getName();

    public static String readFile(String path, String fileName){
        String line;
        String output = "";

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(path + fileName));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            while((line = bufferedReader.readLine()) !=null) {
                stringBuilder.append(line + System.getProperty("line.separator"));
            }
            fileInputStream.close();
            output = stringBuilder.toString();

            bufferedReader.close();
        }
        catch(FileNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }
        catch(IOException e) {
            Log.d(TAG, e.getMessage());
        }
        return output;
    }

    public static boolean saveToFile(String path, String fileName, String data){
        try {
            new File(path).mkdir();
            File file = new File(path + fileName);
            if(!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(file,true);
            outputStream.write(data.getBytes());

            outputStream.close();

            return true;
            
        }  catch(FileNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }  catch(IOException e) {
            Log.d(TAG, e.getMessage());
        }
        return  false;

    }

}
