package eu.ase.acs.eventsappui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.threeten.bp.LocalDateTime;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import eu.ase.acs.eventsappui.entities.Category;
import eu.ase.acs.eventsappui.entities.Event;
import eu.ase.acs.eventsappui.entities.Location;

public class MainActivity extends AppCompatActivity {
    private static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque sit amet maximus purus, id sodales lorem. Sed velit ipsum, viverra vitae convallis fringilla, accumsan ac leo. Nulla aliquam at nulla sit amet ultricies. In et libero fringilla, gravida mi vel, tempus mauris. Vivamus ultrices, leo quis eleifend placerat, libero turpis mattis orci, vel auctor quam lacus id dui. Donec non ligula enim. Aliquam eget felis purus. Curabitur eget ex nisl. ";
    private BottomNavigationView nvMain;
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Toast.makeText(this, R.string.location_permission_warning, Toast.LENGTH_SHORT).show();
        } else {
            run();
        }
    }

    private void run() {
        FusedLocationProviderClient clientLocation = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Task<android.location.Location> locationResult = clientLocation.getLastLocation();
        locationResult.addOnSuccessListener(location -> {
            if (location != null) {
                userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                Log.e("USER LOCATION", location.getLatitude() + " " +  location.getLongitude());
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

                scanForEvents();

                Fragment homeFragment = new HomeFragment();
                Fragment searchFragment = new SearchFragment();
                Fragment mapFragment = new MapFragment();
                Fragment settingsFragment = new SettingsFragment();
                setCurrentFragment(homeFragment, true);

                resetBackgrounds(nvMain);
                setItemBackground(nvMain, nvMain.getSelectedItemId(), R.drawable.nav_item_selected_background);
                nvMain.setOnItemSelectedListener(item -> {
                    resetBackgrounds(nvMain);
                    setItemBackground(nvMain, item.getItemId(), R.drawable.nav_item_selected_background);
                    int itemId = item.getItemId();
                    if (itemId == R.id.home)
                        getSupportFragmentManager().popBackStack();
                    else if (itemId == R.id.search)
                        setCurrentFragment(searchFragment, false);
                    else if (itemId == R.id.map)
                        setCurrentFragment(mapFragment, false);
                    else if (itemId == R.id.settings)
                        setCurrentFragment(settingsFragment, false);
                    return true;
                });
            }
        });

    }

    private void initComponents() {
        nvMain = findViewById(R.id.nv_main);
    }

    private void resetBackgrounds(BottomNavigationView bottomNavigationView) {
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            MenuItem item = bottomNavigationView.getMenu().getItem(i);
            setItemBackground(nvMain, item.getItemId(), R.color.navbar_background);
        }
    }


    public void scanForEvents() {
        getRecommendedCategories();
        getAllLocations();
        getAllRecommendedEvents();
        getSavedEvents();
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

    public void getAllRecommendedEvents() {
        allEvents.clear();
        recommendedIndices.clear();
        for (int i = 0; i < 100; i++) {

            Category[] categories = Category.values();
            Random random = new Random();
            int nrCategories = 1;
            int startMonth = 1, startDay = 1;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                nrCategories = random.nextInt(1, 5);
                startMonth = random.nextInt(3, 8);
                startDay = random.nextInt(1, 30);
            }
            List<Category> chosenCategories = new ArrayList<>(nrCategories);
            for (int j = 0; j < nrCategories; j++) {
                Category category = categories[random.nextInt(categories.length)];
                if (!chosenCategories.contains(category))
                    chosenCategories.add(category);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Event event = new Event(i, "Event " + (i + 1), LOREM_IPSUM,
                        allLocations.get(random.nextInt(allLocations.size())), chosenCategories,
                        List.of("https://picsum.photos/1920/1080", "https://picsum.photos/1920/1080"),
                        "https://www.google.com", LocalDateTime.of(2025, startMonth, startDay, 12, 0),
                        LocalDateTime.of(2025, 8, 12, 12, 0));
                allEvents.add(event);
                recommendedIndices.put(event.getId(), i);
            }
        }
    }

    public void getAllLocations() {
        allLocations.clear();
        for (int i = 0; i < 10; i++) {
            double[] randomCoordinates;
            randomCoordinates = generateRandomCoordinate(userLocation.latitude, userLocation.longitude, radius / 1000);

            allLocations.add(new Location(i, "Location " + (i + 1), randomCoordinates[0], randomCoordinates[1]));
        }
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

    public void getRecommendedCategories() {
        recommendedCategories.clear();
        recommendedCategories = new ArrayList<>(4);
        Category[] categories = Category.values();
        Random random = new Random();
        for (int j = 0; j < 4; j++) {
            Category category = categories[random.nextInt(categories.length)];
            if (!recommendedCategories.contains(category))
                recommendedCategories.add(category);
            else j--;
        }
    }

    public List<Event> getRecommendedEventsForCategory(Category category) {
        List<Event> result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            result = allEvents.stream().filter(e -> e.getCategories().contains(category)).toList();
        }
        return result;
    }

    private double[] generateRandomCoordinate(double centerLat, double centerLong, double radiusInKm) {
        Random rand = new Random();
        final int EARTH_RADIUS = 6371;
        // Random distance and angle
        double angle = 2 * Math.PI * rand.nextDouble(); // Random angle
        double distance = radiusInKm * Math.sqrt(rand.nextDouble()); // Random distance within the radius

        // Convert to latitude and longitude differences
        double deltaLat = (distance / EARTH_RADIUS) * (180 / Math.PI); // Latitude difference
        double deltaLong = (distance / EARTH_RADIUS) * (180 / Math.PI) / Math.cos(Math.toRadians(centerLat)); // Longitude difference

        // Randomize direction within the circle (angle determines direction)
        double randomLat = centerLat + deltaLat * Math.sin(angle);
        double randomLong = centerLong + deltaLong * Math.cos(angle);

        return new double[]{randomLat, randomLong};
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