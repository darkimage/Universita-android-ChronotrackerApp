package unipr.luc_af.chronotracker;


import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class SessionTracker extends Fragment {
    AnimatedVectorDrawable mStartToStopAnim;
    AnimatedVectorDrawable mStopToStartAnim;
    Boolean startStopButtonState = false;

    public SessionTracker() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_session_tracker, container, false);
        mStartToStopAnim = (AnimatedVectorDrawable)getActivity().getDrawable(R.drawable.pause_to_start_anim);
        mStopToStartAnim = (AnimatedVectorDrawable)getActivity().getDrawable(R.drawable.start_to_pause_anim);

        ImageButton startStopButton = view.findViewById(R.id.tracker_start_pause);
        startStopButton.setOnClickListener((v) -> {
            if(startStopButtonState){
                startStopButton.setImageDrawable(mStopToStartAnim);
            }else{
                startStopButton.setImageDrawable(mStartToStopAnim);
            }
            ((AnimatedVectorDrawable)startStopButton.getDrawable()).start();
            startStopButtonState = !startStopButtonState;
        });
        return view;
    }

}
