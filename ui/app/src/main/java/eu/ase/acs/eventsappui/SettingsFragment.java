package eu.ase.acs.eventsappui;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsFragment extends Fragment {
    private TextView tvSliderValue;
    private SeekBar sbRadius;
    private MainActivity mainActivity;

    public SettingsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        initComponents(view);
        mainActivity = (MainActivity) (requireActivity());
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("preferences", MODE_PRIVATE);
        mainActivity.radius = sharedPreferences.getLong("radius", 0);
        sbRadius.setProgress((int) (mainActivity.radius / 1000));
        tvSliderValue.setText(Long.toString(mainActivity.radius / 1000));
        sbRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong("radius", i * 1000L);
                editor.apply();
                mainActivity.radius = i * 1000L;
                tvSliderValue.setText(Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        return view;
    }

    private void initComponents(View view) {
        sbRadius = view.findViewById(R.id.sb_radius);
        tvSliderValue = view.findViewById(R.id.tv_slider_value);
    }

    @Override public void onStart() {
        super.onStart();
        SharedPreferences prefs = mainActivity.getSharedPreferences("preferences", MODE_PRIVATE);
        prefs.edit().putBoolean("isInSettings", true).apply();
        mainActivity.resetAllDataLoaded();
    }

    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = mainActivity.getSharedPreferences("preferences", MODE_PRIVATE);
        prefs.edit().putBoolean("isInSettings", false).apply();
    }
}