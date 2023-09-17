package com.example.nasaimage;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.nasaimage.DAO.AppDatabase;
import com.example.nasaimage.Models.CachedImageData;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView descriptionTextView;

    private RequestQueue requestQueue;

    private static final String APOD_API_URL = "https://api.nasa.gov/planetary/apod?api_key=cTjN5s2xMRI9zmCd3c9NykMXz0jQjzMCOTbXemNR";

    private AppDatabase db; // Room database

    private static final String PREFS_NAME = "APOD_PREFS";
    private static final String PREFS_KEY_RESPONSE = "APOD_RESPONSE";
    private static final String PREFS_KEY_TIMESTAMP = "APOD_TIMESTAMP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        titleTextView = findViewById(R.id.titleTextView);
        dateTextView = findViewById(R.id.dateTextView);
        descriptionTextView = findViewById(R.id.descriptionTextView);

        requestQueue = Volley.newRequestQueue(this);
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "image-database").build();

        // Try to get cached data, or fetch data from APOD API if not cached or cache has expired
        getCachedImageData();
    }

    private void fetchDataFromAPOD() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                APOD_API_URL,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Check for API rate limit exceeded
                            if (response.has("code") && response.getInt("code") == 429) {
                                // API rate limit exceeded, display an error message
                                Toast.makeText(MainActivity.this, "API rate limit exceeded. Please try again later.", Toast.LENGTH_LONG).show();
                                return;
                            }

                            // Parse JSON response
                            String imageUrl = response.getString("url");
                            String title = response.getString("title");
                            String date = response.getString("date");
                            String description = response.getString("explanation");

                            // Cache the fetched data
                            cacheImageData(imageUrl, title, date, description);

                            // Update UI with fetched data
                            titleTextView.setText(title);
                            dateTextView.setText(date);
                            descriptionTextView.setText(description);

                            // Load image using Glide
                            Glide.with(MainActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(imageView);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            // Handle parsing error
                            Toast.makeText(MainActivity.this, "Error parsing API response.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle network errors
                        error.printStackTrace();
                        Toast.makeText(MainActivity.this, "Network error. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

    private void cacheImageData(final String imageUrl, final String title, final String date, final String description) {
        // Use a background thread for database operations
        new Thread(new Runnable() {
            @Override
            public void run() {
                CachedImageData cachedImageData = new CachedImageData();
                cachedImageData.setImageUrl(APOD_API_URL);
                cachedImageData.setImage(imageUrl);
                cachedImageData.setTitle(title);
                cachedImageData.setDate(date);
                cachedImageData.setDescription(description);

                // Insert or update data in the database
                db.cachedImageDataDao().insert(cachedImageData);
            }
        }).start();
    }

    private void getCachedImageData() {
        // Use a background thread for database operations
        new Thread(new Runnable() {
            @Override
            public void run() {
                CachedImageData cachedData = db.cachedImageDataDao().getImageData(APOD_API_URL);

                if (cachedData != null) {
                    // Data is cached, update UI with cached data
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            titleTextView.setText(cachedData.getTitle());
                            dateTextView.setText(cachedData.getDate());
                            descriptionTextView.setText(cachedData.getDescription());

                            // Load image using Glide
                            Glide.with(MainActivity.this)
                                    .load(cachedData.getImage())
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .into(imageView);
                        }
                    });
                } else {
                    // Data is not cached, fetch it from the API
                    fetchDataFromAPOD();
                }
            }
        }).start();
    }

    // Check if the cache has expired (older than 24 hours)
    private boolean isCacheExpired(long timestamp) {
        Calendar lastFetched = Calendar.getInstance();
        lastFetched.setTimeInMillis(timestamp);

        Calendar current = Calendar.getInstance();

        // Set the time to 12:00 AM for both calendars
        lastFetched.set(Calendar.HOUR_OF_DAY, 0);
        lastFetched.set(Calendar.MINUTE, 0);
        lastFetched.set(Calendar.SECOND, 0);
        lastFetched.set(Calendar.MILLISECOND, 0);

        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);

        // Check if the last fetch date is older than the current date
        return lastFetched.before(current);
    }
}
