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
import unipr.luc_af.classes.ActivitySportSpecialization;
import unipr.luc_af.holders.CheckboxListViewHolder;
import unipr.luc_af.models.ActivitySessionModel;

public class DialogActivityTypesAdapter  extends RecyclerView.Adapter<CheckboxListViewHolder> {
    private ActivitySportSpecialization[] mActivitySportsType = new ActivitySportSpecialization[0];
    private ActivitySessionModel mActivitySessionModel;
    private FragmentActivity mContext;

    public DialogActivityTypesAdapter(FragmentActivity context){
        mContext = context;
    }

    public DialogActivityTypesAdapter(FragmentActivity context,ActivitySportSpecialization[] activitySportSpecializations) {
        mContext = context;
        mActivitySportsType = activitySportSpecializations;
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
        holder.itemView.setTag(mActivitySportsType[position]);
        RelativeLayout container = holder.itemView.findViewById(R.id.dialog_item_container);
        container.setOnClickListener((view) -> onContainerClick(view));
        CompoundButton check = holder.checkBox;
        check.setOnCheckedChangeListener((button,state) -> onCheckedChanged(button,state, mActivitySportsType[position] ));
        check.setText(mActivitySportsType[position].name);
    }

    private void onCheckedChanged(CompoundButton button, boolean state, ActivitySportSpecialization activitySportType){
        if(state) {
            mActivitySessionModel.setDialogSelectedActivityType(activitySportType);
        }
    }

    private void onContainerClick(View view){
        CompoundButton check = view.findViewById(R.id.dialog_item_checkbox);
        check.performClick();
    }

    private void uncheckOthersIfSelectionChange(FragmentActivity context, ActivitySessionModel activitySessionModel, View view, CompoundButton check){
        final Observer<ActivitySportSpecialization> mSelectedActivity = (activity) -> {
            ActivitySportSpecialization viewActivityType = (ActivitySportSpecialization)view.getTag();
            if(activity == null) return;
            if(viewActivityType == null) return;
            if (!activity.id.equals(viewActivityType.id) && check.isChecked()) {
                check.setChecked(false);
            }
        };
        mActivitySessionModel.getDialogSelectedActivityType().observe(mContext,mSelectedActivity);
    }

    @Override
    public int getItemCount() {
        return mActivitySportsType.length;
    }
}
