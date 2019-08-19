package unipr.luc_af.chronotracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import android.widget.TextView;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.services.Database;

import static androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

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
                .replace(R.id.frameLayout, new AthleteList()).commit();

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
                        .replace(R.id.frameLayout, new AthleteList())
                        .addToBackStack(null)
                        .commit();
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
