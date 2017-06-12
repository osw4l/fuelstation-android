package com.osw4l.fuelstation;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

public class EstacionAdapter extends RecyclerView.Adapter<EstacionAdapter.MyViewHolder> {

    private List<EstacionItem> estacionsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nombre_estacion, direccion_estacion;

        public MyViewHolder(View view) {
            super(view);
            nombre_estacion = (TextView) view.findViewById(R.id.nombre_estacion);
            direccion_estacion = (TextView) view.findViewById(R.id.direccion_estacion);
        }
    }


    public EstacionAdapter(List<EstacionItem> estacionsList) {
        this.estacionsList = estacionsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.estacion_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        EstacionItem estacion = estacionsList.get(position);
        holder.nombre_estacion.setText(estacion.getNombre());
        holder.direccion_estacion.setText(estacion.getDireccion());
    }

    @Override
    public int getItemCount() {
        return estacionsList.size();
    }

    public void setFilter(List<EstacionItem> estacionModels) {
        estacionsList = new ArrayList<>();
        estacionsList.addAll(estacionModels);
        notifyDataSetChanged();
    }
}