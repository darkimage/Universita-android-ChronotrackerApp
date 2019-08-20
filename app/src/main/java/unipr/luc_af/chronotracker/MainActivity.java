package unipr.luc_af.chronotracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import unipr.luc_af.classes.Athlete;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.services.Database;
import unipr.luc_af.services.Utils;

public class MainActivity extends AppCompatActivity {

    private TitleBarModel mTitleModel;
    private AthleteModel mAthleteModel;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        Database.getInstance().setContext(this); //inizializziamo per questa activity il database service
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Aggiungiamo il fragment AthleteList
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.root, new AthleteList()).commit();

        //Action bar update observer usando un viewmodel
        mTitleModel = new ViewModelProvider(this).get(TitleBarModel.class);
        mAthleteModel = new ViewModelProvider(this).get(AthleteModel.class);

        final Observer<String> titleObserver = (title) -> {
            getSupportActionBar().setTitle(title);
        };

        final Observer<Athlete> athleteObserver = (athlete) -> {
            Database.getInstance().addAthelete(athlete, (id) -> {
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
                    Utils.getInstance().executeWithDelay(1000,() ->
                        Snackbar.make(contextView,
                                getString(R.string.athlete) + " " + athlete.name + " " + athlete.surname + " " + getString(R.string.added),
                                Snackbar.LENGTH_LONG)
                                .show()
                    );
                }
            });
        };

        mAthleteModel.getAthlete().observe(this, athleteObserver);
        mTitleModel.getTitle().observe(this, titleObserver);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
