package unipr.luc_af.chronotracker;

import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.chronotracker.helpers.Utils;
import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;

public class AthleteAdd extends Fragment {
    private String ATHLETE_NAME = "athlete_name";
    private String ATHLETE_SURNAME = "athlete_surname";
    private String ATHLETE_ACTIVITY = "athlete_activity";
    private TitleBarModel mTitleModel;
    private AthleteModel mAthleteModel;

    private EditText mAthleteNameText;
    private EditText mAthleteSurnameText;
    private TextView mAthleteActivity;

    private AwesomeValidation mValidator; //validator
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
        Utils.getInstance().setToolBarNavigation((AppCompatActivity)getActivity());
        //Add the click listener to the button
        fragView.findViewById(R.id.add_button).setOnClickListener((view) -> Commit());
        return fragView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Inizializziamo i reference per degli input del form per la validazione
        mAthleteNameText = getView().findViewById(R.id.athlete_name);
        mAthleteSurnameText = getView().findViewById(R.id.athlete_surname);
        mAthleteActivity = getView().findViewById(R.id.athlete_activity);

        //inizializiomo il validatore
        mValidator = new AwesomeValidation(ValidationStyle.UNDERLABEL);
        mValidator.setContext(getActivity());
        //aggiungiamo le regole di validazione
        mValidator.addValidation(getActivity(), R.id.athlete_name, "^[A-Za-z\\s]+",R.string.athlete_name_error);
        mValidator.addValidation(getActivity(), R.id.athlete_surname, "^[A-Za-z\\s]+",R.string.athlete_surname_error);
        mValidator.addValidation(getActivity(), R.id.athlete_activity, "^[A-Za-z\\s]+",R.string.athlete_activity_error);
    }


    @Override
    public synchronized void onStart() {
        super.onStart();
        //Aggiorniamo la actionbar title
        mTitleModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
        mTitleModel.setTitle(getActivity().getResources().getString(R.string.add_athlete));

        mAthleteModel = new ViewModelProvider(getActivity()).get(AthleteModel.class);

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
        if(mValidator.validate()){
            DatabaseResult activityResult = (cursor) -> {
                if(cursor.moveToFirst()) {
                    Athlete athlete = new Athlete(new Long(-1),
                            mAthleteNameText.getText().toString(),
                            mAthleteSurnameText.getText().toString(),
                            cursor.getLong(0));
                    mAthleteModel.addAthlete(athlete);
                }
            };
            Database.getInstance().getActivityIdFromName(mAthleteActivity.getText().toString(), activityResult);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putCharSequence(ATHLETE_NAME, mAthleteNameText.getText());
        savedInstanceState.putCharSequence(ATHLETE_SURNAME, mAthleteSurnameText.getText());
        savedInstanceState.putCharSequence(ATHLETE_ACTIVITY, mAthleteActivity.getText());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            mAthleteNameText.setText(savedInstanceState.getCharSequence(ATHLETE_NAME));
            mAthleteSurnameText.setText(savedInstanceState.getCharSequence(ATHLETE_SURNAME));
            mAthleteActivity.setText(savedInstanceState.getCharSequence(ATHLETE_ACTIVITY));
        }
    }
}
