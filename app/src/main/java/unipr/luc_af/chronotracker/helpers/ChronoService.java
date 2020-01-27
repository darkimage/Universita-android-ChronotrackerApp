package unipr.luc_af.chronotracker.helpers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import unipr.luc_af.chronotracker.ChronoApp;
import unipr.luc_af.chronotracker.MainActivity;
import unipr.luc_af.chronotracker.R;
import unipr.luc_af.components.ChronoView;

import static unipr.luc_af.chronotracker.ChronoApp.CHANNEL_ID;

public class ChronoService extends Service {
    public final static String TIME_STEP_EXTRA = "time_step_extra";
    public final static String TEXT_SMALL_EXTRA = "text_small_extra";
    public final static String TEXT_TITLE_EXTRA = "text_title_extra";
    public final static String TEXT_DURATION_EXTRA = "text_duration_extra";
    public final static String TEXT_CURRENT_EXTRA = "text_current_extra";
    public final static String TEXT_NO_LAPS_EXTRA = "text_no_laps_extra";
    private Timer mTimer;
    private Handler mHandler;
    private final IBinder mChronoBinder = new ChronoService.ChronoBinder();
    private ChronoData chronoData;
    private TimerTask mTask;
    private long timeStep;
    private ChronoService.OnTickListener tickListener = (a, b) -> {
    };
    private ChronoService.OnTaskRemoved taskRemovedListener = () -> {
    };

    public class ChronoBinder extends Binder {
        public ChronoService getService() {
            return ChronoService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        chronoData = new ChronoData();
        mTimer = new Timer();
        mHandler = new Handler();
        mTask = Task();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timeStep = intent.getLongExtra(TIME_STEP_EXTRA, 10);
        startForeground(1, buildNotification(intent));
        updateNotification();
        return START_NOT_STICKY;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        taskRemovedListener.OnRemove();
    }

    private Notification buildNotification(Intent intent) {
        String textSmall = intent.getStringExtra(TEXT_SMALL_EXTRA);
        String textTitle = intent.getStringExtra(TEXT_TITLE_EXTRA);
        String textDuration = intent.getStringExtra(TEXT_DURATION_EXTRA);
        String textCurrent = intent.getStringExtra(TEXT_CURRENT_EXTRA);
        String textNoLaps = intent.getStringExtra(TEXT_NO_LAPS_EXTRA);

        RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_layout);
        notificationLayout.setTextViewText(R.id.notification_text, textSmall);
        notificationLayout.setTextViewText(R.id.notification_text_title, textTitle);

        RemoteViews notificationLayoutBig = new RemoteViews(getPackageName(), R.layout.notification_layout_expanded);
        notificationLayoutBig.setTextViewText(R.id.notification_text_title, textTitle);
        notificationLayoutBig.setTextViewText(R.id.notification_duration_item_text, textDuration);
        notificationLayoutBig.setTextViewText(R.id.notification_current_item_text, textCurrent);
        notificationLayoutBig.setTextViewText(R.id.notification_no_laps, textNoLaps);

        Intent mainActivity = new Intent(this, MainActivity.class).addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Chrono Training")
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayoutBig)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_stopwatch_solid)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .addAction(R.drawable.ic_play_solid, ChronoApp.getContext().getString(R.string.notification_action_goto), pendingIntent)
                .build();
        return notification;
    }

    static String formatTime(long millisec, boolean useMillisec) {
        long hours = TimeUnit.MILLISECONDS.toHours(millisec);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisec) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisec) % 60;
        if (!useMillisec) {
            if (hours == 0) {
                return String.format("%02d:%02d", minutes, seconds);
            } else {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
        } else {
            if (hours == 0) {
                return String.format("%02d:%02d.%03d", minutes, seconds, millisec % 1000);
            } else {
                return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millisec % 1000);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mChronoBinder;
    }

    public ChronoData getData() {
        return chronoData;
    }

    public void ExecuteTask() {
        ExecuteTask(() -> {
        });
    }

    public void ExecuteTask(ChronoService.ActionTask task) {
        long current = getCurrentTime();
        if (chronoData.startTime == null) {
            chronoData.lastLapTime = current;
            chronoData.initialTime = current;
        }
        chronoData.startTime = current;
        chronoData.state = ChronoData.State.TRACK;
        task.runTask();
        mTimer.scheduleAtFixedRate(mTask, 0, timeStep);
        updateNotification();
    }

    public void LapTask(ChronoService.ActionTask task) {
        if (chronoData.state != ChronoData.State.PAUSE) {
            Long current = getCurrentTime();
            long lapTime = current - chronoData.lastLapTime;
            chronoData.lastLapTime = current;
            chronoData.lastLapDuration = lapTime;
            task.runTask();
        }
        updateNotification();
    }

    public void PauseTask(ChronoService.ActionTask task) {
        if (chronoData.state != ChronoData.State.RESET) {
            chronoData.state = ChronoData.State.PAUSE;
            mTimer.cancel();
            mTimer = new Timer();
            mTask = Task();
            chronoData.lastPausedTime = getCurrentTime();
            chronoData.partialElapsed += chronoData.lastPausedTime - chronoData.startTime;
            task.runTask();
        }
        updateNotification();
    }

    public void ResetTask(ChronoService.ActionTask task) {
        PauseTask(() -> {
        });
        chronoData.state = ChronoData.State.RESET;
        chronoData.startTime = null;
        chronoData.lastPausedTime = null;
        chronoData.lastLapTime = null;
        chronoData.lastLapDuration = (long) 0;
        chronoData.partialElapsed = (long) 0;
        chronoData.theta = (double) 0;
        task.runTask();
        updateNotification();
    }

    public long getCurrentTaskElapsed() {
        long current = getCurrentTime() - chronoData.startTime;
        current += chronoData.partialElapsed;
        return current;
    }

    private TimerTask Task() {
        return new TimerTask() {
            @Override
            public void run() {
                mHandler.post(() -> {
                    if (chronoData.state != ChronoData.State.PAUSE &&
                            chronoData.state != ChronoData.State.RESET) {
                        double angle360 = Math.toRadians(360);
                        double step = (60000f / timeStep);
                        chronoData.theta += angle360 / step;
                        tickListener.onTick(getCurrentTaskElapsed(), chronoData.lastLapDuration);
                    }
                });
            }
        };
    }

    public void updateNotification() {
        String textSmall;
        Context context = ChronoApp.getContext();
        ChronoData.State state = chronoData.state;
        switch (state) {
            case PAUSE:
                textSmall = context.getString(
                        R.string.notification_text_title,
                        context.getString(R.string.chrono_state_pause));
                break;
            case RESET:
                textSmall = context.getString(
                        R.string.notification_text_title,
                        context.getString(R.string.chrono_state_reset));
                break;
            default:
                textSmall = context.getString(
                        R.string.notification_text_title,
                        context.getString(R.string.chrono_state_tracking));
                break;
        }

        Intent updateIntent = new Intent(this, ChronoService.class);
        updateIntent.putExtra(TEXT_TITLE_EXTRA, textSmall);
        updateIntent.putExtra(TEXT_SMALL_EXTRA, context.getString(R.string.chrono_state_touch_message));
        if (chronoData.lastLapDuration != 0) {
            updateIntent.putExtra(TEXT_DURATION_EXTRA, context.getString(R.string.duration_placeholder, formatTime(chronoData.lastLapDuration, true)));
            updateIntent.putExtra(TEXT_CURRENT_EXTRA, formatTime(getCurrentTaskElapsed(), true));
        } else {
            updateIntent.putExtra(TEXT_NO_LAPS_EXTRA, context.getString(R.string.no_laps));
        }
        Notification notification = buildNotification(updateIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    private long getCurrentTime() {
        return Calendar.getInstance().getTime().getTime();
    }

    public interface ActionTask {
        void runTask();
    }

    public void setOnTickListener(ChronoService.OnTickListener listener) {
        tickListener = listener;
    }

    public void setOnTaskRemoved(ChronoService.OnTaskRemoved listener) {
        taskRemovedListener = listener;
    }

    public interface OnTickListener {
        void onTick(long duration, long lap);
    }

    public interface OnTaskRemoved {
        void OnRemove();
    }

    static public class ChronoData implements Parcelable {
        public Long initialTime;
        public Long startTime;
        public Double theta = (double) 0;
        public Long lastLapTime;
        public Long lastLapDuration = (long) 0;
        public Long lastPausedTime;
        public Long partialElapsed = (long) 0;
        public State state = State.RESET;

        public enum State {
            TRACK,
            PAUSE,
            RESET
        }

        public ChronoData() {
        }

        public ChronoData(Parcel parcel) {
            this();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {

        }

        public static final Creator<ChronoData> CREATOR = new Creator<ChronoData>() {
            @Override
            public ChronoData createFromParcel(Parcel parcel) {
                return new ChronoData(parcel);
            }

            @Override
            public ChronoData[] newArray(int i) {
                return new ChronoData[i];
            }
        };
    }

}