package unipr.luc_af.chronotracker;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import java.io.IOException;
import unipr.luc_af.Services.Database;
import unipr.luc_af.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        Database.getInstance().setContext(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addAthlete addAthleteFragment = new addAthlete();
        getSupportFragmentManager().beginTransaction().add(R.id.layout, addAthleteFragment).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Database.getInstance().addActivity("Testing");
    }
}
