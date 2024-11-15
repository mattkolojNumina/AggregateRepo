package com.numinagroup.androidtcpclient.fragments;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.numinagroup.androidtcpclient.R;
import com.numinagroup.androidtcpclient.StringManager;
import com.numinagroup.androidtcpclient.databinding.FragmentMainBinding;
import com.numinagroup.androidtcpclient.fragments.tabLayout.InstructionsFragment;
import com.numinagroup.androidtcpclient.fragments.tabLayout.LogFragment;
import com.numinagroup.androidtcpclient.fragments.tabLayout.OptionsFragment;
import com.numinagroup.androidtcpclient.serviceobjects.ServerMessage;
import com.numinagroup.androidtcpclient.viewmodel.MainViewModel;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private MainViewModel viewModel;
    private final String OPTIONS_TAG = "optionsFragment";
    private final String INSTRUCTIONS_TAG = "instructionsFragment";
    private final String LOG_TAG = "logFragment";

    @Inject
    StringManager stringManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.getShowCameraScanEvent().observe(getViewLifecycleOwner(), shouldShowScanButton -> {
            Log.d("Scanner Init", "value: " + shouldShowScanButton);
            binding.scanButton.setVisibility(hasCamera() ? View.VISIBLE : View.INVISIBLE);
        });
        viewModel.getConnectedEvent().observe(getViewLifecycleOwner(), connected -> {
                    if (!connected) {
                        binding.connectionIndicator.setImageResource(R.drawable.circle_red);
                    }
                    binding.loadingIndicator.setVisibility(connected ? View.INVISIBLE : View.VISIBLE);
                    binding.onOffSwitch.setEnabled(connected);
                }
        );

        viewModel.getServerMessageEvent().observe(getViewLifecycleOwner(), this::showServerMessage);

        viewModel.getNetworkConnectionStrengthEvent().observe(getViewLifecycleOwner(), binding.connectionIndicator::setColorFilter);
        viewModel.getListeningNotificationEvent().observe(getViewLifecycleOwner(), listening -> binding.listeningIndicator.setVisibility(listening ? View.VISIBLE : View.INVISIBLE));
        viewModel.getEnableScanEvent().observe(getViewLifecycleOwner(), scanning -> {
            if (viewModel.getScanAhead()) {
                scanning = true;
            }
            binding.scanningIndicator.setVisibility(scanning ? View.VISIBLE : View.INVISIBLE);

        });
        binding.onOffSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.startStop(isChecked));

        viewModel.getLoginEvent().observe(getViewLifecycleOwner(), logIn -> showFragment(INSTRUCTIONS_TAG));

        Objects.requireNonNull(binding.tabLayout.getTabAt(0)).setText(viewModel.getString(stringManager.INFO_TAB_NAME_KEY));
        Objects.requireNonNull(binding.tabLayout.getTabAt(1)).setText(viewModel.getString(stringManager.LOG_TAB_NAME_KEY));
        Objects.requireNonNull(binding.tabLayout.getTabAt(2)).setText(viewModel.getString(stringManager.OPTION_TAB_NAME_KEY));
        binding.onOffSwitch.setTextOff(viewModel.getString(stringManager.OFF_KEY));
        binding.onOffSwitch.setTextOn(viewModel.getString(stringManager.ON_KEY));
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() != null) {
                    int currentTab = tab.getPosition();
                    switch (currentTab) {
                        case 0:
                            showFragment(INSTRUCTIONS_TAG);
                            break;
                        case 1:
                            showFragment(LOG_TAG);
                            break;
                        case 2:
                            showFragment(OPTIONS_TAG);
                        default:
                            break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        viewModel.getOnOffTriggerEvent().observe(getViewLifecycleOwner(), isOn -> binding.onOffSwitch.setChecked(isOn));
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;

    }


    private boolean hasCamera() {
        return requireContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void onCameraScanComplete() {
        binding.scanButton.setVisibility(View.INVISIBLE);
//        viewModel.stopScanning();
    }

    private void showFragment(String tag) {
        Class<? extends Fragment> fragmentClass;
        switch (tag) {
            case OPTIONS_TAG:
                fragmentClass = OptionsFragment.class;
                break;
            case INSTRUCTIONS_TAG:
                fragmentClass = InstructionsFragment.class;
                break;
            case LOG_TAG:
                fragmentClass = LogFragment.class;
                break;
            default:
                return;
        }
        getChildFragmentManager().beginTransaction().replace(binding.contentView.getId(), fragmentClass, null).commit();
    }

    /**
     * This method is called when we get a new server message from the server.
     * It gets the information of a server message and actually displays it
     * for the user. This method does a few things to the TextView. It displays the
     * message, sets the text color, sets the background color, and sets the
     * visibility of the textview so the user can see it. By default, the textview is
     * invisible until the first message is sent. Once we have the first message, the
     * textview will be visible. When we get another server message, we simply display
     * the new information in the textview.
     *
     * @param serverMessage This is the ServerMessage object that we want to display
     **/
    private void showServerMessage(ServerMessage serverMessage) {
        binding.tvServerMessage.setText(serverMessage.getMessageText());
        binding.tvServerMessage.setTextColor(serverMessage.getTextColor());
        binding.tvServerMessage.setBackgroundColor(serverMessage.getBackgroundColor());
        binding.tvServerMessage.setVisibility(View.VISIBLE);
    }
}

