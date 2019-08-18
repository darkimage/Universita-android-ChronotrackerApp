package unipr.luc_af.chronotracker;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import unipr.luc_af.classes.Athlete;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.services.Database;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.google.gson.Gson;

public class AthleteAdd extends Fragment {
    public String ATHLETE_NAME = "athlete_name";
    public String ATHLETE_SURNAME = "athlete_surname";
    public String ATHLETE_ACTIVITY = "athlete_activity";
    private TitleBarModel titleModel;
    private AthleteModel athleteModel;

    private EditText athleteNameText;
    private EditText athleteSurnameText;
    private TextView athleteActivity;

    private AwesomeValidation validation; //validator
    public AthleteAdd() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragView = inflater.inflate(R.layout.fragment_add_athlete, container, false);
        //Add the click listener to the button
        fragView.findViewById(R.id.add_button).setOnClickListener((view) -> Commit());
        return fragView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Inizializziamo i reference per degli input del form per la validazione
        athleteNameText = getView().findViewById(R.id.athlete_name);
        athleteSurnameText = getView().findViewById(R.id.athlete_surname);
        athleteActivity = getView().findViewById(R.id.athlete_activity);

        //inizializiomo il validatore
        validation = new AwesomeValidation(ValidationStyle.UNDERLABEL);
        validation.setContext(getActivity());
        //aggiungiamo le regole di validazione
        validation.addValidation(getActivity(), R.id.athlete_name, "^[A-Za-z\\s]+",R.string.athlete_name_error);
        validation.addValidation(getActivity(), R.id.athlete_surname, "^[A-Za-z\\s]+",R.string.athlete_surname_error);
        validation.addValidation(getActivity(), R.id.athlete_activity, "^[A-Za-z\\s]+",R.string.athlete_activity_error);
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putCharSequence(ATHLETE_NAME, athleteNameText.getText());
        savedInstanceState.putCharSequence(ATHLETE_SURNAME, athleteSurnameText.getText());
        savedInstanceState.putCharSequence(ATHLETE_ACTIVITY, athleteActivity.getText());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            athleteNameText.setText(savedInstanceState.getCharSequence(ATHLETE_NAME));
            athleteSurnameText.setText(savedInstanceState.getCharSequence(ATHLETE_SURNAME));
            athleteActivity.setText(savedInstanceState.getCharSequence(ATHLETE_ACTIVITY));
        }
    }

    @Override
    public synchronized void onStart() {
        super.onStart();
        //Aggiorniamo la actionbar title
        titleModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
        titleModel.setTitle(getActivity().getResources().getString(R.string.add_athlete));

        athleteModel = new ViewModelProvider(getActivity()).get(AthleteModel.class);

        //Ricaviamo i dati per popolare il dropdown menu dal database
        DatabaseResult activitiesResult = (Cursor cursor) -> setDropdownOptions(cursor);
        Database.getInstance().getActivityNames(activitiesResult);
    }

    private void setDropdownOptions(Cursor cursor){
        String[] result = new String[cursor.getCount()];
        if(cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                result[i] = cursor.getString(0);
                cursor.moveToNext();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_menu_item, result);
            AutoCompleteTextView editTextFilledExposedDropdown = getActivity().findViewById(R.id.athlete_activity);
            editTextFilledExposedDropdown.setAdapter(adapter);
        }
    }

    private void Commit(){
        if(validation.validate()){
            DatabaseResult activityResult = (cursor) -> {
                if(cursor.moveToFirst()) {
                    Athlete athlete = new Athlete(
                            athleteNameText.getText().toString(),
                            athleteSurnameText.getText().toString(),
                            cursor.getLong(0));
                    athleteModel.addAthlete(athlete);
                }
            };
            Database.getInstance().getActivityIdFromName(athleteActivity.getText().toString(), activityResult);
        }
    }
}
