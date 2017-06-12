package com.osw4l.fuelstation;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {


    public MapFragment() {
        // Required empty public constructor
    }

    View view;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleApiClient mGoogleApiClient;
    CircularProgressView progressView;
    private RequestQueue requestQueue;
    JsonObjectRequest jsArrayRequest;
    RelativeLayout mBackground;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_map, container, false);
        progressView = (CircularProgressView) view.findViewById(R.id.progress_view);
        mBackground = (RelativeLayout) view.findViewById(R.id.background);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        setActionBarTittle("Estaciones en el mapa");
        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        onMyLocationButtonClick();
    }

    public void setUpMap(double latitud, double longitud, String titulo){
        requestData();
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitud, longitud))
                .title(titulo)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(latitud, longitud), 14));
    }

    public void putMarker(double latitud, double longitud, String titulo, String data){
        mMap.addMarker(new MarkerOptions().position(new LatLng(latitud, longitud))
                .title(titulo)
                .snippet("Direccion: "+data)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    @Override
    public boolean onMyLocationButtonClick() {
        mMap.clear();
        try {
            enableMyLocation();
            if (mMap.isMyLocationEnabled())
                setUpMap(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude(), "Mi Ubicacion");
        } catch (Exception e){
            setUpMap(10.9735242, -74.8168193, "Barranquilla");
            Toast.makeText(view.getContext(), "Bienvenido a Gasolina App", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            PermissionUtils.requestPermission((AppCompatActivity) view.getContext(), LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }



    public void setActionBarTittle(String t){
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(t);
    }

    public void requestData(){
        requestQueue= Volley.newRequestQueue(this.getContext());
        requestQueue.getCache().clear();
        jsArrayRequest = new JsonObjectRequest(
                Request.Method.GET,
                "https://fuelstation.herokuapp.com/api/viewsets/estaciones/",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        parseJson(response);
                    }
                },
                new Response.ErrorListener()

                {
                    @Override
                    public void onErrorResponse (VolleyError error){

                    }
                });

        requestQueue.add(jsArrayRequest);
        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {
                progressView.stopAnimation();
                progressView.setVisibility(View.INVISIBLE);
                mBackground.setVisibility(View.INVISIBLE);
            }
        });
    }

    public void parseJson(JSONObject jsonObject) {
        JSONArray jsonArray = null;
        try {
            // Obtener el array del objeto
            jsonArray = jsonObject.getJSONArray("estaciones");

            for (int i = 0; i < jsonArray.length(); i++) {

                try {
                    JSONObject objeto = jsonArray.getJSONObject(i);
                    putMarker(
                            Double.parseDouble(objeto.getString("latitud")),
                            Double.parseDouble(objeto.getString("longitud")),
                            objeto.getString("nombre"),
                            objeto.getString("direccion")
                    );
                } catch (JSONException e) {
                    Log.e(TAG, "Error de parsing: " + e.getMessage());
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
