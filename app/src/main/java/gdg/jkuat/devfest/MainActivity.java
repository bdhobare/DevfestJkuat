package gdg.jkuat.devfest;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import gdg.jkuat.devfest.Utils.RoundedTransformation;
import gdg.jkuat.devfest.network.NetworkHandler;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback,GoogleMap.OnInfoWindowClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    LocationRequest mLocationRequest;
    LatLng latLng;
    Marker currLocationMarker;
    private  static final int PERMISSION_ACCESS_FINE_LOCATION=100;
    private final int PLACE_PICKER_REQUEST = 200;

    private Marker parliament;

    private Marker jkuat;

    private Marker kisumu;


    private static final LatLng PARLIAMENT = new LatLng(1.289304, 36.819722);

    private static final LatLng JKUAT = new LatLng(1.088066,37.010540);

    private static final LatLng KISUMU = new LatLng(0.092482, 34.768379);

    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

    private ProgressDialog dialog;
    private String baseString="https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private int radius=1000;

    private static final String TAG = MainActivity.class.getName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dialog=new ProgressDialog(this);
        dialog.setMessage("Fetching...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String token = FirebaseInstanceId.getInstance().getToken();
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
        if (!isLocationPermissionGranted()){
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    PERMISSION_ACCESS_FINE_LOCATION);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView=navigationView.getHeaderView(0);
        ImageView profileImage=(ImageView)headerView.findViewById(R.id.header);
        TextView userName=(TextView)headerView.findViewById(R.id.userName);

        Picasso.with(this)
                .load("http://i.imgur.com/DvpvklR.png")
                .placeholder(R.mipmap.ic_launcher)
                .transform(new RoundedTransformation(50, 4))
                .resizeDimen(R.dimen.image_size, R.dimen.image_size)
                .centerCrop()
                .into(profileImage);
        //TODO remove this.Test
        userName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        subscribe();

    }
    public void subscribe(){
        // [START subscribe_topics]
        FirebaseMessaging.getInstance().subscribeToTopic("news");
        String msg = "Subscribed to notifications.";
        Log.d(TAG, msg);
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
    public boolean isLocationPermissionGranted(){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
            //startActivity(new Intent(getApplicationContext(),MapsActivity.class));
        }else {
            return false;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Devfest needs to access your location!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String API_KEY="AIzaSyAsblIrddstN2kSZl-EL0GUbVufGOTLF28";
        //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670,151.1957&radius=500&types=food&name=cruise&key=API_KEY
        switch (id){
            case R.id.my_location:
                try {
                    startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.cafe:
                if (googleApiClient.isConnected()){
                    new GetPlaces().execute(baseString+"key="+API_KEY+"&location="+latLng.latitude+","+latLng.longitude+"&radius="+radius+"&types=food");
                }
                break;
            case R.id.hospital:
                if (googleApiClient.isConnected()){
                    new GetPlaces().execute(baseString+"key="+API_KEY+"&location="+latLng.latitude+","+latLng.longitude+"&radius="+radius+"&types=hospital");
                }
                break;
            case R.id.university:
                if (googleApiClient.isConnected()){
                    new GetPlaces().execute(baseString+"key="+API_KEY+"&location="+latLng.latitude+","+latLng.longitude+"&radius="+radius+"&types=university");
                }
                break;
            case R.id.bank:
                if (googleApiClient.isConnected()){
                    new GetPlaces().execute(baseString+"key="+API_KEY+"&location="+latLng.latitude+","+latLng.longitude+"&radius="+radius+"&types=bank");
                }
                break;
            case R.id.face:
                startActivity(new Intent(getApplicationContext(),FaceTrackerActivity.class));
                break;
            case R.id.barcode:
                startActivity(new Intent(getApplicationContext(),BarcodeActivity.class));
                break;
            default:
                return true;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Log.e("CONNECTED","READY");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            if (lastLocation != null) {
                latLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.infoWindowAnchor(0.5f, 0.5f);
                markerOptions.title("Your Current Position");
                markerOptions.snippet("("+lastLocation.getLatitude()+","+lastLocation.getLongitude());

                currLocationMarker = mMap.addMarker(markerOptions);


                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(latLng).zoom(14).build();

                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
                mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
                mMap.setOnMarkerClickListener(this);
                mMap.setOnInfoWindowClickListener(this);
            }
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(5000); //5 seconds
            mLocationRequest.setFastestInterval(3000); //3 seconds
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, (LocationListener) this);

            if (googleApiClient != null){
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                        .addLocationRequest(mLocationRequest);

                //**************************
                builder.setAlwaysShow(true);
                //**************************

                PendingResult<LocationSettingsResult> result =
                        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
                result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                    @Override
                    public void onResult(LocationSettingsResult result) {
                        final Status status = result.getStatus();
                        final LocationSettingsStates state = result.getLocationSettingsStates();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can initialize location
                                // requests here.
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the user
                                // a dialog.
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    status.startResolutionForResult(
                                            MainActivity.this, 1000);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                break;
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
            //TODO update the marker
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
            //TODO implement
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 2 * t);

                if (t > 0.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
        return false;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Log.e("MAP","READY");
        mMap = googleMap;
        mMap.setContentDescription("DevFest Jkuat");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            buildGoogleApiClient();

            googleApiClient.connect();
        }
        addMarkersToMap();
    }
    private void addMarkersToMap() {
        // Uses a colored icon.
        parliament = mMap.addMarker(new MarkerOptions()
                .position(PARLIAMENT)
                .title("Parliament")
                .snippet("Kenya")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        // Uses a custom icon with the info window popping out of the center of the icon.
        jkuat = mMap.addMarker(new MarkerOptions()
                .position(JKUAT)
                .title("JKUAT")
                .snippet("Juja")
                .infoWindowAnchor(0.5f, 0.5f));

        // Creates a draggable marker. Long press to drag.
         kisumu= mMap.addMarker(new MarkerOptions()
                .position(KISUMU)
                .title("Kisumu")
                .snippet("Kisumu")
                .draggable(true));

    }


    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, "Map not ready", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void onClearMap() {
        if (!checkReady()) {
            return;
        }
        mMap.clear();
        addCurrentLocation();
    }

    public void onResetMap() {
        if (!checkReady()) {
            return;
        }
        // Clear the map because we don't want duplicates of the markers.
        mMap.clear();
       addCurrentLocation();
    }
    private void addCurrentLocation(){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.infoWindowAnchor(0.5f, 0.5f);
        markerOptions.title("Your Current Position");
        markerOptions.snippet("("+latLng.latitude+","+latLng.longitude);

        currLocationMarker = mMap.addMarker(markerOptions);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng).zoom(14).build();

        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));
    }

    /**
     * Demonstrates converting a {@link Drawable} to a {@link BitmapDescriptor},
     * for use as a marker icon.
     */
    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {


        private final View mWindow;

        private final View mContents;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_marker, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mContents);
            return mContents;
        }
        private void render(Marker marker, View view) {

            int badge=R.mipmap.ic_launcher;
            ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);
            Picasso.with(getApplicationContext()).load((String) marker.getTag()).placeholder(R.mipmap.ic_launcher).into(((ImageView) view.findViewById(R.id.badge)));


            TextView titleUi = ((TextView) view.findViewById(R.id.title));
            titleUi.setText(marker.getTitle());

            //TODO put custom text to textview

            String snippet = marker.getSnippet();
            TextView snippetUi = ((TextView) view.findViewById(R.id.coordinates));
            if (snippet != null && snippet.length() > 0) {
                SpannableString snippetText = new SpannableString(snippet);
                snippetText.setSpan(new ForegroundColorSpan(Color.BLACK), 0, snippetText.length(), 0);
                snippetUi.setText(snippetText);
            } else {
                snippetUi.setText("Unavailable");
            }

        }
    }

    private class GetPlaces extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String result = new NetworkHandler().get(params[0]);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            dialog.dismiss();
            if ((s != null) && (!s.isEmpty())) {
                Log.e("SEARCH",s);
                Object object = null;
                try {
                    object = new JSONTokener(s).nextValue();
                    if (object instanceof JSONObject) {
                            processResult(new JSONObject(s));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void processResult(JSONObject object){
        onClearMap();
        try {
            JSONArray array=object.getJSONArray("results");
            for (int i=0;i<array.length();i++){
                JSONObject location=array.getJSONObject(i).getJSONObject("geometry").getJSONObject("location");
                LatLng lng=new LatLng(location.getDouble("lat"),location.getDouble("lng"));
                MarkerOptions options=new MarkerOptions()
                        .position(lng)
                        .title(array.getJSONObject(i).getString("name"))
                        .snippet("("+lng.latitude+","+lng.longitude)
                        .infoWindowAnchor(0.5f, 0.5f);
                Marker marker=mMap.addMarker(options);
                marker.setTag(array.getJSONObject(i).getString("icon"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
