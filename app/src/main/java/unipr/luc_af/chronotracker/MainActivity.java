package unipr.luc_af.chronotracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.chronotracker.helpers.Utils;
import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.classes.Lap;
import unipr.luc_af.classes.StartSessionData;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.models.ActivitySessionModel;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.models.PopupItemsModel;
import unipr.luc_af.models.TitleBarModel;


public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 0;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE_ALL = 1;
    public static final String TRACKER_TAG = "tracker_tag";
    public static final String ATHLETE_ACTIVITY_SUMMARY_TAG = "athelete_activity_summary_tag";
    public static final String ATHLETE_LIST_TAG = "athlete_tag";
    public static final String ATHLETE_ADD_TAG = "athlete_add_tag";
    public static final String ATHLETE_ACTIVITIES_LIST_TAG = "athlete_activity_tag";
    public static final String TOOLBAR_TRACKER_TAG = "toolbar_tracker_tag";

    private final String singleDayActivitiesFilName = "chrono_tracker_";

    private TitleBarModel mTitleModel;
    private AthleteModel mAthleteModel;
    private PopupItemsModel mPopupItemsModel;
    private ActivitySessionModel mActivitiesSessionModel;
    private int[] mPopupActiveItems = new int[0];
    private ActivitySession mCurrentSessionData = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Database.getInstance().setContext(this); //inizializziamo per questa activity il database service
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.root_toolbar);
        setSupportActionBar(myToolbar);
        if (savedInstanceState == null) {
            //Aggiungiamo il layout iniziale AthleteList se non stiamo ritornando da uno state change
            //in quel caso lo abbiamo gia sostituito con altri fragments
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.root, new AthleteList(), ATHLETE_LIST_TAG)
                    .commit();
        }
        //Action bar update observer usando un viewmodel
        mTitleModel = new ViewModelProvider(this).get(TitleBarModel.class);
        mAthleteModel = new ViewModelProvider(this).get(AthleteModel.class);
        mActivitiesSessionModel = new ViewModelProvider(this).get(ActivitySessionModel.class);
        mPopupItemsModel = new ViewModelProvider(this).get(PopupItemsModel.class);

        final Observer<int[]> popupItemsObserver = (items) -> {
            mPopupActiveItems = items;
            invalidateOptionsMenu();
        };

        final Observer<String> titleObserver = (title) -> getSupportActionBar().setTitle(title);
        final Observer<Athlete> athleteObserver = (athlete) -> addAthlete(athlete);
        final Observer<StartSessionData> startSessionDataObserver = (data) -> {
            if (data != null) {
                showTracker(data);
            }
        };
        final Observer<ActivitySession> activitySessionObserver = (data) ->
                mCurrentSessionData = data;

        mActivitiesSessionModel.getStartSession().observe(this, startSessionDataObserver);
        mActivitiesSessionModel.getActivitySession().observe(this, activitySessionObserver);
        mAthleteModel.getAthlete().observe(this, athleteObserver);
        mTitleModel.getTitle().observe(this, titleObserver);
        mPopupItemsModel.getActiveItems().observe(this, popupItemsObserver);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_icons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_export_current:
                return onExportCurrentClicked();
            case R.id.menu_export_all:
                return onExportAllClicked();
            case R.id.menu_start_tracking:
                return onStartTrackingClicked();
            case R.id.menu_tracking_done:
                return onFinishTrackingClicked();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < mPopupActiveItems.length; i++) {
            MenuItem currentItem = menu.findItem(mPopupActiveItems[i]);
            currentItem.setVisible(true);
        }
        return true;
    }

    private void showTracker(StartSessionData data) {
        if (data == null) return;
        if (data.isDataOk()) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .setCustomAnimations(
                            R.anim.horizontal_in_left,
                            R.anim.horizontal_out_left,
                            R.anim.horizontal_in,
                            R.anim.horizontal_out)
                    .replace(R.id.root, new ChronoTracker(), TRACKER_TAG)
                    .addToBackStack(TRACKER_TAG)
                    .commit();
            fragmentManager.executePendingTransactions();
        }
    }

    private boolean writeSessionsToFile( ActivitySession[] sessions, boolean exportLaps){
        if(sessions.length == 0) {
            return false;
        }
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            return false;
        }
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("ddmmyyyy");
        String strDate = dateFormat.format(date);
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < sessions.length; i++){
            ActivitySession currSession = sessions[i];
            builder.append(Utils.getInstance().concatString(",",
                    String.valueOf(currSession.id.longValue()),
                    String.valueOf(currSession.activity.longValue()),
                    (currSession.activityType != null) ? String.valueOf(currSession.activityType.longValue()) : "" ,
                    String.valueOf(currSession.startTime.longValue()),
                    String.valueOf(currSession.stopTime.longValue()),
                    String.valueOf(currSession.distance.longValue()),
                    String.valueOf(currSession.athlete.longValue()), "\n"
            ));
            if(currSession.laps != null && exportLaps) {
                builder.append( "Laps," + currSession.laps.length + "\n");
                for (int k = 0; k < currSession.laps.length; k++) {
                    Lap currLap = currSession.laps[k];
                    builder.append( Utils.getInstance().concatString(",",
                            String.valueOf(currLap.duration.longValue()),
                            String.valueOf(currLap.fromStart.longValue()), "\n"
                    ));
                }
            }
        }

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                singleDayActivitiesFilName+strDate+".txt");

        FileWriter outputStream;
        try {
            if(file.createNewFile()) {
                //second argument of FileOutputStream constructor indicates whether to append or create new file if one exists
                outputStream = new FileWriter(file, false);

                outputStream.write(builder.toString());
                outputStream.flush();
                outputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean onExportCurrentClicked(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        } else {
            return writeSessionsToFile(mActivitiesSessionModel.getCurrentDayActivities().getValue(),true);
        }
        return true;
    }

    private boolean onExportAllClicked(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_WRITE_EXTERNAL_STORAGE_ALL);
            }
        } else {
            DatabaseResult activitiesResult = (cursor) -> {
                cursor.moveToFirst();
                ActivitySession[] sessions = AthleteActivities.buildActivitySessions(cursor);
                writeSessionsToFile(sessions,false);
            };
            Database.getInstance().getAllActivitiesOfAthlete(mAthleteModel.getSelectedAthlete().getValue(),activitiesResult);
            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    writeSessionsToFile(mActivitiesSessionModel.getCurrentDayActivities().getValue(),true);
                }
                return;
            }
            case PERMISSION_WRITE_EXTERNAL_STORAGE_ALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    DatabaseResult activitiesResult = (cursor) -> {
                        cursor.moveToFirst();
                        ActivitySession[] sessions = AthleteActivities.buildActivitySessions(cursor);
                        writeSessionsToFile(sessions,false);
                    };
                    Database.getInstance().getAllActivitiesOfAthlete(mAthleteModel.getSelectedAthlete().getValue(),activitiesResult);
                }
                return;
            }
        }
    }

    private boolean onStartTrackingClicked() {
        StartTrackingDialog dialog = new StartTrackingDialog();
        dialog.show(getSupportFragmentManager(), "start_tracking_dialog");
        return true;
    }

    private boolean onFinishTrackingClicked() {
        if (mCurrentSessionData != null) {
            mActivitiesSessionModel.setEndSession(mCurrentSessionData);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ChronoTracker tracker = (ChronoTracker) getSupportFragmentManager().findFragmentByTag(TRACKER_TAG);
        if (tracker != null && tracker.isRemoving()) {
            if (mCurrentSessionData != null) {
                ToolBarTracker toolBarTracker = new ToolBarTracker();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.toolbar_row, toolBarTracker, TOOLBAR_TRACKER_TAG)
                        .commit();
            }
        }
    }

    private void addAthlete(Athlete athlete) {
        Database.getInstance().addAthlete(athlete, (id) -> {
            if (id != -1) {
                FragmentManager manager = getSupportFragmentManager();
                manager.beginTransaction()
                        .setCustomAnimations(
                                R.anim.horizontal_in_left,
                                R.anim.horizontal_out_left,
                                R.anim.horizontal_in,
                                R.anim.horizontal_out)
                        .replace(R.id.root, new AthleteList(), ATHLETE_LIST_TAG)
                        .addToBackStack(ATHLETE_LIST_TAG)
                        .commit();
                manager.executePendingTransactions();
                View contextView = findViewById(R.id.root_coordinator_layout);
                Utils.getInstance().executeWithDelay(700, () ->
                        Snackbar.make(contextView,
                                getString(R.string.athlete) + " " + athlete.name + " " + athlete.surname + " " + getString(R.string.added),
                                Snackbar.LENGTH_LONG)
                                .show()
                );
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
