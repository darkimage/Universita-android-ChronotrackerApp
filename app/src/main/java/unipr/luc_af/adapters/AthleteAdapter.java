package unipr.luc_af.adapters;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import unipr.luc_af.chronotracker.R;
import unipr.luc_af.classes.Athlete;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.holders.ListViewHolder;
import unipr.luc_af.services.Database;

public class AthleteAdapter extends RecyclerView.Adapter<ListViewHolder> {

    private Athlete[] mAthleteList;

    public AthleteAdapter(){
        mAthleteList = new Athlete[0];
    }

    public AthleteAdapter(Athlete[] athletes){
        mAthleteList = athletes;
    }

    public void setAthlets(Athlete[] athletes){
        mAthleteList = athletes;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View athleteView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.athelete_list_item,parent,false);
        athleteView.setOnClickListener((view) -> onAthleteClick());
        ListViewHolder viewHolder = new ListViewHolder(athleteView);
        return viewHolder;
    }

    public void onAthleteClick(){

    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        TextView athleteName = holder.itemView.findViewById(R.id.athlete_name);
        athleteName.setText(mAthleteList[position].name);
        TextView athleteSurname = holder.itemView.findViewById(R.id.athlete_surname);
        athleteSurname.setText(mAthleteList[position].surname);
        Chip athleteActivity = holder.itemView.findViewById(R.id.athlete_activity);
        DatabaseResult activitiesResult = (cursor)->{
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {
                if(mAthleteList[position].activityReference == cursor.getLong(0)) {
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
