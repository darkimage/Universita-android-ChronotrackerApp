package unipr.luc_af.chronotracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.widget.TextView;

import unipr.luc_af.classes.Athlete;
import unipr.luc_af.database.interfaces.DatabaseInsert;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.services.Database;

import static androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

public class MainActivity extends AppCompatActivity {

    private TitleBarModel titleModel;
    private AthleteModel athleteModel;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        Database.getInstance().setContext(this); //inizializziamo per questa activity il database service
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Aggiungiamo il fragment AthleteAdd
        AthleteAdd athleteAddFragment = new AthleteAdd();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, athleteAddFragment).commit();

        //Action bar update observer usando un viewmodel
        titleModel = new ViewModelProvider(this).get(TitleBarModel.class);
        athleteModel = new ViewModelProvider(this).get(AthleteModel.class);

        final Observer<String> titleObserver = (title) -> {
            getSupportActionBar().setTitle(title);
        };

        final Observer<Athlete> athleteObserver = (athlete) -> {
            Database.getInstance().addAthelete(athlete, (id) -> {
                if(id != -1){
                    TextView txt = findViewById(R.id.prova);
                    txt.setText(athlete.name);
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, testFragment.newInstance("test","test2"))
                    .addToBackStack("root").commit();
                }
            });
        };

        athleteModel.getAthlete().observe(this, athleteObserver);
        titleModel.getTitle().observe(this, titleObserver);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getSupportFragmentManager().popBackStackImmediate("root", POP_BACK_STACK_INCLUSIVE);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }
}
