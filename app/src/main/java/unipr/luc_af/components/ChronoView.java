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
import unipr.luc_af.chronotracker.helpers.ChronoService;

import static unipr.luc_af.chronotracker.ChronoApp.CHANNEL_ID;

public class ChronoView extends LinearLayout {
    //LOGIC
    private Context mContext;
    private StateChangeListener mStateChangeListener = (state, view) -> {
    };
    private OnTickListener mOnTickListener = (time) -> {
    };
    private OnLapListener mOnLapListener = (elapsed, duration) -> {
    };
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
    private OnServiceConnectedListener mServiceConnectedListener;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mChronoService = ((ChronoService.ChronoBinder) iBinder).getService();
            mChronoService.setOnTickListener((duration, lap) -> {
                updateView(duration, lap);
            });
            mServiceConnectedListener.onConnection(mChronoService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mChronoService = null;
        }
    };

    public ChronoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChronoView(Context context, AttributeSet attrs, int defStyleAttrs) {
        super(context, attrs, defStyleAttrs);
        mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ChronoView, defStyleAttrs, 0);
        View view = inflate(context, R.layout.component_chrono_view, this);
        setUpUI(typedArray, view);
        typedArray.recycle();
        setWillNotDraw(false);
    }

    public void init() {
        updateView(0, 0);
        mContext.startService(new Intent(mContext, ChronoService.class));
        mContext.bindService(new Intent(mContext, ChronoService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindFromService() {
        mContext.unbindService(mConnection);
    }

    public void stopChronoService() {
        mContext.stopService(new Intent(mContext, ChronoService.class));
    }

    public void bindToService() {
        mContext.bindService(new Intent(mContext, ChronoService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int min = Math.min(w, h);
        float offsetLeft = (w - min) / 2.0f;
        float offsetTop = (h - min) / 2.0f;

        mArcViewSize = new RectF(
                offsetLeft + mOffset + mSpinnerWidth / 2.0f, // left
                offsetTop + mOffset + mSpinnerWidth / 2.0f, // top
                min + offsetLeft - mOffset - mSpinnerWidth / 2.0f, // right
                min + offsetTop - mOffset - mSpinnerWidth / 2.0f // bottom
        );

        mViewSize = new RectF(0 + mOffset, 0 + mOffset, w - mOffset, h - mOffset);

        mSpinnerGradient = new SweepGradient(mViewSize.centerX(), mViewSize.centerY(),
                new int[]{Color.TRANSPARENT, mSpinnerColor},
                new float[]{0.75f, 1.0f});

        mSpinnerHeaderBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mSpinnerHeaderCanvas = new Canvas(mSpinnerHeaderBitmap);
        mRadius = Math.min(mViewSize.width(), mViewSize.height()) / 2.0f - mSpinnerWidth / 2.0f;
        mClipRect = new RectF(
                mViewSize.centerX() - mRadius - mSpinnerWidth,
                mViewSize.centerY() - mRadius - mSpinnerWidth,
                mViewSize.centerX() - 5,
                mViewSize.centerY() - 5);

    }

    protected ChronoService.ChronoData getChronoData() {
        return (mChronoService != null) ? mChronoService.getData() : null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        double theta = (getChronoData() != null) ? getChronoData().theta : 0;

        canvas.drawCircle(mViewSize.centerX(), mViewSize.centerY(), mRadius, mSpinnerPaint);
        canvas.drawCircle(mViewSize.centerX(), mViewSize.centerY(), mRadius - mSpinnerWidth / 2.0f, mSpinnerShadowPaint);

        mSpinnerHeaderCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        float startGradient;
        float endGradient;
        SweepGradient gradient;
        mGradientMatrix.reset();
        if (theta < Math.toRadians(90)) {
            startGradient = 0.0f;
            endGradient = (float) (theta / Math.toRadians(360)) % 1.0f;
            gradient = new SweepGradient(mViewSize.centerX(), mViewSize.centerY(),
                    new int[]{Color.TRANSPARENT, mSpinnerColor},
                    new float[]{startGradient, endGradient});
            float rotate = -90;
            mGradientMatrix.preRotate(rotate, mViewSize.centerX(), mViewSize.centerY());
            gradient.setLocalMatrix(mGradientMatrix);
        } else {
            gradient = mSpinnerGradient;
            float rotate = -90 + (float) Math.toDegrees(theta);
            mGradientMatrix.preRotate(rotate, mViewSize.centerX(), mViewSize.centerY());
            gradient.setLocalMatrix(mGradientMatrix);
        }

        mSpinnerTimePaint.setShader(gradient);
        mSpinnerTimePaint.setXfermode(null);
        mSpinnerTimePaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(mArcViewSize, -90, (float) Math.toDegrees(theta), false, mSpinnerTimePaint);
        float circleX = (float) (mRadius * Math.cos(theta - Math.toRadians(90f))) + mViewSize.centerX();
        float circleY = (float) (mRadius * Math.sin(theta - Math.toRadians(90f))) + mViewSize.centerY();

        mSpinnerTimePaint.setStyle(Paint.Style.FILL);
        mSpinnerTimePaint.setShader(null);
        mSpinnerHeaderCanvas.drawCircle(circleX, circleY, mSpinnerWidth / 2.0f, mSpinnerTimePaint);

        mSpinnerHeaderCanvas.save();
        mSpinnerHeaderCanvas.rotate((float) Math.toDegrees(theta), mViewSize.centerX(), mViewSize.centerY());
        mSpinnerTimePaint.setShader(null);
        mSpinnerTimePaint.setXfermode(mClearPorter);
        mSpinnerHeaderCanvas.drawRect(mClipRect, mSpinnerTimePaint);
        mSpinnerHeaderCanvas.restore();
        canvas.drawBitmap(mSpinnerHeaderBitmap, 0, 0, null);
    }

    public ChronoService.ChronoData getData() {
        return mChronoService.getData();
    }

    public void Start() {
        mChronoService.ExecuteTask(() -> {
            mStateChangeListener.onStateChange(getChronoData().state, this);
        });
    }

    public void Pause() {
        mChronoService.PauseTask(() -> {
            mStateChangeListener.onStateChange(getChronoData().state, this);
        });
    }

    public void Lap() {
        mChronoService.LapTask(() -> {
            mOnLapListener.onLap(getChronoData().lastLapDuration, mChronoService.getCurrentTaskElapsed());
        });
    }

    public void Reset() {
        mChronoService.ResetTask(() -> {
            mStateChangeListener.onStateChange(getChronoData().state, this);
        });
        resetView();
    }

    public void Stop() {
        if (mChronoService != null) {
            // Detach the service connection.
//            unbindFromService();
            mContext.stopService(new Intent(mContext, ChronoService.class));
            Pause();
        }
    }

    protected void resetView() {
        invalidate();
        updateView(0, 0);
    }

    protected void updateView(long current, long lap) {
        if (!setFirstState && mStateChangeListener != null && getChronoData() != null) {
            mStateChangeListener.onStateChange(getChronoData().state, this);
            setFirstState = true;
        }

        String timeText = formatTime(current, false);
        mChronoText.setText(timeText);

        String millText = String.format(".%03d", current % 1000);
        mChronoMillText.setText(millText);

        String lapText = formatTime(lap, false);
        mChronoLapText.setText(lapText);

        String millLapText = String.format(".%03d", lap % 1000);
        mChronoLapMillText.setText(millLapText);

        mTheta = (current == 0) ? 0 : mTheta + Math.toRadians(360) / 6000;
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

    protected void setUpUI(TypedArray typedArray, View view) {
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

        mChronoText = view.findViewById(R.id.chrono_view_main_text);
        mChronoText.setTextSize(textSize);
        mChronoMillText = view.findViewById(R.id.chrono_view_milliseconds_text);
        mChronoMillText.setTextSize(millisecondsSize);
        mChronoMillText.setTextColor(millisecondsColor);

        mChronoLapText = view.findViewById(R.id.chrono_view_lap_text);
        mChronoLapText.setTextSize(textLapSize);
        mChronoLapMillText = view.findViewById(R.id.chrono_view_lap_milliseconds_text);
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
        mSpinnerShadowPaint.setShadowLayer(mSpinnerWidth / 2.0f, 0, mSpinnerWidth / 4.0f, shadowColor);

        mSpinnerTimePaint = new Paint();
        mSpinnerTimePaint.setAntiAlias(true);
        mSpinnerTimePaint.setStyle(Paint.Style.STROKE);
        mSpinnerTimePaint.setStrokeWidth(mSpinnerWidth);
        mSpinnerTimePaint.setColor(mSpinnerColor);

        mOffset = mSpinnerWidth / 2.0f;
    }

    public void setOnServiceConnectedListener(OnServiceConnectedListener onServiceConnectedListener) {
        mServiceConnectedListener = onServiceConnectedListener;
    }

    public void setOnStateChangeListener(StateChangeListener onStateChange) {
        mStateChangeListener = onStateChange;
    }

    public void setOnTickListener(OnTickListener onTickListener) {
        mOnTickListener = onTickListener;
    }

    public void setOnLapListener(OnLapListener onLapListener) {
        mOnLapListener = onLapListener;
    }

    public interface StateChangeListener {
        void onStateChange(ChronoService.ChronoData.State state, ChronoView view);
    }

    public interface OnTickListener {
        void onTick(long time);
    }

    public interface OnLapListener {
        void onLap(long duration, long currentDuration);
    }

    public interface OnServiceConnectedListener {
        void onConnection(ChronoService service);
    }
}
