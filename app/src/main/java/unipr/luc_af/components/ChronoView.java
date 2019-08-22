package unipr.luc_af.components;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import unipr.luc_af.chronotracker.R;

public class ChronoView extends LinearLayout {
    private Timer mChronometer;
    private TimerTask mChronoTask;
    private TextView mChronoText;
    private Long mStartTime;
    private Long mLastLap;

    public ChronoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = inflate(context, R.layout.component_chrono_view, this);
        final Handler handler = new Handler();
        mChronometer = new Timer();
        mChronoText = view.findViewById(R.id.chronoview_main_text);
        mChronoTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(() -> updateView());
            }
        };
    }


    public long Start(){
        mChronometer.scheduleAtFixedRate(mChronoTask,0,10);
        mStartTime = getCurrentTime();
        mLastLap = mStartTime;
        return 0;
    }

    public long Pause(){
        mChronometer.cancel();
        return 0;
    }

    public long Lap(){
        return 0;
    }

    public long ElapseFromLastLap(){
        return 0;
    }

    public void Reset(){

    }

    private void updateView(){
        long current = getCurrentTime() - mStartTime;
        String text = String.format("%02d:%02d:%02d:%03d",
            TimeUnit.MILLISECONDS.toHours(current),
            TimeUnit.MILLISECONDS.toMinutes(current) % 60,
            TimeUnit.MILLISECONDS.toSeconds(current) % 60,
            current % 1000);
        mChronoText.setText(text);
    }

    private long getCurrentTime(){
        return Calendar.getInstance().getTime().getTime();
    }

    private Calendar getCurrentCalendar(){
        return Calendar.getInstance();
    }
}
