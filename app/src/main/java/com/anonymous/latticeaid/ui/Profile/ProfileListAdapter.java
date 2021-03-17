package com.anonymous.latticeaid.ui.Profile;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anonymous.latticeaid.R;
import com.anonymous.latticeaid.ui.Connect.ConnectListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ProfileListAdapter extends RecyclerView.Adapter<ProfileListAdapter.ViewHolder> {

    HashMap<String, Location> userLocations;
    List<String> latLng = new ArrayList<>();

    public ProfileListAdapter(HashMap<String, Location> userLocations) {
        this.userLocations = userLocations;
        for (Location location : userLocations.values()) {
            String loc = location.getLatitude() + ", " + location.getLongitude();
            latLng.add(loc);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String s = (position+1) + ".   " + latLng.get(position);
        holder.deviceNamesTV.setText(s);
    }

    @Override
    public int getItemCount() {
        return userLocations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView deviceNamesTV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceNamesTV = itemView.findViewById(R.id.deviceNamesTV);

        }
    }
}
