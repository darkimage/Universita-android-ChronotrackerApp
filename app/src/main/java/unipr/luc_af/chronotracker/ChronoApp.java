package unipr.luc_af.chronotracker;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class ChronoApp extends Application {
    private static Context mContext;
    public static final String CHANNEL_ID = "Chrono_Service_Channel";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
        mContext = this;
    }

    private void createNotificationChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    "Chrono tracking service channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    public static Context getContext(){
        return mContext;
    }
}
