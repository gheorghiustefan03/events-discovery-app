package eu.ase.acs.eventsappui;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import eu.ase.acs.eventsappui.api.ApiService;
import eu.ase.acs.eventsappui.api.ApiViewModel;
import eu.ase.acs.eventsappui.api.ApiViewModelFactory;
import eu.ase.acs.eventsappui.entities.Category;
import eu.ase.acs.eventsappui.entities.Event;
import eu.ase.acs.eventsappui.entities.Location;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet maximus purus, id sodales lorem. Sed velit ipsum, viverra vitae convallis fringilla, accumsan ac leo. Nulla aliquam at nulla sit amet ultricies. In et libero fringilla, gravida mi vel, tempus mauris. Vivamus ultrices, leo quis eleifend placerat, libero turpis mattis orci, vel auctor quam lacus id dui. Donec non ligula enim. Aliquam eget felis purus. Curabitur eget ex nisl. ";
    public BottomNavigationView nvMain;
    public List<Event> allEvents = new ArrayList<>();
    public List<Event> savedEvents = new ArrayList<>();
    public List<Location> allLocations = new ArrayList<>();
    List<Category> recommendedCategories = new ArrayList<>();
    public LatLng userLocation = null;
    public long radius;
    private SharedPreferences sharedPreferences;
    public Map<Integer, Integer> recommendedIndices = new HashMap<>();
    public String savedEventsSorting = "Recommended";
    public String allEventsSorting = "Recommended";
    private ApiService apiService;
    private ApiViewModel apiViewModel;
    private MediatorLiveData<Boolean> allDataFetched = new MediatorLiveData<>();
    private ViewFlipper viewFlipper;

    public LiveData<Boolean> getAllDataLoaded() {
        return allDataFetched;
    }

    public void resetAllDataLoaded() {
        allDataFetched.setValue(false);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Toast.makeText(this, R.string.location_permission_warning, Toast.LENGTH_SHORT).show();
        } else {
            run();
        }
    }

    private void run() {
        FusedLocationProviderClient clientLocation = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<android.location.Location> locationResult = clientLocation.getLastLocation();

            locationResult.addOnSuccessListener(location -> {
                if (location != null) {
                    userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.e("Location", "User location: " + userLocation.latitude + ", " + userLocation.longitude);
                    initComponents();
                    sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (!sharedPreferences.contains("radius")) {
                        editor.putLong("radius", 10000);
                        editor.apply();
                    }
                    if (!sharedPreferences.contains("saved_events_size")) {
                        editor.putInt("saved_events_size", 0);
                        editor.apply();
                    }
                    radius = sharedPreferences.getLong("radius", 0);

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("http://10.0.2.2:5073/api/")  // Base URL
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    apiService = retrofit.create(ApiService.class);
                    ApiViewModelFactory factory = new ApiViewModelFactory(apiService, this);
                    apiViewModel = new ViewModelProvider(this, factory).get(ApiViewModel.class);

                    scanForEvents(R.id.home);
                }
            });
    }

    private void onDataFetched(int selectedItemId) {
        System.out.println(recommendedIndices);

        Fragment homeFragment = new HomeFragment();
        Fragment searchFragment = new SearchFragment();
        Fragment mapFragment = new MapFragment();
        Fragment settingsFragment = new SettingsFragment();
        if(selectedItemId == R.id.home)
            setCurrentFragment(homeFragment, false);
        else if(selectedItemId == R.id.search)
            setCurrentFragment(searchFragment, false);
        else if(selectedItemId == R.id.map)
            setCurrentFragment(homeFragment, false);
        else if(selectedItemId == R.id.settings)
            setCurrentFragment(settingsFragment, false);

        resetBackgrounds(nvMain);
        setItemBackground(nvMain, nvMain.getSelectedItemId(), R.drawable.nav_item_selected_background);
        nvMain.setOnItemSelectedListener(item -> {
            boolean inSettings = sharedPreferences.getBoolean("isInSettings", false);
            if(inSettings){
                scanForEvents(item.getItemId());
                return true;
            }
            resetBackgrounds(nvMain);
                setItemBackground(nvMain, item.getItemId(), R.drawable.nav_item_selected_background);

                    int itemId = item.getItemId();
                    if (itemId == R.id.home){
                        allDataFetched.observe(this, allFetched -> {
                            if (allFetched) {
                                setCurrentFragment(homeFragment, false);
                            }
                        });
                    }
                    else if (itemId == R.id.search){
                        allDataFetched.observe(this, allFetched -> {
                            if (allFetched) {
                                setCurrentFragment(searchFragment, false);
                            }
                        });
                    }
                    else if (itemId == R.id.map) {
                        allDataFetched.observe(this, allFetched -> {
                            if (allFetched) {
                                setCurrentFragment(mapFragment, false);
                            }
                        });
                    }
                    else if (itemId == R.id.settings) {
                        allDataFetched.observe(this, allFetched -> {
                            if (allFetched) {
                                setCurrentFragment(settingsFragment, false);
                            }
                        });
                    }
            return true;
        });
    }

    private void initComponents() {
        viewFlipper = findViewById(R.id.view_flipper);
        nvMain = findViewById(R.id.nv_main);
    }

    private void resetBackgrounds(BottomNavigationView bottomNavigationView) {
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            MenuItem item = bottomNavigationView.getMenu().getItem(i);
            setItemBackground(nvMain, item.getItemId(), R.color.navbar_background);
        }
    }

    private void showLoadingScreen() {
        viewFlipper.setDisplayedChild(0);
        nvMain.setVisibility(View.GONE);
    }

    private void hideLoadingScreen() {
        viewFlipper.setDisplayedChild(1);
        nvMain.setVisibility(View.VISIBLE);
        //initComponents();
    }


    public void scanForEvents(int selectedItemId) {
        apiViewModel.resetLiveData();

        showLoadingScreen();

        MutableLiveData<Boolean> categoriesFetched = new MutableLiveData<>(false);
        MutableLiveData<Boolean> locationsFetched = new MutableLiveData<>(false);
        MutableLiveData<Boolean> eventsFetched = new MutableLiveData<>(false);
        MutableLiveData<Boolean> indicesFetched = new MutableLiveData<>(false);

        apiViewModel.getCategoriesLiveData().observe(this, categories -> {
            if (categories != null) {
                recommendedCategories = categories;
                categoriesFetched.setValue(true);
            }
        });

        apiViewModel.getLocationsLiveData().observe(this, locations -> {
            if (locations != null) {
                allLocations = locations;
                locationsFetched.setValue(true);
            }
        });

        apiViewModel.getEventsLiveData().observe(this, events -> {
            if (events != null) {
                allEvents = events;
                eventsFetched.setValue(true);
            }
        });

        apiViewModel.getIndicesLiveData().observe(this, indices -> {
            if (indices != null) {
                recommendedIndices = indices;
                indicesFetched.setValue(true);
            }
        });

        // Fetch categories and locations in parallel
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        AtomicReference<List<Integer>> eventIds = new AtomicReference<>(new ArrayList<>());


        apiViewModel.fetchLocations(userLocation.latitude, userLocation.longitude, radius);

        // Observe locationsFetched to fetch events after locations are fetched
        locationsFetched.observe(this, fetched -> {
            if (fetched) {
                List<Integer> locationIds = allLocations.stream().map(Location::getId).collect(Collectors.toList());
                apiViewModel.fetchEvents(locationIds);
            }
        });

        eventsFetched.observe(this, fetched -> {
            if (fetched) {
                // Fetch indices after events are fetched
                eventIds.set(allEvents.stream().map(Event::getId).collect(Collectors.toList()));
                apiViewModel.fetchIndices(deviceId, eventIds.get());
            }
        });

        indicesFetched.observe(this, fetched -> {
            if (fetched) {
                apiViewModel.fetchCategories(deviceId, eventIds.get());
            }
        });

        // Observe all fetch statuses to continue the flow after all data is fetched
        allDataFetched = new MediatorLiveData<>();
        allDataFetched.addSource(categoriesFetched, value -> allDataFetched.setValue(
                categoriesFetched.getValue() && locationsFetched.getValue() && eventsFetched.getValue() && indicesFetched.getValue()));
        allDataFetched.addSource(locationsFetched, value -> allDataFetched.setValue(
                categoriesFetched.getValue() && locationsFetched.getValue() && eventsFetched.getValue() && indicesFetched.getValue()));
        allDataFetched.addSource(eventsFetched, value -> allDataFetched.setValue(
                categoriesFetched.getValue() && locationsFetched.getValue() && eventsFetched.getValue() && indicesFetched.getValue()));
        allDataFetched.addSource(indicesFetched, value -> allDataFetched.setValue(
                categoriesFetched.getValue() && locationsFetched.getValue() && eventsFetched.getValue() && indicesFetched.getValue()));

        allDataFetched.observe(this, allFetched -> {
            if (allFetched) {
                onDataFetched(selectedItemId);
                getSavedEvents();
                hideLoadingScreen();
            }
        });
    }

    private void setItemBackground(BottomNavigationView bnv, int itemId, int backgroundId) {
        bnv.findViewById(itemId).setBackgroundResource(backgroundId);
    }

    public void setCurrentFragment(Fragment fragment, boolean animate) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fl_main);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (animate && !(fragment instanceof HomeFragment)) {
            transaction.setCustomAnimations(R.anim.enter_down, R.anim.exit_down);
        } else if (animate) {
            transaction.setCustomAnimations(R.anim.enter_up, R.anim.exit_up);
        }
        transaction.replace(R.id.fl_main, fragment);
        if (currentFragment instanceof EventListFragment
                || currentFragment instanceof HomeFragment) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            // If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                run();
            } else {
                // Permission denied, show a message or handle the case
                System.exit(0);
            }
        }
    }

    public List<Event> getRecommendedEventsForCategory(Category category) {
        List<Event> result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            result = allEvents.stream().filter(e -> e.getCategories().contains(category)).toList();
        }
        return result;
    }


    void getSavedEvents() {
        savedEvents.clear();
        int size = sharedPreferences.getInt("saved_events_size", 0);
        for (int i = 0; i < size; i++) {
            int id = sharedPreferences.getInt("saved_events_" + i, 0);
            List<Event> event = allEvents.stream().filter(e -> e.getId() == id).collect(Collectors.toList());
            if (!event.isEmpty()) {
                savedEvents.add(event.get(0));
            }
        }
        savedEvents.sort(Comparator.comparingInt(e -> recommendedIndices.get(e.getId())));
    }
}