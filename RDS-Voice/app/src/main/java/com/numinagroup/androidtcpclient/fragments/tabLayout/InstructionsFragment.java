package com.numinagroup.androidtcpclient.fragments.tabLayout;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.numinagroup.androidtcpclient.R;
import com.numinagroup.androidtcpclient.databinding.FragmentInstructionsBinding;
import com.numinagroup.androidtcpclient.viewmodel.MainViewModel;
import com.numinagroup.androidtcpclient.viewmodel.OptionsViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class InstructionsFragment extends Fragment {

    FragmentInstructionsBinding binding;
    MainViewModel viewModel;
    OptionsViewModel optionsViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_instructions, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        optionsViewModel = new ViewModelProvider(requireActivity()).get(OptionsViewModel.class);
        binding.setOptionsViewModel(optionsViewModel);
        binding.setLifecycleOwner(this);
        viewModel.getCommandSentEvent().observe(getViewLifecycleOwner(),
                command -> {
                    binding.lastSentToServer.setText(command);
                    resetTextEntry();
                });
        viewModel.getPrintInstructionsEvent().observe(getViewLifecycleOwner(),
                instructions -> {
                    binding.instructionView.setText(instructions);
                    resetTextEntry();
                });
        binding.keyBoardButton.setOnClickListener(v -> {
            if (viewModel.getMainViewUI().getConnected().get()) {
                binding.textEntryCardView.setVisibility(View.VISIBLE);
                binding.operatorEntryEditText.requestFocus();
                binding.operatorEntryEditText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
                binding.operatorEntryEditText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
            }
        });
        binding.operatorEntryEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                final String text = v.getText().toString();
                if (!text.trim().isEmpty()) {
                    viewModel.sendVoiceResponse(text);
                }
            }
            resetTextEntry();
            return false;

        });
        return binding.getRoot();
    }

    private void resetTextEntry() {
        binding.operatorEntryEditText.setText("");
        binding.textEntryCardView.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.operatorEntryEditText.getWindowToken(), 0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}