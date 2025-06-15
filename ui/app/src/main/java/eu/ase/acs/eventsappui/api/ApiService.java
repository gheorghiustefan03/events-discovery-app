package eu.ase.acs.eventsappui.api;

import java.util.List;
import java.util.Map;

import eu.ase.acs.eventsappui.entities.Category;
import eu.ase.acs.eventsappui.entities.Event;
import eu.ase.acs.eventsappui.entities.EventInteraction;
import eu.ase.acs.eventsappui.entities.Location;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @GET("Event")
    Call<ResponseBody> getEvents(@Query("lId") List<Integer> locationIds);

    @POST("Event/categories")
    Call<List<Integer>> getCategories(@Body Map<String, Object> body);

    @GET("Location")
    Call<List<Location>> getLocations(@Query("userLat") double userLat, @Query("userLon") double userLon, @Query("radius") long radius);

    @POST("EventInteraction")
    Call<ResponseBody> postEventInteraction(@Body EventInteraction eventInteraction);

    @POST("Event/indices")
    Call<Map<Integer, Integer>> getEventIndices(@Body Map<String, Object> body);
}
