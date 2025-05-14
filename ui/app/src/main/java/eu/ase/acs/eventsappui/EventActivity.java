package eu.ase.acs.eventsappui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smarteist.autoimageslider.SliderView;

import org.threeten.bp.LocalDateTime;

import java.util.List;
import java.util.Locale;

import eu.ase.acs.eventsappui.adapters.SliderAdapter;
import eu.ase.acs.eventsappui.entities.Event;

public class EventActivity extends AppCompatActivity {
    private Event event;
    private SliderView eventsSlider;
    private TextView tvName, tvDescription, tvLocation, tvLink, tvEndTime, tvStartTime;
    private int savedArrayIndex = -1;
    private int savedArraySize;
    FloatingActionButton fabSave;
    FloatingActionButton fabBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initComponents();

        Bundle bundle = getIntent().getExtras();
        event = (Event) bundle.getSerializable(HomeFragment.EVENT_KEY);

        SharedPreferences sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        savedArraySize = sharedPreferences.getInt("saved_events_size", 0);
        for (int i = 0; i < savedArraySize; i++) {
            if (sharedPreferences.getInt("saved_events_" + i, -1) == event.getId()) {
                savedArrayIndex = i;
                fabSave.setImageResource(R.drawable.saved_icon);
                break;
            }
        }

        List<String> imgUrls = event.getImageUrls();
        SliderAdapter adapter = new SliderAdapter(imgUrls);
        eventsSlider.setSliderAdapter(adapter);
        tvName.setText(event.getName());
        tvDescription.setText(event.getDescription());
        tvLocation.setText(Html.fromHtml(
                "<a href=\"" + "https://www.google.com/maps/search/?api=1&query=" + event.getLocation().getLatitude() + "," + event.getLocation().getLongitude() + "&zoom=13" + "\">" + "\uD83D\uDCCC" + event.getLocation().getName() + "</a>"
                , Html.FROM_HTML_MODE_COMPACT));
        tvLocation.setMovementMethod(LinkMovementMethod.getInstance());
        tvLink.setText(Html.fromHtml(
                "<a href=\"" + event.getLink() + "\">" + getResources().getString(R.string.organizer_page_hyperlink) + "</a>"
                , Html.FROM_HTML_MODE_COMPACT));
        tvLink.setMovementMethod(LinkMovementMethod.getInstance());

        LocalDateTime startDate = event.getStartDate();
        LocalDateTime endDate = event.getEndDate();

        tvStartTime.setText(String.format(Locale.ENGLISH, "%02d.%02d.%02d, %02d:%02d",
                startDate.getMonth().getValue(),
                startDate.getDayOfMonth(),
                startDate.getYear() - 2000,
                startDate.getHour(),
                startDate.getMinute()) + " ");

        tvEndTime.setText(String.format(Locale.ENGLISH, "%02d.%02d.%02d, %02d:%02d",
                endDate.getMonth().getValue(),
                endDate.getDayOfMonth(),
                endDate.getYear() - 2000,
                endDate.getHour(),
                endDate.getMinute()) + " ");

        fabBack.setOnClickListener(view -> {
            finish();
        });
        fabSave.setOnClickListener(view -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (savedArrayIndex == -1) {
                editor.putInt("saved_events_size", savedArraySize + 1);
                editor.putInt("saved_events_" + (savedArraySize), event.getId());
                savedArraySize += 1;
                savedArrayIndex = savedArraySize - 1;
                fabSave.setImageResource(R.drawable.saved_icon);
            } else {
                for (int i = savedArrayIndex; i < savedArraySize - 1; i++) {
                    editor.putInt("saved_events_" + i, sharedPreferences.getInt("saved_events_" + (i + 1), 0));
                }
                editor.remove("saved_events_" + (savedArraySize - 1));
                editor.putInt("saved_events_size", savedArraySize - 1);
                savedArrayIndex = -1;
                savedArraySize -= 1;
                fabSave.setImageResource(R.drawable.unsaved_icon);
            }
            editor.apply();
        });
    }

    private void initComponents() {
        eventsSlider = findViewById(R.id.events_slider);
        tvName = findViewById(R.id.tv_details_name);
        tvDescription = findViewById(R.id.tv_details_description);
        tvLocation = findViewById(R.id.tv_details_location);
        tvLink = findViewById(R.id.tv_details_link);
        tvEndTime = findViewById(R.id.tv_details_end_time);
        tvStartTime = findViewById(R.id.tv_details_start_time);
        fabBack = findViewById(R.id.fab_back);
        fabSave = findViewById(R.id.fab_save);
    }
}