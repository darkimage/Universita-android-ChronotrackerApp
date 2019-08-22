package unipr.luc_af.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import unipr.luc_af.chronotracker.R;
import unipr.luc_af.classes.ActivitySport;
import unipr.luc_af.holders.CheckboxListViewHolder;
import unipr.luc_af.models.ActivitySessionModel;

public class DialogActivitySportAdapter extends RecyclerView.Adapter<CheckboxListViewHolder> {
    private ActivitySport[] mActivitySports = new ActivitySport[0];
    private ActivitySessionModel mActivitySessionModel;
    private FragmentActivity mContext;

    public DialogActivitySportAdapter(FragmentActivity context) {
        mContext = context;
    }

    public DialogActivitySportAdapter(FragmentActivity context, ActivitySport[] activitySports) {
        mContext = context;
        mActivitySports = activitySports;
    }

    @NonNull
    @Override
    public CheckboxListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View dialogActivityItem = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dialog_activity_list_item,parent,false);

        mActivitySessionModel = new ViewModelProvider(mContext).get(ActivitySessionModel.class);
        CheckboxListViewHolder.CheckboxInitListener initListener = (view,check) ->
                uncheckOthersIfSelectionChange(mContext,mActivitySessionModel,view,check);
        return new CheckboxListViewHolder(dialogActivityItem,mContext,initListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckboxListViewHolder holder, int position) {
        holder.itemView.setTag(mActivitySports[position]);
        RelativeLayout container = holder.itemView.findViewById(R.id.dialog_item_container);
        container.setOnClickListener((view) -> onContainerClick(view));
        CompoundButton check = holder.checkBox;
        check.setOnCheckedChangeListener((button,state) -> onCheckedChanged(button,state, mActivitySports[position] ));
        check.setText(mActivitySports[position].name);
    }

    private void onCheckedChanged(CompoundButton button, boolean state, ActivitySport activitySport){
        if(state) {
            mActivitySessionModel.setDialogSelectedActivity(activitySport);
        }
    }

    private void onContainerClick(View view){
        CompoundButton check = view.findViewById(R.id.dialog_item_checkbox);
        check.performClick();
    }

    private void uncheckOthersIfSelectionChange(FragmentActivity context, ActivitySessionModel activitySessionModel, View view, CompoundButton check){
        final Observer<ActivitySport> mSelectedActivity = (activity) -> {
            ActivitySport viewActivitySport = (ActivitySport)view.getTag();
            if(activity == null) return;
            if(viewActivitySport == null) return;
            if (!activity.id.equals(viewActivitySport.id) && check.isChecked()) {
                check.setChecked(false);
            }
        };
        mActivitySessionModel.getDialogSelectedActivity().observe(mContext,mSelectedActivity);
    }

    @Override
    public int getItemCount() {
        return mActivitySports.length;
    }
}
