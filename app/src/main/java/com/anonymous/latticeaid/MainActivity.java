package com.anonymous.latticeaid;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import com.anonymous.latticeaid.ui.Connect.MyRecyclerViewAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {

    public static Boolean shouldShowDialog = true;
    AlertDialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        checkPermission();


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_connect, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavHostFragment nav_host_fragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert nav_host_fragment != null;
        NavController navController = nav_host_fragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);


    }

    public void refresh() {
    }

    private void checkPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 400);
            Toast.makeText(MainActivity.this, "Access fine location failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dialog != null) {
            if (!dialog.isShowing()) {
                checkPermission();
            }
        }
    }

    //Handling access fine location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 400) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Please allow all permissions to continue!").setCancelable(false).setPositiveButton("OK", (dialog, which) -> startActivity(intent));
                dialog = builder.create();
                dialog.show();

            }
        }
    }




}