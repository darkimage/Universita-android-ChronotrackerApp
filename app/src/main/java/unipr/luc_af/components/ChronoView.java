package unipr.luc_af.components;

import android.content.Context;
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
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import unipr.luc_af.chronotracker.R;

public class ChronoView extends LinearLayout {
    //LOGIC
    private Timer mChronometer;
    private TimerTask mChronoTask;
    private Long mStartTime;
    private Long mLastLapTime;
    private Long mLastLapDuration = new Long(0);
    private Long mLastPauseTime;
    private Long mTotalElapsed = new Long(0);
    private final Handler mHandler = new Handler();
    private ChronoView.State mCurrentState;
    private StateChangeListener mStateChangeListener = (state, view) -> {};
    private OnTickListener mOnTickListener = (time) -> {};
    private OnLapListener mOnLapListener = (duration) -> {};

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

    enum State {
        TRACKING,
        PAUSED,
        RESETTED
    }

    public ChronoView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public ChronoView(Context context, AttributeSet attrs, int defStyleAttrs) {
        super(context, attrs, defStyleAttrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChronoView, defStyleAttrs, 0);
        View view = inflate(context, R.layout.component_chrono_view, this);
        mChronometer = new Timer();
        mChronoTask = createTimerTask();
        setUpUI(typedArray,view);
        typedArray.recycle();
        setWillNotDraw(false);
        updateView(0);
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle( mViewSize.centerX(),mViewSize.centerY(),mRadius, mSpinnerPaint);
        canvas.drawCircle( mViewSize.centerX(),mViewSize.centerY(),mRadius - mSpinnerWidth/2.0f, mSpinnerShadowPaint);

        mSpinnerHeaderCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        float startGradient;
        float endGradient;
        SweepGradient gradient;
        mGradientMatrix.reset();
        if(mTheta < Math.toRadians(90)) {
            startGradient = 0.0f;
            endGradient = (float) (mTheta / Math.toRadians(360)) % 1.0f;
            gradient = new SweepGradient(mViewSize.centerX(), mViewSize.centerY(),
                    new int[]{Color.TRANSPARENT, mSpinnerColor},
                    new float[]{startGradient, endGradient});
            float rotate = -90;
            mGradientMatrix.preRotate(rotate, mViewSize.centerX(), mViewSize.centerY());
            gradient.setLocalMatrix(mGradientMatrix);
        }else{
            gradient = mSpinnerGradient;
            float rotate = -90 + (float)Math.toDegrees(mTheta);
            mGradientMatrix.preRotate(rotate, mViewSize.centerX(), mViewSize.centerY());
            gradient.setLocalMatrix(mGradientMatrix);
        }

        mSpinnerTimePaint.setShader(gradient);
        mSpinnerTimePaint.setXfermode(null);
        mSpinnerTimePaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(mArcViewSize, -90, (float)Math.toDegrees(mTheta), false, mSpinnerTimePaint);
        float circleX = (float)(mRadius*Math.cos(mTheta-Math.toRadians(90f))) + mViewSize.centerX();
        float circleY = (float)(mRadius*Math.sin(mTheta-Math.toRadians(90f))) + mViewSize.centerY();

        mSpinnerTimePaint.setStyle(Paint.Style.FILL);
        mSpinnerTimePaint.setShader(null);
        mSpinnerHeaderCanvas.drawCircle(circleX, circleY,mSpinnerWidth/2.0f, mSpinnerTimePaint);

        mSpinnerHeaderCanvas.save();
        mSpinnerHeaderCanvas.rotate((float)Math.toDegrees(mTheta), mViewSize.centerX(), mViewSize.centerY());
        mSpinnerTimePaint.setShader(null);
        mSpinnerTimePaint.setXfermode(mClearPorter);
        mSpinnerHeaderCanvas.drawRect(mClipRect,mSpinnerTimePaint);
        mSpinnerHeaderCanvas.restore();
        canvas.drawBitmap(mSpinnerHeaderBitmap,0,0,null);
    }

    public long Start(){
        mCurrentState = State.TRACKING;
        mChronometer.scheduleAtFixedRate(mChronoTask,0,10);
        if(mStartTime == null) {
            mStartTime = getCurrentTime();
            mLastLapTime = mStartTime;
        }else{
            long oldStart = mStartTime;
            mStartTime = getCurrentTime();
            return oldStart;
        }
        return mStartTime;
    }

    public long Pause(){
        mCurrentState = State.PAUSED;
        mChronometer.cancel();
        mChronometer = new Timer();
        mChronoTask = createTimerTask();
        mLastPauseTime = getCurrentTime();
        mTotalElapsed += mLastPauseTime - mStartTime;
        return mLastPauseTime;
    }

    public long Lap(){
        if(mCurrentState != State.PAUSED) {
            long current = getCurrentTime();
            long lapTime = current - mLastLapTime;
            mLastLapDuration = lapTime;
            mLastLapTime = current;
            mOnLapListener.onLap(lapTime);
            return current;
        }
        return -1;
    }

    public long ElapsedFromLastLap(){
        return 0;
    }

    public void Reset(){
        mCurrentState = State.RESETTED;
        Pause();
        mStartTime = null;
        mLastPauseTime = null;
        mTheta = 0;
        resetView();
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

    private void resetView(){
        invalidate();
        updateView(0);
    }

    private void updateView(long current){
        String timeText = String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(current),
            TimeUnit.MILLISECONDS.toMinutes(current) % 60,
            TimeUnit.MILLISECONDS.toSeconds(current) % 60);
        mChronoText.setText(timeText);

        String millText = String.format(":%03d",current % 1000);
        mChronoMillText.setText(millText);

        String lapText = String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(mLastLapDuration),
            TimeUnit.MILLISECONDS.toMinutes(mLastLapDuration) % 60,
            TimeUnit.MILLISECONDS.toSeconds(mLastLapDuration) % 60);
        mChronoLapText.setText(lapText);

        String millLapText = String.format(":%03d",mLastLapDuration % 1000);
        mChronoLapMillText.setText(millLapText);

        mTheta = (current == 0) ? 0 : mTheta + Math.toRadians(360) / 6000f;
    }

    private TimerTask createTimerTask(){
        return new TimerTask() {
            @Override
            public void run() {
                mHandler.post(() -> {
                    if(mCurrentState != State.PAUSED) {
                        long current;
                        current = getCurrentTime() - mStartTime;
                        if (mLastPauseTime != null) {
                            current += mTotalElapsed;
                        }
                        updateView(current);
                    }
                });
            }
        };
    }

    private long getCurrentTime(){
        return Calendar.getInstance().getTime().getTime();
    }

    private Calendar getCurrentCalendar(){
        return Calendar.getInstance();
    }

    private void setUpUI(TypedArray typedArray, View view){
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
        void onStateChange(State state, ChronoView view);
    }

    public interface OnTickListener {
        void onTick(long time);
    }

    public interface OnLapListener{
        void onLap(long duration);
    }
}
