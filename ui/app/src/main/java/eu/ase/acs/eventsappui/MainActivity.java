package eu.ase.acs.eventsappui;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView nv_main;

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
        initComponents();

        Fragment homeFragment = new HomeFragment();
        Fragment searchFragment = new SearchFragment();
        Fragment mapFragment = new MapFragment();
        Fragment settingsFragment = new SettingsFragment();
        setCurrentFragment(homeFragment, true);

        resetBackgrounds(nv_main);
        setItemBackground(nv_main, nv_main.getSelectedItemId(), R.drawable.nav_item_selected_background);
        nv_main.setOnItemSelectedListener(item -> {
            resetBackgrounds(nv_main);
            setItemBackground(nv_main, item.getItemId(), R.drawable.nav_item_selected_background);
            int itemId = item.getItemId();
            if(itemId == R.id.home)
                getSupportFragmentManager().popBackStack();
            else if(itemId == R.id.search)
                setCurrentFragment(searchFragment, false);
            else if(itemId == R.id.map)
                setCurrentFragment(mapFragment, false);
            else if(itemId == R.id.settings)
                setCurrentFragment(settingsFragment, false);
            return true;
        });
    }
    private void initComponents(){
        nv_main = findViewById(R.id.nv_main);
    }
    private void resetBackgrounds(BottomNavigationView bottomNavigationView) {
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            MenuItem item = bottomNavigationView.getMenu().getItem(i);
            setItemBackground(nv_main, item.getItemId(), R.color.navbar_background);
        }
    }
    private void setItemBackground(BottomNavigationView bnv, int itemId, int backgroundId){
        bnv.findViewById(itemId).setBackgroundResource(backgroundId);
    }
    public void setCurrentFragment(Fragment fragment, boolean animate) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fl_main);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(animate && !(fragment instanceof HomeFragment)){
            transaction.setCustomAnimations(R.anim.enter_down, R.anim.exit_down);
        }
        else if(animate){
            transaction.setCustomAnimations(R.anim.enter_up, R.anim.exit_up);
        }
        transaction.replace(R.id.fl_main, fragment);
        if(currentFragment instanceof EventListFragment
        || currentFragment instanceof HomeFragment){
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
}