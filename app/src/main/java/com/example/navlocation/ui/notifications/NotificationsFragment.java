package com.example.navlocation.ui.notifications;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;


import com.example.navlocation.R;
import com.example.navlocation.databinding.FragmentNotificationsBinding;
import com.example.navlocation.ui.API.CitibikesResult;
import com.example.navlocation.ui.API.Station;
import com.example.navlocation.ui.API.ValenBisiApi;
import com.example.navlocation.ui.SharedViewModel;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    String mCurrentPhotoPath;
    private Uri photoURI;
    private ImageView foto;
    static final int REQUEST_TAKE_PHOTO = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        Context ctx = requireActivity().getApplicationContext();

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        return root;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
/*
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference base = FirebaseDatabase.getInstance().getReference();

        DatabaseReference users = base.child("users");
        DatabaseReference uid = users.child(auth.getUid());
        DatabaseReference incidencies = uid.child("incidencies");

        SharedViewModel model = new  ViewModelProvider(getActivity()).get(SharedViewModel.class);
*/

        final String BASE_URL = "http://api.citybik.es/v2/networks/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        ValenBisiApi valenBisiApi = retrofit.create(ValenBisiApi.class);

        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.setMultiTouchControls(true);
        IMapController mapController = binding.map.getController();
        mapController.setZoom(14.5);
        GeoPoint startPoint = new GeoPoint(39.4715612, -0.3930977);
        mapController.setCenter(startPoint);

        MyLocationNewOverlay myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(requireContext()), binding.map);
        myLocationNewOverlay.enableMyLocation();
        binding.map.getOverlays().add(myLocationNewOverlay);

        CompassOverlay compassOverlay = new CompassOverlay(requireContext(), new InternalCompassOrientationProvider(requireContext()), binding.map);
        compassOverlay.enableCompass();
        binding.map.getOverlays().add(compassOverlay);

        requestPermissionsIfNecessary(new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                }
        );

        valenBisiApi.status().enqueue(new Callback<CitibikesResult>() {

            @Override
            public void onResponse(Call<CitibikesResult> call, Response<CitibikesResult> response) {
                CitibikesResult result = response.body();
                List<Station> stations = result.getNetwork().getStations();

                Log.e("XXXXX", stations.toString());
                for (Station station : stations) {
                    Marker m = new Marker(binding.map);
                    m.setPosition(new GeoPoint(station.getLatitude(), station.getLongitude()));
                    m.setTextLabelFontSize(40);
                    m.setIcon(getResources().getDrawable(R.drawable.ic_notifications_black_24dp));
                    m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
                    m.setTitle(station.getName());
                    m.setSnippet(
                            String.format("%s llocs - %s disponibles", station.getFreeBikes() + station.getEmptySlots(), station.getFreeBikes())
                    );
                    binding.map.getOverlays().add(m);
                }

            }

            @Override
            public void onFailure(Call<CitibikesResult> call, Throwable t) {
                Log.e("XXXXX",call.toString());
                t.printStackTrace();
            }
        });
    }//oncreateviwed

    @Override
    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        binding.map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        binding.map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireActivity(), permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    requireActivity(),
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}