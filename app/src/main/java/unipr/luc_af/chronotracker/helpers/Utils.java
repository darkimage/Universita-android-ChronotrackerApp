package unipr.luc_af.chronotracker.helpers;

import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Utils {
    private static Utils instance = null;
    private Utils () { }

    public static Utils getInstance() {
        if(instance == null)
            instance = new Utils();
        return instance;
    }

    public void executeWithDelay(int delay, Utils.DelayedTask delayedtask){
        AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(delay);
                }catch (InterruptedException ext){
                    System.out.println(ext.getMessage());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void voided) {
                delayedtask.AfterDelay();
            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public interface DelayedTask{
        void AfterDelay();
    }

    public String concatString(String delimiter, String ... strings){
        String res = "";
        for (int i = 0; i < strings.length; i++) {
            res += strings[i];
            if(i < strings.length-1){
                res += delimiter;
            }
        }
        return res;
    }

    public void setToolBarNavigation(AppCompatActivity fragment){
        if(fragment.getSupportFragmentManager().getBackStackEntryCount() != 0) {
            fragment.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            fragment.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }else{
            fragment.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            fragment.getSupportActionBar().setDisplayShowHomeEnabled(false);
        }
    }

    public String formatTime(long millisec, boolean useMillisec){
        long hours = TimeUnit.MILLISECONDS.toHours(millisec);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisec) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisec) % 60;
        if(!useMillisec) {
            if (hours == 0) {
                return String.format("%02d:%02d", minutes, seconds);
            } else {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
        }else{
            if (hours == 0) {
                return String.format("%02d:%02d.%03d", minutes, seconds, millisec % 1000);
            } else {
                return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millisec % 1000);
            }
        }
    }

    public String formatDate(long time, String format){
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(time);
        SimpleDateFormat formatStr = new SimpleDateFormat(format, Locale.getDefault());
        return formatStr.format(date.getTime());
    }
}
