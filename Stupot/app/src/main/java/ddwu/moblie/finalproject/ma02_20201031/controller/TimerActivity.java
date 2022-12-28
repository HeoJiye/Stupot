package ddwu.moblie.finalproject.ma02_20201031.controller;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.heojiye.stupot.R;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ddwu.moblie.finalproject.ma02_20201031.model.LogDBHelper;
import ddwu.moblie.finalproject.ma02_20201031.model.PlaceDBHelper;
import ddwu.moblie.finalproject.ma02_20201031.model.Time;

public class TimerActivity extends AppCompatActivity {
    GoogleMap mMap;
    FusedLocationProviderClient flpClient;

    LatLng curpos;
    Time time;

    View timer_group;
    View timer_play;
    View timer_pause;

    TextView[] timer_text;

    Timer timer;
    boolean flag;
    TimerTask timerTask;

    Double lat;
    Double lng;
    String name;
    String id;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timer);

        Intent intent = getIntent();

        lat = intent.getDoubleExtra("lat", 0);
        lng = intent.getDoubleExtra("lng", 0);
        name = intent.getStringExtra("name");
        id = intent.getStringExtra("id");

        curpos = new LatLng(lat, lng);

        TextView title = findViewById(R.id.timer_name);
        title.setText(name);

        createMap();
        timer_set();

        View bg = findViewById(R.id.timer_bg);
        bg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        View button = findViewById(R.id.timer_cancel);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlaceDBHelper placeDBHelper = new PlaceDBHelper(TimerActivity.this);
                SQLiteDatabase placeDB = placeDBHelper.getWritableDatabase();

                Cursor cursor = placeDB.rawQuery("SELECT * FROM " + PlaceDBHelper.TABLE_NAME + " WHERE " + PlaceDBHelper.COL_PLACEID + "= ?;", new String[] { id });
                if(!cursor.moveToNext()) {
                    ContentValues place = new ContentValues();
                    place.put(PlaceDBHelper.COL_PLACEID, id);
                    place.put(PlaceDBHelper.COL_NAME, name);
                    place.put(PlaceDBHelper.COL_LATITUDE, lat);
                    place.put(PlaceDBHelper.COL_LONGITUDE, lng);

                    placeDB.insert(PlaceDBHelper.TABLE_NAME, null, place);
                }
                cursor.close();

                LogDBHelper logDBHelper = new LogDBHelper(TimerActivity.this);
                SQLiteDatabase logDB = logDBHelper.getWritableDatabase();

                ContentValues log = new ContentValues();
                log.put(LogDBHelper.COL_PLACEID, id);
                log.put(LogDBHelper.COL_DATE, String.valueOf(new Date()));
                log.put(LogDBHelper.COL_TIME, time.toInteger());

                logDB.insert(LogDBHelper.TABLE_NAME, null, log);

                placeDBHelper.close();
                logDBHelper.close();

                if(flag == true) {
                    timerTask.cancel();
                }

                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });
    }

    void timer_set() {
        timer_group = findViewById(R.id.timer);
        timer_play = findViewById(R.id.timer_play);
        timer_pause = findViewById(R.id.timer_pause);

        timer_text = new TextView[2];
        timer_text[0] = findViewById(R.id.timer_play_text);
        timer_text[1] = findViewById(R.id.timer_pause_text);

        flag = true;

        time = new Time(0);
        updateTimerText();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                TimerActivity.this.runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        time = time.add(new Time(1));
                        updateTimerText();
                    }
                });

            }
        };

        timer = new Timer();
        timer.schedule(timerTask, 1000, 1000);

        timer_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flag == true) {
                    timerTask.cancel();
                    timer_play.setVisibility(View.INVISIBLE);
                    timer_pause.setVisibility(View.VISIBLE);
                    flag = false;
                } else {
                    timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            TimerActivity.this.runOnUiThread(new Runnable(){
                                @Override
                                public void run() {
                                    time = time.add(new Time(1));
                                    updateTimerText();
                                }
                            });

                        }
                    };
                    timer.schedule(timerTask, 1000, 1000);
                    timer_play.setVisibility(View.VISIBLE);
                    timer_pause.setVisibility(View.INVISIBLE);
                    flag = true;
                }
            }
        });
    }
    void updateTimerText() {
        for(TextView text : timer_text) {
            text.setText(time.toString());
        }
    };

    void createMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {return;}

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.timer_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mMap = googleMap;

                MarkerOptions options = new MarkerOptions();
                options.position(curpos);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));

                Marker marker = mMap.addMarker(options);
                marker.showInfoWindow();

                LatLng pos = new LatLng(curpos.latitude+0.0012, curpos.longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 17));
            }
        });
    }
}
