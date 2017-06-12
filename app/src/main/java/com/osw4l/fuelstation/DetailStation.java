package com.osw4l.fuelstation;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class DetailStation extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {

    int Id = 0;
    MapView mMapView;
    private GoogleMap mMap;
    double longitud, latitud;
    String nombre, direccion;
    private List<FuelItem> productosList = new ArrayList<>();
    private RecyclerView recyclerView;
    private FuelStationAdapter productosAdapter;
    CircularProgressView progressView;
    RequestQueue requestQueue;
    JsonObjectRequest jsArrayRequest;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_station);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Id = getIntent().getIntExtra("id", 0);
        nombre = getIntent().getStringExtra("nombre");
        setActionBarTitle(nombre);
        direccion = getIntent().getStringExtra("direccion");
        latitud = getIntent().getDoubleExtra("latitud", 0);
        longitud = getIntent().getDoubleExtra("longitud", 0);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_2);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.combustibles_recycler_view);

        productosAdapter = new FuelStationAdapter(productosList);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(productosAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                FuelItem producto = productosList.get(position);
                snackBar("valor del galon de " + producto.getNombre()+" $"+producto.humanize(), view);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        setDataAdapter();


    }

    public void setActionBarTitle(String title){
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitud, longitud))
                .title(nombre)
                .snippet("Direccion: "+direccion)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitud, longitud), 17));
    }

    public void setDataAdapter() {
        productosList.clear();
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.getCache().clear();
        jsArrayRequest = new JsonObjectRequest(
                Request.Method.GET,
                "https://fuelstation.herokuapp.com/api/viewsets/estaciones/"+Id+"/combustibles/",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        productosList = parseJson(response);
                        productosAdapter = new FuelStationAdapter(productosList);
                        recyclerView.setAdapter(productosAdapter);
                        productosAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        finish();
                        Log.d(TAG, "Error Respuesta en JSON: " + error.getMessage());

                    }
                }
        );

        requestQueue.add(jsArrayRequest);
    }

    public List<FuelItem> parseJson(JSONObject jsonObject) {
        // Variables locales
        List<FuelItem> productos = new ArrayList();
        JSONArray jsonArray = null;

        try {
            // Obtener el array del objeto
            jsonArray = jsonObject.getJSONArray("combustibles");

            for (int i = 0; i < jsonArray.length(); i++) {

                try {
                    JSONObject objeto = jsonArray.getJSONObject(i);

                    FuelItem producto = new FuelItem(
                            objeto.getString("tipo_combustible_nombre"),
                            Integer.parseInt(objeto.getString("valor"))

                    );
                    productos.add(producto);

                } catch (JSONException e) {
                    Log.e(TAG, "Error de parsing: " + e.getMessage());
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return productos;
    }

    public static void snackBar(String t, View view){
        Snackbar snackbar = Snackbar.make(view, t, Snackbar.LENGTH_SHORT);
        try {
            Field mAccessibilityManagerField = BaseTransientBottomBar.class.getDeclaredField("mAccessibilityManager");
            mAccessibilityManagerField.setAccessible(true);
            AccessibilityManager accessibilityManager = (AccessibilityManager) mAccessibilityManagerField.get(snackbar);
            Field mIsEnabledField = AccessibilityManager.class.getDeclaredField("mIsEnabled");
            mIsEnabledField.setAccessible(true);
            mIsEnabledField.setBoolean(accessibilityManager, false);
            mAccessibilityManagerField.set(snackbar, accessibilityManager);
        } catch (Exception e) {
            Log.d("Snackbar", "Reflection error: " + e.toString());
        }
        snackbar.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
