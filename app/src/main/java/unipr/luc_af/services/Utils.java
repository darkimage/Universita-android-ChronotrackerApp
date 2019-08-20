package unipr.luc_af.services;

import android.os.AsyncTask;

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
}
