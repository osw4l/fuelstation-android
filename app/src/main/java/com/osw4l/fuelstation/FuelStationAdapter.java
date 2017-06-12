package com.osw4l.fuelstation;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FuelStationAdapter extends RecyclerView.Adapter<FuelStationAdapter.MyViewHolder> {

    private List<FuelItem> productosList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView fuel_name, fuel_price;

        public MyViewHolder(View view) {
            super(view);
            fuel_name = (TextView) view.findViewById(R.id.combustible_nombre);
            fuel_price = (TextView) view.findViewById(R.id.combustible_precio);
        }
    }


    public FuelStationAdapter(List<FuelItem> productosList) {
        this.productosList = productosList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fuel_station_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FuelItem producto = productosList.get(position);
        holder.fuel_name.setText(producto.getNombre());
        holder.fuel_price.setText(producto.humanize());
    }

    @Override
    public int getItemCount() {
        return productosList.size();
    }

}