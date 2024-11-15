package com.numinagroup.androidtcpclient.fragments.tabLayout;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.slider.Slider;
import com.numinagroup.androidtcpclient.R;
import com.numinagroup.androidtcpclient.StringManager;
import com.numinagroup.androidtcpclient.databinding.FragmentOptionsBinding;
import com.numinagroup.androidtcpclient.viewmodel.MainViewModel;
import com.numinagroup.androidtcpclient.viewmodel.OptionsViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OptionsFragment extends Fragment {

    OptionsViewModel optionsViewModel;
    MainViewModel mainViewModel;
    FragmentOptionsBinding binding;

    @Inject
    StringManager stringManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_options, container, false);

        optionsViewModel = new ViewModelProvider(requireActivity()).get(OptionsViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding.setOptionsViewModel(optionsViewModel);
        binding.setLifecycleOwner(this);
        binding.sensitivitySlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                optionsViewModel.updateSensitivity(slider.getValue());
                mainViewModel.stopListeningAndClearGrammars();
                mainViewModel.initPicoResources(slider.getValue(), true, true);
            }
        });
        binding.volumeSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                optionsViewModel.updateVolume(slider.getValue());
            }
        });
        binding.speedSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                optionsViewModel.updateRate(slider.getValue());
            }
        });
        binding.pitchSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {}

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                optionsViewModel.updatePitch(slider.getValue());
            }
        });


        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            String version = optionsViewModel.getString(stringManager.VERSION_COLON_KEY) + " " + pInfo.versionName;
            binding.versionNumber.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}