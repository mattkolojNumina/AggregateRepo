package com.numinagroup.androidtcpclient.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.numinagroup.androidtcpclient.R;
import com.numinagroup.androidtcpclient.StringManager;
import com.numinagroup.androidtcpclient.databinding.FragmentLoginBinding;
import com.numinagroup.androidtcpclient.viewmodel.MainViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    @Inject
    StringManager stringManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_login, container, false);

        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        viewModel.getLoginFailureEvent().observe(getViewLifecycleOwner(), message -> {
            binding.operatorIdEditTextLayout.setError(message != null ? message : getString(R.string.login_failure_error_text));
            Log.d("LoginFragment", "got message: " + message);
        });
        viewModel.getLoginEvent().observe(getViewLifecycleOwner(), v -> {
            binding.operatorIdEditTextLayout.setError(null);
            Log.d("LoginFragment", "got login event");
        });
        binding.setViewModel(viewModel);
        try {
            PackageInfo pInfo = requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0);
            String version = stringManager.getString(stringManager.VERSION_COLON_KEY) + " " + pInfo.versionName;
            binding.versionNumber.setText(version);
            binding.deviceID.setText("Device ID: "+ viewModel.getDeviceID());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}