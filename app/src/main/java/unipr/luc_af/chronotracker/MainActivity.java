package unipr.luc_af.chronotracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;

import unipr.luc_af.classes.Athlete;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.models.PopupItemsModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.services.Database;
import unipr.luc_af.services.Utils;

public class MainActivity extends AppCompatActivity {

    private TitleBarModel mTitleModel;
    private AthleteModel mAthleteModel;
    private PopupItemsModel mPopupItemsModel;
    private int[] mPopupActiveItems = new int[0];

    @Override
    protected void onCreate(Bundle savedInstanceState){
        Database.getInstance().setContext(this); //inizializziamo per questa activity il database service
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = findViewById(R.id.root_toolbar);
        setSupportActionBar(myToolbar);

        //Aggiungiamo il fragment AthleteList
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root, new AthleteList()).commit();

        //Action bar update observer usando un viewmodel
        mTitleModel = new ViewModelProvider(this).get(TitleBarModel.class);
        mAthleteModel = new ViewModelProvider(this).get(AthleteModel.class);
        mPopupItemsModel = new ViewModelProvider(this).get(PopupItemsModel.class);

        final Observer<int[]> popupItemsObserver = (items) ->{
            mPopupActiveItems = items;
            invalidateOptionsMenu();
        };

        final Observer<String> titleObserver = (title) -> {
            getSupportActionBar().setTitle(title);
        };

        final Observer<Athlete> athleteObserver = (athlete) -> {
            addAthlete(athlete);
        };

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
                onStartTrackingClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean onStartTrackingClicked(){
        DialogPlus.newDialog(this)
            .setContentHolder(new ViewHolder(R.layout.dialog_start_tracking))
            .setGravity(Gravity.CENTER)
            .setContentBackgroundResource(R.drawable.material_dialog_background)
            .create()
            .show();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for (int i = 0; i < mPopupActiveItems.length; i++) {
            MenuItem currentItem = menu.findItem(mPopupActiveItems[i]);
            currentItem.setVisible(true);
        }
        return true;
    }

    private void addAthlete(Athlete athlete){
        Database.getInstance().addAthlete(athlete, (id) -> {
            if(id != -1){
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.anim.horizontal_in_left,
                                R.anim.horizontal_out_left,
                                R.anim.horizontal_in,
                                R.anim.horizontal_out)
                        .replace(R.id.root, new AthleteList())
                        .addToBackStack(null)
                        .commit();
                View contextView = findViewById(R.id.root_coordinator_layout);
                Utils.getInstance().executeWithDelay(700,() ->
                        Snackbar.make(contextView,
                                getString(R.string.athlete) + " " + athlete.name + " " + athlete.surname + " " + getString(R.string.added),
                                Snackbar.LENGTH_LONG)
                                .show()
                );
            }
        });
    }
}
