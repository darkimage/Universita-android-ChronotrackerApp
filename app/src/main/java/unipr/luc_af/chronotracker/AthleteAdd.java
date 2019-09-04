package unipr.luc_af.chronotracker;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
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
    private String ATHLETE_TAG = "athlete_tag";
    private String ATHLETE_NAME = "athlete_name";
    private String ATHLETE_SURNAME = "athlete_surname";
    private String ATHLETE_ACTIVITY = "athlete_activity";

    //LOGIC
    private TitleBarModel mTitleModel;
    private AthleteModel mAthleteModel;
    private Athlete mAthlete;

    //UI
    private EditText mAthleteNameText;
    private EditText mAthleteSurnameText;
    private AutoCompleteTextView mAthleteActivity;

    private AwesomeValidation mValidator; //validator
    public AthleteAdd() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_athlete, container, false);
        Utils.getInstance().setToolBarNavigation((AppCompatActivity)getActivity());
        SetUpModels(savedInstanceState);
        SetUpUi(view,savedInstanceState);
        return view;
    }

    private void SetUpModels(Bundle savedInstanceState){
        mTitleModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
        mTitleModel.setTitle(getActivity().getResources().getString(R.string.add_athlete));
        mAthleteModel = new ViewModelProvider(getActivity()).get(AthleteModel.class);
        //Ricaviamo i dati per popolare il dropdown menu dal database
    }


    private void SetUpUi(View view, Bundle savedInstanceState){
        view.findViewById(R.id.add_button).setOnClickListener((v) -> Commit());

        //Inizializziamo i reference per degli input del form per la validazione
        TextWatcher updateAthleteWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateAthleteReference();
            }
        };

        mAthleteNameText = view.findViewById(R.id.athlete_name);
        mAthleteNameText.addTextChangedListener(updateAthleteWatcher);
        mAthleteSurnameText = view.findViewById(R.id.athlete_surname);
        mAthleteSurnameText.addTextChangedListener(updateAthleteWatcher);
        mAthleteActivity = view.findViewById(R.id.athlete_activity);
        mAthleteActivity.addTextChangedListener(updateAthleteWatcher);

        //inizializiomo il validatore
        mValidator = new AwesomeValidation(ValidationStyle.UNDERLABEL);
        mValidator.setContext(getActivity());
        //aggiungiamo le regole di validazione
        mValidator.addValidation(getActivity(), R.id.athlete_name, "^[A-Za-z\\s]+",R.string.athlete_name_error);
        mValidator.addValidation(getActivity(), R.id.athlete_surname, "^[A-Za-z\\s]+",R.string.athlete_surname_error);
        mValidator.addValidation(getActivity(), R.id.athlete_activity, "^[A-Za-z\\s]+",R.string.athlete_activity_error);

        DatabaseResult activitiesResult = (Cursor cursor) -> setDropdownOptions(cursor);
        Database.getInstance().getActivityNames(activitiesResult);

        if(savedInstanceState != null){
            mAthleteNameText.setText(mAthlete.name);
            mAthleteSurnameText.setText(mAthlete.surname);
            DatabaseResult activityIdResult = (cursor1) -> {
                cursor1.moveToNext();
                mAthleteActivity.setText(cursor1.getString(1));
                mAthleteActivity.showDropDown();
            };
            Database.getInstance().getActivityFromId(mAthlete.activityReference, activityIdResult);
        }
    }

    private interface AthleteUpdateListener {
        void onUpdate(Athlete athlete);
    }

    private void updateAthleteReference(){
        updateAthleteReference((a)->{});
    }

    private void updateAthleteReference(AthleteUpdateListener athleteUpdate){
        DatabaseResult activityResult = (cursor) -> {
            if(cursor.moveToFirst()) {
                mAthlete = new Athlete(new Long(-1),
                        mAthleteNameText.getText().toString(),
                        mAthleteSurnameText.getText().toString(),
                        cursor.getLong(0));
                athleteUpdate.onUpdate(mAthlete);
            }
        };
        Database.getInstance().getActivityIdFromName(mAthleteActivity.getText().toString(), activityResult);
    }

    private void setDropdownOptions(Cursor cursor){
        String[] result = new String[cursor.getCount()];
        if(cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                result[i] = cursor.getString(0);
                cursor.moveToNext();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.dropdown_menu_item, result);
            mAthleteActivity.setAdapter(adapter);
        }
    }

    private void Commit(){
        if(mValidator.validate()){
            updateAthleteReference((athlete) -> mAthleteModel.addAthlete(athlete));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            mAthlete = savedInstanceState.getParcelable(ATHLETE_TAG);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(ATHLETE_TAG, mAthlete);
    }
}
