package eu.ase.acs.eventsappui.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import org.threeten.bp.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import eu.ase.acs.eventsappui.MainActivity;
import eu.ase.acs.eventsappui.entities.Category;
import eu.ase.acs.eventsappui.entities.Event;
import eu.ase.acs.eventsappui.entities.EventInteraction;
import eu.ase.acs.eventsappui.entities.Location;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiViewModel extends ViewModel {
    public class DataBundle{
        private final List<Category> categories;
        private final List<Event> events;
        private final List<Location> locations;
        private final Map<Integer, Integer> indices;
        public DataBundle(List<Category> categories, List<Event> events, List<Location> locations, Map<Integer, Integer> indices) {
            this.categories = categories;
            this.events = events;
            this.locations = locations;
            this.indices = indices;
        }
        public List<Category> getCategories() {
            return categories;
        }
        public List<Event> getEvents() {
            return events;
        }
        public List<Location> getLocations() {
            return locations;
        }
        public Map<Integer, Integer> getIndices() {
            return indices;
        }
    }
    private ApiService apiService;
    private MutableLiveData<List<Category>> categoriesLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Event>> eventsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<Location>> locationsLiveData = new MutableLiveData<>();
    private MutableLiveData<DataBundle> combinedLiveData = new MediatorLiveData<>();
    private MutableLiveData<Map<Integer, Integer>> indicesLiveData = new MutableLiveData<>();
    private Activity activity;
    public ApiViewModel(ApiService apiService, Activity activity) {
        this.apiService = apiService;
        this.activity = activity;
    }
    public MutableLiveData<List<Category>> getCategoriesLiveData() {
        return categoriesLiveData;
    }
    public MutableLiveData<List<Event>> getEventsLiveData() {
        return eventsLiveData;
    }
    public MutableLiveData<List<Location>> getLocationsLiveData() {
        return locationsLiveData;
    }
    public MutableLiveData<Map<Integer, Integer>> getIndicesLiveData() {
        return indicesLiveData;
    }
    public MutableLiveData<DataBundle> getCombinedLiveData() {
        return combinedLiveData;
    }
    public void combineData(){
        if(categoriesLiveData.getValue() != null && eventsLiveData.getValue() != null && locationsLiveData.getValue() != null && indicesLiveData != null) {
            DataBundle dataBundle = new DataBundle(categoriesLiveData.getValue(), eventsLiveData.getValue(), locationsLiveData.getValue(), indicesLiveData.getValue());
            combinedLiveData.postValue(dataBundle);
        }
    }
    public void sendEventInteraction(String deviceId, int eventId, String interactionType, LocalDateTime timestamp) {
        EventInteraction eventInteraction = new EventInteraction(deviceId, eventId, interactionType, timestamp.toString());
        Gson gson = new Gson();
        Log.d("API_REQUEST", gson.toJson(eventInteraction));
        apiService.postEventInteraction(eventInteraction).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d("API", "Event interaction posted successfully");
                } else {
                    Log.e("API", "Failed to post event interaction: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("API", "Error posting event interaction", t);
            }
        });
    }
    public void fetchCategories(String deviceId, List<Integer> eventIds){
        Map<String, Object> body = new HashMap<>();
        body.put("deviceId", deviceId);
        body.put("availableEventIds", eventIds);

        apiService.getCategories(body).enqueue(new Callback<List<Integer>>() {
            @Override
            public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    categoriesLiveData.postValue(response.body().stream().map(Category::fromInt).collect(Collectors.toList()));
                    //Log.e("CATEGORIES", categoriesLiveData.getValue().toString());
                }
                else {
                    Log.e("SERVER ERROR", "Failed to fetch categories");
                }
            }
            @Override
            public void onFailure(Call<List<Integer>> call, Throwable t) {
                Log.e("SERVER ERROR", "Failed to fetch categories", t);
            }
        });
    }
    public void fetchEvents(List<Integer> locationIds) {
        apiService.getEvents(locationIds).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONArray jsonArray = new JSONArray(jsonResponse);
                        List<Event> events = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            // Parse the JSON object manually
                            int id = jsonObject.getInt("id");
                            String name = jsonObject.getString("name");
                            String description = jsonObject.getString("description");
                            JSONArray categoriesJSON = jsonObject.getJSONArray("categories");
                            List<Integer> categoriesInt = new ArrayList<>();
                            for(int j = 0; j < categoriesJSON.length(); j++){
                                categoriesInt.add(categoriesJSON.getInt(j));
                            }
                            List<Category> categories = categoriesInt.stream()
                                    .map(Category::fromInt)
                                    .collect(Collectors.toList());
                            JSONArray imageUrlsJSON = jsonObject.getJSONArray("imageUrls");
                            List<String> imageUrls = new ArrayList<>();
                            for(int j = 0; j < imageUrlsJSON.length(); j++){
                                imageUrls.add(imageUrlsJSON.getString(j));
                            }
                            String link = jsonObject.getString("link");
                            @SuppressLint("NewApi") LocalDateTime startDate = LocalDateTime.parse(jsonObject.getString("startDate"));
                            @SuppressLint("NewApi") LocalDateTime endDate = LocalDateTime.parse(jsonObject.getString("endDate"));
                            Location location = ((MainActivity)activity).allLocations.stream()
                                    .filter(l -> {
                                        try {
                                            return l.getId() == jsonObject.getInt("locationId");
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    })
                                    .collect(Collectors.toList()).get(0);
                            // Add other fields as needed
                            Event event = new Event(id, name, description, location, categories, imageUrls, link, startDate, endDate);

                            events.add(event);
                            ((MainActivity)activity).recommendedIndices.put(event.getId(), i);
                        }
                        eventsLiveData.postValue(events);
                    } catch (IOException | JSONException e) {
                        Log.e("PARSING ERROR", "Failed to parse events", e);
                    }
                } else {
                    Log.e("SERVER ERROR", "Failed to fetch events");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("SERVER ERROR", "Failed to fetch events", t);
            }
        });
    }
    public void fetchLocations(double userLat, double userLon, long radius){
        apiService.getLocations(userLat, userLon, radius).enqueue(new Callback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    locationsLiveData.postValue(response.body());
                    //Log.e("LOCATIONS", locationsLiveData.getValue().toString());
                }
                else {
                    Log.e("SERVER ERROR", "Failed to fetch locations");
                }
            }
            @Override
            public void onFailure(Call<List<Location>> call, Throwable t) {
                Log.e("SERVER ERROR", "Failed to fetch locations");
            }
        });
    }
    public void fetchIndices(String deviceId, List<Integer> eventIds) {
        Map<String, Object> body = new HashMap<>();
        body.put("deviceId", deviceId);
        body.put("availableEventIds", eventIds);
        System.out.println(deviceId);
        System.out.println(eventIds);
        apiService.getEventIndices(body).enqueue(new Callback<Map<Integer, Integer>>() {
            @Override
            public void onResponse(Call<Map<Integer, Integer>> call, Response<Map<Integer, Integer>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    indicesLiveData.postValue(response.body());
                    Log.d("API", "Indices fetched successfully: " + response.body());
                } else {
                    Log.e("API", "Failed to fetch indices: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Map<Integer, Integer>> call, Throwable t) {
                Log.e("API", "Error fetching indices", t);
            }

        });
    }

}