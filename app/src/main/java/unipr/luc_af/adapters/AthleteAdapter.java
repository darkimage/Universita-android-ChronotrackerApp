package unipr.luc_af.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import unipr.luc_af.chronotracker.R;
import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.holders.ListViewHolder;

public class AthleteAdapter extends RecyclerView.Adapter<ListViewHolder> {
    private AthleteListItemClick mItemClick = (view, item) -> {
    };
    private Athlete[] mAthleteList = new Athlete[0];

    public AthleteAdapter() {
    }

    public AthleteAdapter(Athlete[] athletes, AthleteListItemClick itemClick) {
        mItemClick = itemClick;
        mAthleteList = athletes;
    }

    public interface AthleteListItemClick {
        void onClick(View view, Athlete athlete);
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View athleteView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleview_athelete_item, parent, false);
        return new ListViewHolder(athleteView);
    }

    private void onAthleteClick(View view, Athlete athlete) {
        mItemClick.onClick(view, athlete);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        holder.itemView.setOnClickListener((view) -> onAthleteClick(view, mAthleteList[position]));
        TextView athleteName = holder.itemView.findViewById(R.id.athlete_name);
        athleteName.setText(mAthleteList[position].name);
        TextView athleteSurname = holder.itemView.findViewById(R.id.athlete_surname);
        athleteSurname.setText(mAthleteList[position].surname);
        Chip athleteActivity = holder.itemView.findViewById(R.id.athlete_activity);
        DatabaseResult activitiesResult = (cursor) -> {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                if (mAthleteList[position].activityReference == cursor.getLong(0)) {
                    athleteActivity.setText(cursor.getString(1));
                }
                cursor.moveToNext();
            }
        };
        Database.getInstance().getActivities(activitiesResult);
    }

    @Override
    public int getItemCount() {
        return mAthleteList.length;
    }
}
