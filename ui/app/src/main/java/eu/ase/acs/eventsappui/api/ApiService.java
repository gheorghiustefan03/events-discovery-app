package eu.ase.acs.eventsappui.api;

import java.util.List;

import eu.ase.acs.eventsappui.entities.Category;
import eu.ase.acs.eventsappui.entities.Event;
import eu.ase.acs.eventsappui.entities.Location;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("Event")
    Call<ResponseBody> getEvents(@Query("lId") List<Integer> locationIds);

    @GET("Event/categories")
    Call<List<Integer>> getCategories();

    @GET("Location")
    Call<List<Location>> getLocations(@Query("userLat") double userLat, @Query("userLon") double userLon, @Query("radius") long radius);
}
