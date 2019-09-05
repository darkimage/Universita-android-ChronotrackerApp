package unipr.luc_af.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.concurrent.TimeUnit;

import unipr.luc_af.chronotracker.R;
import unipr.luc_af.chronotracker.helpers.Database;
import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.classes.Lap;
import unipr.luc_af.database.interfaces.DatabaseResult;
import unipr.luc_af.holders.ListViewHolder;

public class ActivitySessionAdapter extends RecyclerView.Adapter<ListViewHolder> {

    private ActivitySession[] mActivitySessions = new ActivitySession[0];
    private ActivitiesListItemClick mItemClick = (a, b) -> {
    };


    public ActivitySessionAdapter() {
    }

    public ActivitySessionAdapter(ActivitiesListItemClick onClick) {
        mItemClick = onClick;
    }

    public ActivitySessionAdapter(ActivitySession[] activitySessions, ActivitiesListItemClick onClick) {
        mActivitySessions = activitySessions;
        mItemClick = onClick;
    }

    public interface ActivitiesListItemClick {
        void onClick(View view, ActivitySession activitySession);
    }

    public void setActivitySessions(ActivitySession[] activitySessions) {
        mActivitySessions = activitySessions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View activitiesView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleview_activity_item, parent, false);
        return new ListViewHolder(activitiesView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        ActivitySession currentSession = mActivitySessions[position];
        TextView activityName = holder.itemView.findViewById(R.id.activities_list_activity_name);
        holder.itemView.setOnClickListener((v) -> mItemClick.onClick(v, mActivitySessions[position]));
        DatabaseResult activityNameResult = (cursor) -> {
            cursor.moveToNext();
            activityName.setText(cursor.getString(1));
        };
        Database.getInstance().getActivityFromId(currentSession.activity, activityNameResult);
        TextView duration = holder.itemView.findViewById(R.id.activities_list_elapsed_time);
        duration.setText(formatTime(currentSession.stopTime - currentSession.startTime, true));
        TextView lapsTextView = holder.itemView.findViewById(R.id.activities_list_laps);

        DatabaseResult lapsResult = cursor -> {
            cursor.moveToNext();
            Lap[] laps = new Lap[cursor.getCount()];
            for (int j = 0; j < cursor.getCount(); j++) {
                laps[j] = new Lap(cursor.getLong(1), cursor.getLong(2));
                cursor.moveToNext();
            }
            currentSession.laps = laps;
            lapsTextView.setText(String.valueOf(currentSession.laps.length));
        };
        Database.getInstance().getLapsOfSession(currentSession, lapsResult);

    }

    static String formatTime(long millisec, boolean useMillisec) {
        long hours = TimeUnit.MILLISECONDS.toHours(millisec);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millisec) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millisec) % 60;
        if (!useMillisec) {
            if (hours == 0) {
                return String.format("%02d:%02d", minutes, seconds);
            } else {
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
        } else {
            if (hours == 0) {
                return String.format("%02d:%02d.%03d", minutes, seconds, millisec % 1000);
            } else {
                return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millisec % 1000);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mActivitySessions.length;
    }
}
