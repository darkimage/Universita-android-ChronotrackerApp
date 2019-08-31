package unipr.luc_af.components;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import unipr.luc_af.chronotracker.ChronoApp;
import unipr.luc_af.chronotracker.MainActivity;
import unipr.luc_af.chronotracker.R;

import static unipr.luc_af.chronotracker.ChronoApp.CHANNEL_ID;

public class ChronoView extends LinearLayout {
    //LOGIC
    private Context mContext;
    private StateChangeListener mStateChangeListener = (state, view) -> {};
    private OnTickListener mOnTickListener = (time) -> {};
    private OnLapListener mOnLapListener = (elapsed, duration) -> {};
    private ChronoService mChronoService = null;

    //UI
    private TextView mChronoText;
    private TextView mChronoMillText;
    private TextView mChronoLapText;
    private TextView mChronoLapMillText;
    private RectF mArcViewSize;
    private RectF mViewSize;
    private Paint mSpinnerPaint;
    private Paint mSpinnerTimePaint;
    private Paint mSpinnerShadowPaint;
    private SweepGradient mSpinnerGradient;
    private int mSpinnerColor;
    private double mTheta = 0f;
    private float mSpinnerWidth;
    private float mOffset;
    private Matrix mGradientMatrix;
    private Bitmap mSpinnerHeaderBitmap;
    private Canvas mSpinnerHeaderCanvas;
    private RectF mClipRect;
    private float mRadius;
    private PorterDuffXfermode mClearPorter;
    private boolean setFirstState = false;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mChronoService = ((ChronoService.ChronoBinder) iBinder).getService();
            mChronoService.setOnTickListener((duration, lap) -> {
                updateView(duration,lap);
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mChronoService = null;
        }
    };

    public ChronoView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public ChronoView(Context context, AttributeSet attrs, int defStyleAttrs) {
        super(context, attrs, defStyleAttrs);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChronoView, defStyleAttrs, 0);
        View view = inflate(context, R.layout.component_chrono_view, this);
        setUpUI(typedArray,view);
        typedArray.recycle();
        setWillNotDraw(false);
    }

    public void init(){
        updateView(0,0);
        mContext.startService(new Intent(mContext, ChronoService.class));
        mContext.bindService(new Intent(mContext, ChronoService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindFromService(){
        mContext.unbindService(mConnection);
    }

    public void bindToService(){
        mContext.bindService(new Intent(mContext, ChronoService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int min = Math.min(w,h);
        float offsetLeft = (w - min)/2.0f;
        float offsetTop = (h - min)/2.0f;

        mArcViewSize = new RectF(
                offsetLeft + mOffset +  mSpinnerWidth/2.0f, // left
                offsetTop + mOffset +  mSpinnerWidth/2.0f, // top
                min + offsetLeft - mOffset -  mSpinnerWidth/2.0f, // right
                min + offsetTop - mOffset -  mSpinnerWidth/2.0f // bottom
        );

        mViewSize = new RectF(0 + mOffset,0 + mOffset, w - mOffset,h - mOffset);

        mSpinnerGradient = new SweepGradient(mViewSize.centerX(), mViewSize.centerY(),
                new int[]{Color.TRANSPARENT, mSpinnerColor},
                new float[]{0.75f, 1.0f});

        mSpinnerHeaderBitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        mSpinnerHeaderCanvas = new Canvas(mSpinnerHeaderBitmap);
        mRadius = Math.min(mViewSize.width(),mViewSize.height())/2.0f - mSpinnerWidth/2.0f;
        mClipRect = new RectF(
                mViewSize.centerX() - mRadius - mSpinnerWidth,
                mViewSize.centerY() - mRadius - mSpinnerWidth,
                mViewSize.centerX() - 5,
                mViewSize.centerY() - 5);

    }

    protected ChronoData getChronoData(){
        return (mChronoService != null) ? mChronoService.getData() : null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        double theta = (getChronoData() != null) ?  getChronoData().theta : 0;

        canvas.drawCircle( mViewSize.centerX(),mViewSize.centerY(),mRadius, mSpinnerPaint);
        canvas.drawCircle( mViewSize.centerX(),mViewSize.centerY(),mRadius - mSpinnerWidth/2.0f, mSpinnerShadowPaint);

        mSpinnerHeaderCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        float startGradient;
        float endGradient;
        SweepGradient gradient;
        mGradientMatrix.reset();
        if(theta < Math.toRadians(90)) {
            startGradient = 0.0f;
            endGradient = (float) (theta / Math.toRadians(360)) % 1.0f;
            gradient = new SweepGradient(mViewSize.centerX(), mViewSize.centerY(),
                    new int[]{Color.TRANSPARENT, mSpinnerColor},
                    new float[]{startGradient, endGradient});
            float rotate = -90;
            mGradientMatrix.preRotate(rotate, mViewSize.centerX(), mViewSize.centerY());
            gradient.setLocalMatrix(mGradientMatrix);
        }else{
            gradient = mSpinnerGradient;
            float rotate = -90 + (float)Math.toDegrees(theta);
            mGradientMatrix.preRotate(rotate, mViewSize.centerX(), mViewSize.centerY());
            gradient.setLocalMatrix(mGradientMatrix);
        }

        mSpinnerTimePaint.setShader(gradient);
        mSpinnerTimePaint.setXfermode(null);
        mSpinnerTimePaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(mArcViewSize, -90, (float)Math.toDegrees(theta), false, mSpinnerTimePaint);
        float circleX = (float)(mRadius*Math.cos(theta-Math.toRadians(90f))) + mViewSize.centerX();
        float circleY = (float)(mRadius*Math.sin(theta-Math.toRadians(90f))) + mViewSize.centerY();

        mSpinnerTimePaint.setStyle(Paint.Style.FILL);
        mSpinnerTimePaint.setShader(null);
        mSpinnerHeaderCanvas.drawCircle(circleX, circleY,mSpinnerWidth/2.0f, mSpinnerTimePaint);

        mSpinnerHeaderCanvas.save();
        mSpinnerHeaderCanvas.rotate((float)Math.toDegrees(theta), mViewSize.centerX(), mViewSize.centerY());
        mSpinnerTimePaint.setShader(null);
        mSpinnerTimePaint.setXfermode(mClearPorter);
        mSpinnerHeaderCanvas.drawRect(mClipRect,mSpinnerTimePaint);
        mSpinnerHeaderCanvas.restore();
        canvas.drawBitmap(mSpinnerHeaderBitmap,0,0,null);
    }

    public ChronoData getData(){
        return mChronoService.getData();
    }

    public void Start(){
        mChronoService.ExecuteTask(() ->{
            mStateChangeListener.onStateChange(getChronoData().state,this);
        });
    }

    public void Pause(){
        mChronoService.PauseTask(() -> {
            mStateChangeListener.onStateChange(getChronoData().state,this);
        });
    }

    public void Lap(){
        mChronoService.LapTask(() ->{
            mOnLapListener.onLap(getChronoData().lastLapDuration, mChronoService.getCurrentTaskElapsed());
        });
    }

    public void Reset(){
        mChronoService.ResetTask(() ->{
            mStateChangeListener.onStateChange(getChronoData().state,this);
        });
        resetView();
    }

    public void Stop(){
        if (mChronoService != null) {
            // Detach the service connection.
            unbindFromService();
            mContext.stopService(new Intent(mContext,ChronoService.class));
            Pause();
        }
    }

    public void setOnStateChangeListener(StateChangeListener onStateChange){
        mStateChangeListener = onStateChange;
    }

    public void setOnTickListener(OnTickListener onTickListener){
        mOnTickListener = onTickListener;
    }

    public void setOnLapListener(OnLapListener onLapListener){
        mOnLapListener = onLapListener;
    }

    protected void resetView(){
        invalidate();
        updateView(0,0);
    }

    protected void updateView(long current, long lap){
        if(!setFirstState && mStateChangeListener != null && getChronoData() != null){
            mStateChangeListener.onStateChange(getChronoData().state,this);
            setFirstState = true;
        }

        String timeText = formatTime(current,false);
        mChronoText.setText(timeText);

        String millText = String.format(".%03d",current % 1000);
        mChronoMillText.setText(millText);

        String lapText = formatTime(lap,false);
        mChronoLapText.setText(lapText);

        String millLapText = String.format(".%03d",lap % 1000);
        mChronoLapMillText.setText(millLapText);

        mTheta = (current == 0) ? 0 : mTheta + Math.toRadians(360) / 6000;
    }

    static String formatTime(long millisec, boolean useMillisec){
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

    protected void setUpUI(TypedArray typedArray, View view){
        int millisecondsColor = typedArray.getColor(
                R.styleable.ChronoView_milliseconds_color,
                getResources().getColor(R.color.primaryDisabledColor));

        int backgroundColor = typedArray.getColor(
                R.styleable.ChronoView_spinner_background,
                getResources().getColor(R.color.primaryLightColor));

        int shadowColor = typedArray.getColor(
                R.styleable.ChronoView_spinner_shadow,
                Color.BLACK);

        float textSize = typedArray.getDimension(
                R.styleable.ChronoView_time_size,
                18);

        float textLapSize = typedArray.getDimension(
                R.styleable.ChronoView_lap_size,
                10);

        float millisecondsSize = typedArray.getDimension(
                R.styleable.ChronoView_milliseconds_size,
                12);

        float millisecondsLapSize = typedArray.getDimension(
                R.styleable.ChronoView_lap_milliseconds_size,
                6);

        mGradientMatrix = new Matrix();
        mClearPorter = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        mSpinnerColor = typedArray.getColor(
                R.styleable.ChronoView_spinner_color,
                getResources().getColor(R.color.primaryColor));

        mSpinnerWidth = typedArray.getDimension(
                R.styleable.ChronoView_spinner_width,
                50.0f);

        mChronoText = view.findViewById(R.id.chronoview_main_text);
        mChronoText.setTextSize(textSize);
        mChronoMillText = view.findViewById(R.id.chronoview_milliseconds_text);
        mChronoMillText.setTextSize(millisecondsSize);
        mChronoMillText.setTextColor(millisecondsColor);

        mChronoLapText = view.findViewById(R.id.chronoview_lap_text);
        mChronoLapText.setTextSize(textLapSize);
        mChronoLapMillText = view.findViewById(R.id.chronoview_lap_milliseconds_text);
        mChronoLapMillText.setTextSize(millisecondsLapSize);
        mChronoLapMillText.setTextColor(millisecondsColor);


        mSpinnerPaint = new Paint();
        mSpinnerPaint.setAntiAlias(true);
        mSpinnerPaint.setStyle(Paint.Style.STROKE);
        mSpinnerPaint.setStrokeWidth(mSpinnerWidth);
        mSpinnerPaint.setColor(backgroundColor);

        mSpinnerShadowPaint = new Paint();
        mSpinnerShadowPaint.setAntiAlias(true);
        mSpinnerShadowPaint.setColor(Color.WHITE);
        mSpinnerShadowPaint.setShadowLayer(mSpinnerWidth/2.0f,0,mSpinnerWidth/4.0f, shadowColor);

        mSpinnerTimePaint = new Paint();
        mSpinnerTimePaint.setAntiAlias(true);
        mSpinnerTimePaint.setStyle(Paint.Style.STROKE);
        mSpinnerTimePaint.setStrokeWidth(mSpinnerWidth);
        mSpinnerTimePaint.setColor(mSpinnerColor);

        mOffset = mSpinnerWidth / 2.0f;
    }

    public interface StateChangeListener{
        void onStateChange(ChronoData.State state, ChronoView view);
    }

    public interface OnTickListener {
        void onTick(long time);
    }

    public interface OnLapListener{
        void onLap(long duration, long currentDuration);
    }

    static public class ChronoService extends Service{
        public final static String TIME_STEP_EXTRA = "time_step_extra";
        public final static String TEXT_SMALL_EXTRA = "text_small_extra";
        public final static String TEXT_TITLE_EXTRA = "text_title_extra";
        public final static String TEXT_DURATION_EXTRA = "text_duration_extra";
        public final static String TEXT_CURRENT_EXTRA = "text_current_extra";
        public final static String TEXT_NO_LAPS_EXTRA = "text_no_laps_extra";
        private Timer mTimer ;
        private Handler mHandler ;
        private final IBinder mChronoBinder = new ChronoBinder();
        private ChronoData chronoData;
        private TimerTask mTask;
        private long timeStep;
        private OnTickListener tickListener = (a,b)->{};

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
            timeStep = intent.getLongExtra(TIME_STEP_EXTRA,10);
            startForeground(1,buildNotification(intent));
            updateNotification();
            return START_NOT_STICKY;
        }


        private Notification buildNotification(Intent intent){
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
            PendingIntent pendingIntent = PendingIntent.getActivity(this,0, mainActivity,PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setContentTitle("Chrono Training")
                    .setCustomContentView(notificationLayout)
                    .setCustomBigContentView(notificationLayoutBig)
                    .setOnlyAlertOnce(true)
                    .setSmallIcon(R.drawable.ic_stopwatch_solid)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .addAction(R.drawable.ic_play_solid,ChronoApp.getContext().getString(R.string.notification_action_goto),pendingIntent)
                    .build();
            return notification;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return mChronoBinder;
        }

        private class ChronoBinder extends Binder {
            ChronoService getService(){
                return ChronoService.this;
            }
        }

        public ChronoData getData(){
            return chronoData;
        }

        public void ExecuteTask() {
            ExecuteTask(() -> {});
        }

        public void ExecuteTask(ActionTask task){
            long current = getCurrentTime();
            if(chronoData.startTime == null){
                chronoData.lastLapTime = current;
                chronoData.initialTime = current;
            }
            chronoData.startTime = current;
            chronoData.state = ChronoData.State.TRACK;
            task.runTask();
            mTimer.scheduleAtFixedRate(mTask,0,timeStep);
            updateNotification();
        }

        public void LapTask(ActionTask task){
            if(chronoData.state != ChronoData.State.PAUSE) {
                Long current = getCurrentTime();
                long lapTime = current - chronoData.lastLapTime;
                chronoData.lastLapTime = current;
                chronoData.lastLapDuration = lapTime;
                task.runTask();
            }
            updateNotification();
        }

        public void PauseTask(ActionTask task){
            if(chronoData.state != ChronoData.State.RESET) {
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

        public void ResetTask(ActionTask task){
            PauseTask(()->{});
            chronoData.state = ChronoData.State.RESET;
            chronoData.startTime = null;
            chronoData.lastPausedTime = null;
            chronoData.lastLapTime = null;
            chronoData.lastLapDuration = (long)0;
            chronoData.partialElapsed = (long)0;
            chronoData.theta = (double)0;
            task.runTask();
            updateNotification();
        }

        public long getCurrentTaskElapsed(){
            long current = getCurrentTime() - chronoData.startTime;
            current += chronoData.partialElapsed;
            return current;
        }

        public void setOnTickListener(OnTickListener listener){
            tickListener = listener;
        }

        private TimerTask Task(){
            return new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(() -> {
                        if(chronoData.state != ChronoData.State.PAUSE &&
                        chronoData.state != ChronoData.State.RESET) {
                            double angle360 = Math.toRadians(360);
                            double step = (60000f / timeStep);
                            chronoData.theta += angle360 / step;
                            tickListener.onTick(getCurrentTaskElapsed(),chronoData.lastLapDuration);
                        }
                    });
                }
            };
        }

        public void updateNotification(){
            String textSmall;
            Context context = ChronoApp.getContext();
            ChronoData.State state = chronoData.state;
            switch (state){
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
            if(chronoData.lastLapDuration != 0) {
                updateIntent.putExtra(TEXT_DURATION_EXTRA, context.getString(R.string.duration_placeholder, formatTime(chronoData.lastLapDuration, true)));
                updateIntent.putExtra(TEXT_CURRENT_EXTRA, formatTime(getCurrentTaskElapsed(), true));
            }else{
                updateIntent.putExtra(TEXT_NO_LAPS_EXTRA, context.getString(R.string.no_laps));
            }
            Notification notification = buildNotification(updateIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1,notification);
        }

        private long getCurrentTime() {
            return Calendar.getInstance().getTime().getTime();
        }

        public interface ActionTask{
            void runTask();
        }

        public interface OnTickListener{
            void onTick(long duration, long lap);
        }
    }

    static public class ChronoData implements Parcelable {
        public Long initialTime;
        public Long startTime;
        public Double theta = (double)0;
        public Long lastLapTime;
        public Long lastLapDuration = (long)0;
        public Long lastPausedTime;
        public Long partialElapsed = (long)0;
        public State state = State.RESET;

        public enum State {
            TRACK,
            PAUSE,
            RESET
        }

        public ChronoData(){}

        public ChronoData(Parcel parcel){
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
