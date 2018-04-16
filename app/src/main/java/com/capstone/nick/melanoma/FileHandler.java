package com.capstone.nick.melanoma;


import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A File I/O class that writes Strings to files, and reads files into Strings. Currently used for
 * storing profile.txt for each user.
 */
public class FileHandler {
    final String TAG = FileHandler.class.getName();

    /**
     * Reads the selected file in the selected directory into a String. Uses Stringbuilder to
     * combine file contents.
     * @param path directory of file chosen
     * @param fileName name of file chosen
     * @return String of file contents
     */
    public  String readFile(String path, String fileName){
        String line;
        String output = "";

        try {
            FileInputStream fileInputStream = new FileInputStream (new File(path +"/"+ fileName));
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            //read file and create SB of contents
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
        //return the contents of file as string
        return output;
    }

    /**
     * Writes data into the selected file at the selected directory. Data is passed in as String and
     * then written to output file.
     * @param path directory of file chosen
     * @param fileName name of file chosen
     * @param data String data to be written to file
     * @return boolean of success/failure to save file
     */
    public boolean saveToFile(String path, String fileName, String data){
        try {
            new File(path).mkdir();
            File file = new File(path + fileName);
            if(!file.exists()) {
                file.createNewFile();
            }
            //write the input string to file
            FileOutputStream outputStream = new FileOutputStream(file,false);
            outputStream.write(data.getBytes());

            outputStream.close();

            //completed successfully
            return true;
            
        }  catch(FileNotFoundException e) {
            Log.d(TAG, e.getMessage());
        }  catch(IOException e) {
            Log.d(TAG, e.getMessage());
        }
        //error completing save
        return  false;

    }

}
