package unipr.luc_af.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import unipr.luc_af.chronotracker.R;
import unipr.luc_af.classes.ActivitySession;
import unipr.luc_af.holders.ListViewHolder;

public class ActivitiesAdapter extends RecyclerView.Adapter<ListViewHolder> {

    private ActivitySession[] mActivitySessions;
    private ActivitiesListItemClick mItemClick;

    public ActivitiesAdapter() {
        mActivitySessions = new ActivitySession[0];
    }

    public ActivitiesAdapter(ActivitySession[] activitySessions, ActivitiesListItemClick onClick) {
        mActivitySessions = activitySessions;
        mItemClick = onClick;
    }

    public interface ActivitiesListItemClick{
        void onClick(View view, ActivitySession activitySession);
    }

    public void onActivityClick(View view, ActivitySession activitySession){
        if(mItemClick != null){
            mItemClick.onClick(view, activitySession);
        }
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View activitiesView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_list_item,parent,false);
        ListViewHolder viewHolder = new ListViewHolder(activitiesView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mActivitySessions.length;
    }
}
