package eu.ase.acs.eventsappui.api;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import eu.ase.acs.eventsappui.MainActivity;

public class ApiViewModelFactory implements ViewModelProvider.Factory{
    private ApiService apiService;
    private Activity activity;
    public ApiViewModelFactory(ApiService apiService, Activity activity) {
        this.apiService = apiService;
        this.activity = activity;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ApiViewModel(apiService, activity);
    }
}
