package com.numinagroup.androidtcpclient.fragments.tabLayout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.numinagroup.androidtcpclient.StringManager;
import com.numinagroup.androidtcpclient.databinding.FragmentLogBinding;
import com.numinagroup.androidtcpclient.viewmodel.MainViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LogFragment extends Fragment {

    FragmentLogBinding binding;
    private MainViewModel viewModel;

    @Inject
    StringManager stringManager;

    private synchronized void printToConsole(String input) {
        binding.consoleTextView.setText(viewModel.getMainViewUI().getConsoleText());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLogBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding.dumpLogsButton.setText(viewModel.getString(stringManager.DUMP_LOGS_BUTTON_LABEL_KEY));
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.getPrintToConsoleEvent().observe(getViewLifecycleOwner(), this::printToConsole);
        binding.dumpLogsButton.setOnClickListener(v -> viewModel.dumpLogs());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}