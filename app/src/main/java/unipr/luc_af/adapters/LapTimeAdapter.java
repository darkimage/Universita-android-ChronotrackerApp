package unipr.luc_af.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.concurrent.TimeUnit;
import unipr.luc_af.chronotracker.R;
import unipr.luc_af.classes.Lap;
import unipr.luc_af.holders.ListViewHolder;

public class LapTimeAdapter extends RecyclerView.Adapter<ListViewHolder> {

    private Lap[] mLaps;
    private Context mContext;

    public LapTimeAdapter(Lap[] laps, Context context){
        mLaps = laps;
        mContext = context;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View lapView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycleview_lap_item,parent,false);
        return new ListViewHolder(lapView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        Lap currentLap = mLaps[position];
        View view = holder.itemView;
        TextView lapText = view.findViewById(R.id.lap_item_text);
        lapText.setText(mContext.getString(R.string.lap_placeholder, String.valueOf(mLaps.length - position)));

        TextView currentText = view.findViewById(R.id.current_item_text);
        currentText.setText(formatTime(currentLap.fromStart));

        TextView durationText = view.findViewById(R.id.duration_item_text);
        durationText.setText(mContext.getString(R.string.duration_placeholder, formatTime(currentLap.duration) ));
    }

    public void setLaps(Lap[] laps){
        mLaps = laps;
    }

    private String formatTime(long time){
        long hours = TimeUnit.MILLISECONDS.toHours(time);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;
        if(hours != 0) {
            return String.format(
                    "%02d:%02d:%02d.%03d",
                    hours, minutes, seconds, time % 1000);
        }else{
            return String.format(
                    "%02d:%02d.%03d",
                    minutes, seconds, time % 1000);
        }
    }

    @Override
    public int getItemCount() {
        return mLaps.length;
    }
}
