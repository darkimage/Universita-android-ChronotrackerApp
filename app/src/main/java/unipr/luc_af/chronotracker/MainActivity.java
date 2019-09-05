package unipr.luc_af.chronotracker;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.chronotracker.helpers.Utils;
import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.classes.StartSessionData;
import unipr.luc_af.models.ActivitySessionModel;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.models.PopupItemsModel;
import unipr.luc_af.models.TitleBarModel;


public class MainActivity extends AppCompatActivity {
    public static final String TRACKER_TAG = "tracker_tag";
    public static final String ATHLETE_ACTIVITY_SUMMARY_TAG = "athelete_activity_summary_tag";
    public static final String ATHLETE_LIST_TAG = "athlete_tag";
    public static final String ATHLETE_ADD_TAG = "athlete_add_tag";
    public static final String ATHLETE_ACTIVITIES_LIST_TAG = "athlete_activity_tag";
    public static final String TOOLBAR_TRACKER_TAG = "toolbar_tracker_tag";

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
