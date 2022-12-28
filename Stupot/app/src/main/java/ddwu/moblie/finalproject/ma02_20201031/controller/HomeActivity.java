package ddwu.moblie.finalproject.ma02_20201031.controller;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.MotionEventCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceTypes;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.heojiye.stupot.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ddwu.mobile.place.placebasic.OnPlaceBasicResult;
import ddwu.mobile.place.placebasic.PlaceBasicManager;
import ddwu.mobile.place.placebasic.pojo.PlaceBasic;
import ddwu.moblie.finalproject.ma02_20201031.model.LogDBHelper;
import ddwu.moblie.finalproject.ma02_20201031.model.PlaceDBHelper;
import ddwu.moblie.finalproject.ma02_20201031.model.Time;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mMap;
    FusedLocationProviderClient flpClient;
    Location curPos;
    PlaceBasicManager recommendManager;

    private PlacesClient placesClient;

    /* markers */
    List<Marker> recommend_markers = new ArrayList<>();
    List<Marker> log_markers = new ArrayList<>();

    View title_bar;
    View[] window;
    int curWindow = 0;
    boolean[] window_flag;

    /* option */
    Switch[] switches;

    /* selPos */
    LatLng sel_pos;
    String sel_name;
    String sel_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        setWindows();
        setOptionView();

        createMap();
    }

    void setWindows() {
        title_bar = findViewById(R.id.home_title_bar);

        window = new View[2];
        window[0] = findViewById(R.id.home_option);
        window[1] = findViewById(R.id.home_info);
        window_flag = new boolean[2];
        window_flag[0] = window_flag[1] = false;

        title_bar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        for (int i = 0; i < 2; i++) {
            window[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return true;
                }
            });
        }
    }

    void setOptionView() {
        switches = new Switch[2];
        switches[0] = (Switch) findViewById(R.id.filter1_switch);
        switches[1] = (Switch) findViewById(R.id.filter2_switch);

        switches[0].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                if (b) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        switches[0].setTrackTintList(getResources().getColorStateList(R.color.point_2, null));
                        log_place();
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        switches[0].setTrackTintList(getResources().getColorStateList(R.color.text_2, null));
                        for(Marker marker: log_markers) marker.remove();
                    }
                }
            }
        });

        switches[1].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean b) {
                if (b) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        switches[1].setTrackTintList(getResources().getColorStateList(R.color.point_2, null));
                        recommend_place();
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        switches[1].setTrackTintList(getResources().getColorStateList(R.color.text_2, null));
                        for(Marker marker: recommend_markers) marker.remove();
                    }
                }

            }
        });
    }

    public void windowTap(View view) {
        if (!window_flag[curWindow]) {
            ObjectAnimator animation = ObjectAnimator.ofFloat(window[curWindow], "translationY", -740f);
            animation.setDuration(250);
            animation.start();
            window_flag[curWindow] = true;
        } else {
            ObjectAnimator animation = ObjectAnimator.ofFloat(window[curWindow], "translationY", 0f);
            animation.setDuration(250);
            animation.start();
            window_flag[curWindow] = false;
        }
    }

    void createMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {return;}

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        flpClient = LocationServices.getFusedLocationProviderClient(this);

        flpClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                curPos = location;
                LatLng currentLoc = new LatLng(lat, lng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, 17));
            }
        });

        Places.initialize(getApplicationContext(), "AIzaSyBkOc_8tbRWcUVX3zjZXRicKgAL9PwnjKk");
        placesClient = Places.createClient(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                String placeId = (String) marker.getTag();

                List<Place.Field> placeFields = Arrays.asList(
                        Place.Field.ID, Place.Field.NAME, Place.Field.TYPES,
                        Place.Field.ADDRESS, Place.Field.PHOTO_METADATAS
                );

                sel_pos = marker.getPosition();
                sel_name = marker.getTitle();
                sel_id = placeId;

                FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

                placesClient.fetchPlace(request)
                        .addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                                  @Override
                                  public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                                      Place place = fetchPlaceResponse.getPlace();

                                      window[curWindow].setVisibility(View.INVISIBLE);
                                      window_flag[curWindow] = true; windowTap(window[curWindow]);

                                      curWindow = 1;
                                      window[curWindow].setVisibility(View.VISIBLE);
                                      window_flag[curWindow] = false; windowTap(window[curWindow]);

                                      TextView title = (TextView) findViewById(R.id.info_name);
                                      TextView type = (TextView) findViewById(R.id.info_type);
                                      TextView address = (TextView) findViewById(R.id.info_address);
                                      ImageView img = (ImageView) findViewById(R.id.info_image);

                                      title.setText(place.getName());
                                      type.setText(place.getTypes().get(0).toString());
                                      address.setText(place.getAddress());

                                      List<PhotoMetadata> metadatas = place.getPhotoMetadatas();
                                      if(metadatas == null || metadatas.isEmpty()) {
                                          img.setImageBitmap(null);
                                          return;
                                      }

                                      FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(metadatas.get(0))
                                              .setMaxWidth(500) // Optional.
                                              .setMaxHeight(300) // Optional.
                                              .build();
                                      placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                                          Bitmap bitmap = fetchPhotoResponse.getBitmap();
                                          img.setImageBitmap(bitmap);
                                      }).addOnFailureListener((exception) -> {
                                          if (exception instanceof ApiException) {
                                              final ApiException apiException = (ApiException) exception;
                                              Log.e(TAG, "Place not found: " + exception.getMessage());
                                              final int statusCode = apiException.getStatusCode();
                                              // TODO: Handle error with given status code.
                                          }
                                      });
                                  }
                              }
                        )
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, e.getMessage());
                            }
                        });
                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (curWindow == 1) {
                    window[curWindow].setVisibility(View.INVISIBLE);
                    window_flag[curWindow] = true; windowTap(window[curWindow]);

                    curWindow = 0;
                    window[curWindow].setVisibility(View.VISIBLE);
                }
            }
        });

        log_place();
    }

    public void go_timer(View view) {
        Intent intent = new Intent(getApplicationContext(), TimerActivity.class);

        intent.putExtra("lat", sel_pos.latitude);
        intent.putExtra("lng", sel_pos.longitude);
        intent.putExtra("name", sel_name);
        intent.putExtra("id", sel_id);

        startActivity(intent);
    }

    private void exitProgram() {
        moveTaskToBack(true);

        if (Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
        } else {
            finish();
        }

        System.exit(0);
    }

    void recommend_place() {
        recommendManager = new PlaceBasicManager("AIzaSyBkOc_8tbRWcUVX3zjZXRicKgAL9PwnjKk");

        recommendManager.setOnPlaceBasicResult(new OnPlaceBasicResult() {
            @Override
            public void onPlaceBasicResult(List<PlaceBasic> list) {
                for(PlaceBasic place: list) {
                    MarkerOptions options = new MarkerOptions();
                    options.position(new LatLng(place.getLatitude(), place.getLongitude()));
                    options.title(place.getName());
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

                    Marker marker = mMap.addMarker(options);
                    marker.showInfoWindow();
                    marker.setTag(place.getPlaceId());

                    recommend_markers.add(marker);
                }
            }
        });

        recommendManager.searchPlaceBasic(curPos.getLatitude(), curPos.getLongitude(), 5000, PlaceTypes.CAFE);
        recommendManager.searchPlaceBasic(curPos.getLatitude(), curPos.getLongitude(), 5000, PlaceTypes.LIBRARY);
    }

    void log_place() {
        PlaceDBHelper placeDBHelper = new PlaceDBHelper(HomeActivity.this);
        SQLiteDatabase placeDB = placeDBHelper.getReadableDatabase();

        LogDBHelper logDBHelper = new LogDBHelper(HomeActivity.this);
        SQLiteDatabase logDB = logDBHelper.getReadableDatabase();

        Cursor cursor = placeDB.rawQuery("SELECT * FROM " + PlaceDBHelper.TABLE_NAME + ";", null);
        while(cursor.moveToNext()) {
            String id = cursor.getString(0);
            String name = cursor.getString(1);
            Double lat = cursor.getDouble(2);
            Double lng = cursor.getDouble(3);

            Log.d(TAG, name);

            String[] columns = { LogDBHelper.COL_TIME };
            String selection = LogDBHelper.COL_PLACEID + "=?";
            String[] selectArgs = new String[]{ id };

            Cursor c = logDB.query(LogDBHelper.TABLE_NAME, columns, selection, selectArgs, null, null, null, null);

            Time sum = new Time(0);
            while(c.moveToNext()) {
                int t = c.getInt(0);
                sum = sum.add(new Time(t));
            }
            MarkerOptions options = new MarkerOptions();
            options.position(new LatLng(lat, lng));
            options.title(name);
            options.snippet(sum.toString());
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

            Marker marker = mMap.addMarker(options);
            marker.setTag(id);

            log_markers.add(marker);
        }
    }
}
