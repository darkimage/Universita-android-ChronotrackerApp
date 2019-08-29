package unipr.luc_af.chronotracker.helpers;

import android.content.Context;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class FileIO {
    private static FileIO instance = null;
    private FileIO () {}

    public static FileIO getInstance() {
        if(instance == null)
            instance = new FileIO();
        return instance;
    }

    public String ReadFileFromAssets(String name, Context context) throws IOException {
        String res = "";
        String line;
        BufferedReader fileReader = new BufferedReader( new InputStreamReader(context.getAssets().open(name)));
        while ((line = fileReader.readLine()) != null) {
            res += line;
        }
        return res;
    }
}
