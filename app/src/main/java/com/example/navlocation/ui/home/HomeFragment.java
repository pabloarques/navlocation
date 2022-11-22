package com.example.navlocation.ui.home;


import android.location.Location;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.navlocation.databinding.FragmentHomeBinding;
import com.example.navlocation.ui.SharedViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private ActivityResultLauncher<String[]> locationPermissionRequest;
    private boolean mTrackingLocation = false;
    private LocationCallback mLocationCallback;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedViewModel sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        SharedViewModel.getCurrentAddress().observe(getViewLifecycleOwner(), address -> {
            binding.localitzacio.setText(String.format(
                    "Direcció: %1$s \n Hora: %2$tr",
                    address, System.currentTimeMillis()));
        });
        sharedViewModel.getButtonText().observe(getViewLifecycleOwner(), s -> binding.buttonlocation.setText(s));
        sharedViewModel.getProgressBar().observe(getViewLifecycleOwner(), visible -> {
            if (visible)
                binding.loading.setVisibility(ProgressBar.VISIBLE);
            else
                binding.loading.setVisibility(ProgressBar.INVISIBLE);
        });

        binding.buttonlocation.setOnClickListener(view -> {
            Log.d("DEBUG", "Clicked Get Location");
            sharedViewModel.switchTrackingLocation();
        });

        final TextView textView = binding.localitzacio;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}