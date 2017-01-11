package eis.bikefriends;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class CalibrationActivity extends AppCompatActivity implements LocationListener {
    private static String LOCATION_PERMISSIONS[] = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
    };

    Button savebtn;
    ToggleButton toggleCalbtn;
    TextView traveledDistanceTV, currentSpeedTV, averageSpeedTV;


    private final static int REQUEST_LOCATIONS_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        traveledDistanceTV = (TextView)findViewById(R.id.traveledDistanceTV);
        currentSpeedTV = (TextView)findViewById(R.id.currectSpeedTV);
        averageSpeedTV = (TextView)findViewById(R.id.averageSpeedTV);

        toggleCalbtn = (ToggleButton)findViewById(R.id.toggleCalbtn);

        savebtn = (Button)findViewById(R.id.savebtn);



        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATIONS_CODE);
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.onLocationChanged(null);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATIONS_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission erteilt", Toast.LENGTH_SHORT).show();
                } else {
                    // permission denied
                    Toast.makeText(this, "Permission nicht erteilt!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     * Called when the provider is enabled by the user.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Called when the provider is disabled by the user. If requestLocationUpdates
     * is called on an already disabled provider, this method is called
     * immediately.
     *
     * @param provider the name of the location provider associated with this
     *                 update.
     */
    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onLocationChanged(Location location) {
        if (location == null){
            currentSpeedTV.setText("-- m/s");

        }else {
            float currentSpeed = location.getSpeed();
            currentSpeedTV.setText(currentSpeed + " m/s");
        }

    }
}
