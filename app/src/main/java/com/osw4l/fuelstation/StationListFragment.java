package com.osw4l.fuelstation;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.rahatarmanahmed.cpv.CircularProgressView;

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
public class StationListFragment extends Fragment implements SearchView.OnQueryTextListener {


    public StationListFragment() {
        // Required empty public constructor
    }

    View view;
    private List<EstacionItem> estacionesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private EstacionAdapter estacionAdapter;
    CircularProgressView progressView;
    private RequestQueue requestQueue;
    JsonObjectRequest jsArrayRequest;
    SwipeRefreshLayout swipeRefreshLayout;
    
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_station_list, container, false);
        progressView = (CircularProgressView) view.findViewById(R.id.progress_view_estaciones);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayoutEstaciones);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

        
        recyclerView = (RecyclerView) view.findViewById(R.id.estaciones_recycler_view);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        estacionAdapter = new EstacionAdapter(estacionesList);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), LinearLayoutManager.VERTICAL));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(estacionAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(view.getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                EstacionItem estacion = estacionesList.get(position);

                Intent intent = new Intent(view.getContext(), DetailStation.class);
                intent.putExtra("id", estacion.getId());
                intent.putExtra("nombre", estacion.getNombre());
                intent.putExtra("direccion", estacion.getDireccion());
                intent.putExtra("latitud", estacion.getLatitud());
                intent.putExtra("longitud", estacion.getLongitud());
                startActivity(intent);

            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
        setDataAdapter();
        
        setActionBarTittle("Lista de estaciones");
        return view;
    }

    public void setActionBarTittle(String t){
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(t);
    }

    public void setDataAdapter(){


        estacionesList.clear();
        requestQueue= Volley.newRequestQueue(this.getContext());
        requestQueue.getCache().clear();
        jsArrayRequest = new JsonObjectRequest(
                Request.Method.GET,
                "https://fuelstation.herokuapp.com/api/viewsets/estaciones/",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        estacionesList = parseJson(response);
                        estacionAdapter = new EstacionAdapter(estacionesList);
                        recyclerView.setAdapter(estacionAdapter);
                        estacionAdapter.notifyDataSetChanged();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        
                    }
                }
        );

        requestQueue.add(jsArrayRequest);
        requestQueue.addRequestFinishedListener(new RequestQueue.RequestFinishedListener<String>() {
            @Override
            public void onRequestFinished(Request<String> request) {
                progressView.stopAnimation();
                progressView.setVisibility(View.INVISIBLE);
            }
        });
    }

    public List<EstacionItem> parseJson(JSONObject jsonObject) {
        // Variables locales
        List<EstacionItem> estaciones = new ArrayList();
        JSONArray jsonArray = null;

        try {
            // Obtener el array del objeto
            jsonArray = jsonObject.getJSONArray("estaciones");

            for (int i = 0; i < jsonArray.length(); i++) {

                try {
                    JSONObject objeto = jsonArray.getJSONObject(i);

                    EstacionItem estacion = new EstacionItem(
                            Integer.parseInt(objeto.getString("id")),
                            objeto.getString("nombre"),
                            objeto.getString("direccion"),
                            Double.parseDouble(objeto.getString("latitud")),
                            Double.parseDouble(objeto.getString("longitud")));
                    estaciones.add(estacion);

                } catch (JSONException e) {
                    Log.e(TAG, "Error de parsing: " + e.getMessage());
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return estaciones;
    }

    public void refreshData(){
        setDataAdapter();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("Escriba el numero de la mesa");
        searchView.setOnQueryTextListener(this);
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        MenuItemCompat.setOnActionExpandListener(item,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        estacionAdapter.setFilter(estacionesList);
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }
                });
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        setDataAdapter();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        final List<EstacionItem> filteredModelList = filter(estacionesList, newText);
        estacionAdapter.setFilter(filteredModelList);
        return true;
    }

    private List<EstacionItem> filter(List<EstacionItem> models, String query) {
        query = query.toLowerCase();
        final List<EstacionItem> filteredModelList = new ArrayList<>();
        for (EstacionItem model : models) {
            final String text = model.getNombre().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }
}
