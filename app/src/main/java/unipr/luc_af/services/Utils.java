package unipr.luc_af.services;

import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

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
}
