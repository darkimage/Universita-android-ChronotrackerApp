package unipr.luc_af.holders;

import android.view.View;
import android.widget.CompoundButton;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import unipr.luc_af.chronotracker.R;


public class CheckboxListViewHolder extends RecyclerView.ViewHolder {
    public CompoundButton checkBox;

    public CheckboxListViewHolder(@NonNull View itemView, FragmentActivity context, CheckboxInitListener init ) {
        super(itemView);
        checkBox = itemView.findViewById(R.id.dialog_item_checkbox);
        if(init != null) {
            init.onInit(itemView, checkBox);
        }
    }

    public interface CheckboxInitListener {
        void onInit(View view, CompoundButton check);
    }
}
