package com.anonymous.latticeaid.ui.Profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.anonymous.latticeaid.R;

public class ProfileFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textView = root.findViewById(R.id.text_profile);
        profileViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }
}