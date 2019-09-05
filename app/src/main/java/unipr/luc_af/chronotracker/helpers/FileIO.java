package unipr.luc_af.chronotracker.helpers;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileIO {
    private static FileIO instance = null;

    private FileIO() {
    }

    public static FileIO getInstance() {
        if (instance == null)
            instance = new FileIO();
        return instance;
    }

    public String ReadFileFromAssets(String name, Context context) throws IOException {
        String res = "";
        String line;
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(context.getAssets().open(name)));
        while ((line = fileReader.readLine()) != null) {
            res += line;
        }
        return res;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private interface OnWriteFileListener {
        void OnAfterWrite();

        void OnWriteError(Exception err);
    }

    private void writeToFile(String name, String data, OnWriteFileListener writeFileListener) {
        try {
            File documentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(documentDir, name);
            if (isExternalStorageWritable()) {
                FileWriter fileWriter = new FileWriter(file);
                fileWriter.write(data);
                fileWriter.flush();
                writeFileListener.OnAfterWrite();
            } else {
                throw new Exception("Cannot write to External Storage");
            }
        } catch (Exception e) {
            writeFileListener.OnWriteError(e);
        }
    }
}
