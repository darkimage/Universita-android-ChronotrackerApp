package unipr.luc_af.chronotracker;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import unipr.luc_af.adapters.AthleteAdapter;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.models.TitleBarModel;
import unipr.luc_af.services.Database;

public class AthleteList extends Fragment {
    private String SCROLL_POS = "scroll_pos";
    private int mScrollPos = 0;
    private RecyclerView mAtheleteList;
    private FloatingActionButton mAddAthlete;
    private LinearLayoutManager mLayoutManager;
    private TitleBarModel mTitleModel;
    public AthleteList() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_layout, container, false);
        mAtheleteList = view.findViewById(R.id.recycle_list);
        // Performance extra se gli oggetti non cambiano il layout della RecycleView
        mAtheleteList.setHasFixedSize(true);

        // Aggiungiamo un standard linearlayout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAtheleteList.setLayoutManager(mLayoutManager);

        DatabaseResult athletesResult = (cursor)-> {
            Athlete[] athletes = getAthleteList(cursor);
            mAtheleteList.setAdapter(new AthleteAdapter(getActivity(),athletes, (v,athlete) -> onItemClick(v,athlete)));
            mAtheleteList.invalidate();
        };
        Database.getInstance().getAthletes(athletesResult);
        mAtheleteList.setAdapter(new AthleteAdapter(getActivity()));

        mAddAthlete = view.findViewById(R.id.add_fab);
        mAddAthlete.setOnClickListener((v) -> goToAddAthlete());
        return view;
    }


    public Athlete[] getAthleteList(Cursor cursor){
        cursor.moveToFirst();
        Athlete[] athletes = new Athlete[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            athletes[i] = new Athlete(
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getLong(3));
            cursor.moveToNext();
        }
        mAtheleteList.scrollToPosition(mScrollPos);
        return athletes;
    }

    private void onItemClick(View view, Athlete athlete){
        mScrollPos = mLayoutManager.findFirstVisibleItemPosition();
        getActivity().getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(
                    R.anim.horizontal_in_left,
                    R.anim.horizontal_out_left,
                    R.anim.horizontal_in,
                    R.anim.horizontal_out)
            .replace(R.id.root,new AthleteActivities())
            .addToBackStack(null)
            .commit();
    }

    private void goToAddAthlete(){
        mScrollPos = mLayoutManager.findFirstVisibleItemPosition();
        getActivity().getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(
                    R.anim.horizontal_in_left,
                    R.anim.horizontal_out_left,
                    R.anim.horizontal_in,
                    R.anim.horizontal_out)
            .replace(R.id.root,new AthleteAdd())
            .addToBackStack(null)
            .commit();
    }

    @Override
    public synchronized void onStart() {
        super.onStart();
        mTitleModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
        mTitleModel.setTitle(getActivity().getString(R.string.athlete_list));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(SCROLL_POS, mScrollPos);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            mScrollPos = savedInstanceState.getInt(SCROLL_POS);
        }
    }

}
