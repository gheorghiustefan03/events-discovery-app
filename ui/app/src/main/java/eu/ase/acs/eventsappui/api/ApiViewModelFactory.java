package eu.ase.acs.eventsappui.api;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import eu.ase.acs.eventsappui.MainActivity;

public class ApiViewModelFactory implements ViewModelProvider.Factory{
    private ApiService apiService;
    private MainActivity mainActivity;
    public ApiViewModelFactory(ApiService apiService, MainActivity mainActivity) {
        this.apiService = apiService;
        this.mainActivity = mainActivity;
    }
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ApiViewModel(apiService, mainActivity);
    }
}
