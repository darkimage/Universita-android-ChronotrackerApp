package unipr.luc_af.chronotracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.services.Database;

public class MainActivity extends AppCompatActivity {

    private TitleBarModel titleModel;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        Database.getInstance().setContext(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addAthlete addAthleteFragment = new addAthlete();
        getSupportFragmentManager().beginTransaction().add(R.id.layout, addAthleteFragment).commit();

        titleModel = new ViewModelProvider(this).get(TitleBarModel.class);

        // Create the observer which updates the UI.
        final Observer<String> titleObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable final String title) {
                // Update the UI, in this case, a TextView.
                getSupportActionBar().setTitle(title);
            }
        };

        titleModel.getTitle().observe(this, titleObserver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Database.getInstance().addActivity("Testing");
    }
}
