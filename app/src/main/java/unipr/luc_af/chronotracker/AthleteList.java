package unipr.luc_af.chronotracker;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import unipr.luc_af.adapters.AthleteAdapter;
import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.chronotracker.helpers.Utils;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.models.AthleteModel;
import unipr.luc_af.models.PopupItemsModel;
import unipr.luc_af.models.TitleBarModel;

import static unipr.luc_af.chronotracker.MainActivity.ATHLETE_ACTIVITIES_LIST_TAG;
import static unipr.luc_af.chronotracker.MainActivity.ATHLETE_ADD_TAG;

public class AthleteList extends Fragment {
    private String SCROLL_POS = "scroll_pos";
    private int mScrollPos = 0;
    private RecyclerView mAthleteList;
    private FloatingActionButton mAddAthlete;
    private LinearLayoutManager mLayoutManager;
    private TitleBarModel mTitleModel;
    private AthleteModel mAthleteModel;
    private PopupItemsModel mPopupItemsModel;

    public AthleteList() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_athletes_list, container, false);
        Utils.getInstance().setToolBarNavigation((AppCompatActivity) getActivity());
        SetUpModels(savedInstanceState);
        SetUpUi(view, savedInstanceState);
        return view;
    }

    void SetUpModels(Bundle savedInstanceStat) {
        mPopupItemsModel = new ViewModelProvider(getActivity()).get(PopupItemsModel.class);
        mPopupItemsModel.setActiveItems(new int[0]);
    }

    void SetUpUi(View view, Bundle savedInstanceState) {
        mAthleteList = view.findViewById(R.id.recycle_list);
        // Performance extra se gli oggetti non cambiano il layout della RecycleView
        mAthleteList.setHasFixedSize(true);

        // Aggiungiamo un standard linearlayout manager
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAthleteList.setLayoutManager(mLayoutManager);

        DatabaseResult athletesResult = (cursor) -> {
            Athlete[] athletes = getAthleteList(cursor);
            mAthleteList.setAdapter(new AthleteAdapter(athletes, (v, athlete) -> onItemClick(v, athlete)));
            mAthleteList.invalidate();
            if (athletes.length == 0) {
                mAthleteList.setVisibility(View.GONE);
                TextView emptyMessage = view.findViewById(R.id.no_data_message);
                emptyMessage.setText(getActivity().getText(R.string.no_athlete_records));
                emptyMessage.setVisibility(View.VISIBLE);
            }
        };
        Database.getInstance().getAthletes(athletesResult);
        mAthleteList.setAdapter(new AthleteAdapter());

        mAddAthlete = view.findViewById(R.id.add_fab);
        mAddAthlete.setOnClickListener((v) -> goToAddAthlete());
    }


    private Athlete[] getAthleteList(Cursor cursor) {
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
        mAthleteList.scrollToPosition(mScrollPos);
        return athletes;
    }

    private void onItemClick(View view, Athlete athlete) {
        mScrollPos = mLayoutManager.findFirstVisibleItemPosition();
        mAthleteModel.selectAthlete(athlete);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        manager.beginTransaction()
                .setCustomAnimations(
                        R.anim.horizontal_in_left,
                        R.anim.horizontal_out_left,
                        R.anim.horizontal_in,
                        R.anim.horizontal_out)
                .replace(R.id.root, new AthleteActivities(), ATHLETE_ACTIVITIES_LIST_TAG)
                .addToBackStack(ATHLETE_ACTIVITIES_LIST_TAG)
                .commit();
        manager.executePendingTransactions();
    }

    private void goToAddAthlete() {
        mScrollPos = mLayoutManager.findFirstVisibleItemPosition();
        FragmentManager manger = getActivity().getSupportFragmentManager();
        manger.beginTransaction()
                .setCustomAnimations(
                        R.anim.horizontal_in_left,
                        R.anim.horizontal_out_left,
                        R.anim.horizontal_in,
                        R.anim.horizontal_out)
                .replace(R.id.root, new AthleteAdd(), ATHLETE_ADD_TAG)
                .addToBackStack(ATHLETE_ADD_TAG)
                .commit();
        manger.executePendingTransactions();
    }

    @Override
    public synchronized void onStart() {
        super.onStart();
        mAthleteModel = new ViewModelProvider(getActivity()).get(AthleteModel.class);
        mTitleModel = new ViewModelProvider(getActivity()).get(TitleBarModel.class);
        mTitleModel.setTitle(getActivity().getString(R.string.athlete_list));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(SCROLL_POS, mScrollPos);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mScrollPos = savedInstanceState.getInt(SCROLL_POS);
        }
    }
}
