package unipr.luc_af.chronotracker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import unipr.luc_af.database.DatabaseResult;
import unipr.luc_af.services.Database;

public class addAthlete extends Fragment {
    public addAthlete() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_athlete, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        DatabaseResult activities = new DatabaseResult() {
            @Override
            public void OnResult(Cursor cursor) {
                String[] result = new String[cursor.getCount()];
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    result[i] = cursor.getString(0);
                    cursor.moveToNext();
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.dropdown_menu_item, result);
                AutoCompleteTextView editTextFilledExposedDropdown = getActivity().findViewById(R.id.filled_exposed_dropdown);
                editTextFilledExposedDropdown.setAdapter(adapter);
            }
        };
        Database.getInstance().getActivityNames(activities);
    }


}
