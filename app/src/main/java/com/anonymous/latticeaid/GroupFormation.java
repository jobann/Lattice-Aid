package com.anonymous.latticeaid;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class GroupFormation extends AppCompatActivity {

    static int numOfConnections = 0;
    static List<Location> locations = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_formation);

        locations.add(createLocation(43.7248485, -79.8996285));
        locations.add(createLocation(43.5769843, -79.7745345));
        locations.add(createLocation(43.7181557, -79.5181426));
        locations.add(createLocation(43.8322774, -80.0597995));
        locations.add(createLocation(43.6535434, -79.9373463));
        locations.add(createLocation(43.8192812, -79.6873988));


        locations.add(createLocation(43.8192812, -79.6873988));
        locations.add(createLocation(43.2192812, -79.3473988));
        locations.add(createLocation(43.3192812, -79.2673988));
        locations.add(createLocation(43.4192812, -79.5373988));
        locations.add(createLocation(43.5192812, -79.6273988));

        createGroup(locations);

    }

    public void createGroup(List<Location> locations) {
        TreeMap<Integer, Integer> group = new TreeMap<>();
        TreeMap<Location, List<Location>> groupInfo = new TreeMap<>();
        List<Location> tmpLocation = locations;

        for (int i = 0; i < locations.size(); i++) {
            numOfConnections = 0;
            List<Location> listLocation = new ArrayList<>();
            for (int j = 0; j < locations.size(); j++) {
                if (i == j) {
                    continue;
                }
                if (i == 0) {
                    Log.d("JOBANN", String.valueOf(locations.get(i).distanceTo(locations.get(j))));
                }

                if (locations.get(i).distanceTo(locations.get(j)) <= 1000 * 25) {
                    numOfConnections++;
                    group.put(i, numOfConnections);
                    listLocation.add(locations.get(i));
                }
            }
        }
        Log.d("JOBANN", group.toString());
    }

    //Getting Center device
    public static LatLng getCentralGeoCoordinate(List<LatLng> geoCoordinates) {
        if (geoCoordinates.size() == 1) {
            return geoCoordinates.get(0);
        }

        double x = 0;
        double y = 0;
        double z = 0;

        for (LatLng geoCoordinate : geoCoordinates) {
            double latitude = geoCoordinate.latitude * Math.PI / 180;
            double longitude = geoCoordinate.longitude * Math.PI / 180;

            x += Math.cos(latitude) * Math.cos(longitude);
            y += Math.cos(latitude) * Math.sin(longitude);
            z += Math.sin(latitude);
        }

        int total = geoCoordinates.size();

        x = x / total;
        y = y / total;
        z = z / total;

        double centralLongitude = Math.atan2(y, x);
        double centralSquareRoot = Math.sqrt(x * x + y * y);
        double centralLatitude = Math.atan2(z, centralSquareRoot);

        //return new LatLng(centralLatitude * 180 / Math.PI, centralLongitude * 180 / Math.PI);


        //Log.d("JOBANN", getCentralGeoCoordinate(coordinates).toString());

        double minDistance = 0;
        LatLng closestLatLng = null;
        LatLng centerLatLng = new LatLng(centralLatitude * 180 / Math.PI, centralLongitude * 180 / Math.PI);

        for (int i = 0; i < geoCoordinates.size(); i++) {
            double dist = distance(centerLatLng, geoCoordinates.get(i), "K");

            if (minDistance == 0 || dist < minDistance) {
                minDistance = dist;
                closestLatLng = geoCoordinates.get(i);
            }
        }
        assert closestLatLng != null;
        return closestLatLng;
        //Log.d("JOBANN", closestLatLng.toString());

    }

    private static double distance(LatLng latLng1, LatLng latLng2, String unit) {
        double lat1 = latLng1.latitude;
        double lon1 = latLng1.longitude;
        double lat2 = latLng2.latitude;
        double lon2 = latLng2.longitude;

        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }

    public Location createLocation(double lat, double lng) {
        Location loc = new Location(LocationManager.GPS_PROVIDER);
        loc.setLatitude(lat);
        loc.setLongitude(lng);

        return loc;
    }

    public void checkIfIdeal(TreeMap<Location, List<Location>> groupInfo) {
       for(List<Location> devices: groupInfo.values()){

       }
    }
}