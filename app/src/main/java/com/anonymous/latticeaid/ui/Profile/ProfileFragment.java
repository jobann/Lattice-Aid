package com.anonymous.latticeaid.ui.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anonymous.latticeaid.MainActivity;
import com.anonymous.latticeaid.R;

public class ProfileFragment extends Fragment {

    RecyclerView locationRV;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        locationRV = root.findViewById(R.id.locationRV);
        locationRV.setLayoutManager(new LinearLayoutManager(getContext()));
        locationRV.setAdapter(new ProfileListAdapter(((MainActivity) requireActivity()).getUserLocations()));


        return root;
    }
}