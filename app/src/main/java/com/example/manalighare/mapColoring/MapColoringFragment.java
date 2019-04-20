package com.example.manalighare.mapColoring;


import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import static com.example.manalighare.mapColoring.util.constants.MAPVIEW_BUNDLE_KEY;


public class MapColoringFragment extends Fragment implements OnMapReadyCallback{

    private GoogleApiClient mGoogleApiClient;
    private  static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40,-168),new LatLng(71,136));

    private MapView mMapView;
    private GoogleMap googleMap;

    private FloatingActionButton play_button;
    private Spinner country_spinner;
    private Spinner algorithm_spinner;

    private String selected_country;
    private String selected_algorithm;
    private String ParsedStateData;

    HashMap<Integer, Float> stateColors=new HashMap<Integer, Float>();

    private ArrayList<Point> Map_points=new ArrayList<>();

    public MapColoringFragment() {

    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_map_coloring, container, false);
        getActivity().setTitle("Map Coloring");

        play_button=(FloatingActionButton)view.findViewById(R.id.play_button);
        country_spinner=(Spinner)view.findViewById(R.id.country_spinner);
        algorithm_spinner=(Spinner)view.findViewById(R.id.algorithm_spinner);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);


        play_button.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               Map_points=new ArrayList<>();
               stateColors=new HashMap<>();
               googleMap.clear();

               if (isEverythingFilled()){

                   String jsonString=LoadJSONfromAsset();

                   ParseJsonString(jsonString,country_spinner.getSelectedItem().toString());


                   switch (algorithm_spinner.getSelectedItemPosition()){

                       case 1:
                           Graph g=createGraph(country_spinner.getSelectedItem().toString());
                           function_first_algo(country_spinner.getSelectedItem().toString(),g,v);
                           showColors(stateColors,country_spinner.getSelectedItem().toString());

                           break;

                       case 2:
                           function_second_algo(ParsedStateData,country_spinner.getSelectedItem().toString());
                           break;

                       case 3:
                           function_third_algo(ParsedStateData,country_spinner.getSelectedItem().toString());
                           break;

                       case 4:
                           function_fourth_algo(ParsedStateData,country_spinner.getSelectedItem().toString());
                           break;
                   }
               }

           }
       });

       return view;
    }

    private void ParseJsonString(String jsonString, String selected_country) {

        try {
            JSONObject root=new JSONObject(jsonString);
            JSONArray points=root.getJSONArray(selected_country);
            Gson gson=new Gson();

            for (int i=0;i<points.length();i++){

                Point tmp=gson.fromJson(points.get(i).toString(),Point.class);
                Map_points.add(tmp);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void showColors(HashMap<Integer, Float> stateColors, String s) {

        int count=0;
        for(Integer key:stateColors.keySet()){
            if (key==Map_points.get(count).stateID){
                Map_points.get(count).Color=stateColors.get(count).floatValue();
            }
            count++;
        }


        Marker marker;

        if (s.equals("USA")) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(39.0646, -105.3272)));

        }else if (s.equals("Australia")){
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(-34.28293455, 140.6000378)));
        }


        for (int i=0;i< Map_points.size();i++){

             marker=googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(Map_points.get(i).latitude)
                    ,Double.parseDouble(Map_points.get(i).longitude))).title(String.valueOf(Map_points.get(i).state))
                    .icon(BitmapDescriptorFactory.defaultMarker(Map_points.get(i).Color))
                    );

             dropPinEffect(marker);

        }




    }



    //Function for Depth first search
    private void function_first_algo(String country, Graph g,View v) {

        if (country.equals("USA")) {


            int result[] = new int[50];

            // Initialize all vertices as unassigned
            Arrays.fill(result, -1);

            // Assign the first color to first vertex
            result[0] = 0;

            // A temporary array to store the available colors. False
            // value of available[cr] would mean that the color cr is
            // assigned to one of its adjacent vertices
            boolean available[] = new boolean[5];

            // Initially, all colors are available
            Arrays.fill(available, true);

            // Assign colors to remaining V-1 vertices
            for (int u = 1; u < 50; u++) {
                // Process all adjacent vertices and flag their colors
                // as unavailable
                Iterator<Integer> it = g.getAdj()[u].iterator();

                while (it.hasNext()) {
                    int i = it.next();
                    if (result[i] != -1)
                        available[result[i]] = false;
                }

                int color;
                for (color = 0; color < 4; color++) {
                    if (available[color])
                        break;
                }

                result[u] = color;
                Arrays.fill(available, true);
            }

            // print the result
            for (int u = 0; u < 50; u++) {
                System.out.println("Vertex " + u + " ---> Color " + result[u]);
                if (result[u] == 0) {
                    stateColors.put(u, 210.0f);
                } else if (result[u] == 1) {
                    stateColors.put(u, 120.0f);
                } else if (result[u] == 2) {
                    stateColors.put(u, 300.0f);
                } else if (result[u] == 3) {
                    stateColors.put(u, 60.0f);
                } else {
                    stateColors.put(u, 30.0f);
                }

            }

        }else{



            int result[] = new int[7];

            // Initialize all vertices as unassigned
            Arrays.fill(result, -1);

            // Assign the first color to first vertex
            result[0] = 0;

            // A temporary array to store the available colors. False
            // value of available[cr] would mean that the color cr is
            // assigned to one of its adjacent vertices
            boolean available[] = new boolean[3];

            // Initially, all colors are available
            Arrays.fill(available, true);

            // Assign colors to remaining V-1 vertices
            for (int u = 1; u < 7; u++) {
                // Process all adjacent vertices and flag their colors
                // as unavailable
                Iterator<Integer> it = g.getAdj()[u].iterator();

                while (it.hasNext()) {
                    int i = it.next();
                    if (result[i] != -1)
                        available[result[i]] = false;
                }

                int color;
                for (color = 0; color < 2; color++) {
                    if (available[color])
                        break;
                }

                result[u] = color;
                Arrays.fill(available, true);
            }

            // print the result
            for (int u = 0; u < 7; u++) {
                System.out.println("Vertex " + u + " ---> Color " + result[u]);
                if (result[u] == 0) {
                    stateColors.put(u, 210.0f);
                } else if (result[u] == 1) {
                    stateColors.put(u, 120.0f);
                } else if (result[u] == 2) {
                    stateColors.put(u, 300.0f);
                }else {
                    stateColors.put(u, 30.0f);
                }

            }


        }
    }

    //Function for Depth first search + forward checking
    private void function_second_algo(String parsedStateData, String country) {

    }


    //Depth first search + forward checking + propagation through singleton domains
    private void function_third_algo(String parsedStateData, String country) {

    }

    //Function for Depth first search + forward checking + propagation through reduced domain
    private void function_fourth_algo(String parsedStateData, String country) {


    }

    private String LoadJSONfromAsset() {

        String json = null;
        try {

            InputStream is = getContext().getAssets().open("LatLngData.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.d("demo","Error is : "+ex.toString());
            return null;
        }
        return json;
    }

    private boolean isEverythingFilled() {

        boolean flag=true;

        if (country_spinner.getSelectedItemPosition()==0){
            ((TextView)country_spinner.getSelectedView()).setError("Please select a country");
            flag=false;
        }

        if (algorithm_spinner.getSelectedItemPosition()==0){
            ((TextView)algorithm_spinner.getSelectedView()).setError("Please select an algorithm");
            flag=false;
        }

        if (flag==true){
            return true;
        }else{
            return false;
        }

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap=map;
        LatLng latLng=new LatLng(39,98);
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //googleMap.setOnInfoWindowClickListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    private Graph createGraph(String Country) {
        if (Country.equals("USA")){
            Graph USGraph=new Graph(50);
            USGraph.addEdge(0,1);
            USGraph.addEdge(0,2);
            USGraph.addEdge(1,2);
            USGraph.addEdge(4,36);
            USGraph.addEdge(36,46);
            USGraph.addEdge(36,32);
            USGraph.addEdge(32,3);
            USGraph.addEdge(32,4);
            USGraph.addEdge(4,3);
            USGraph.addEdge(3,43);
            USGraph.addEdge(43,12);
            USGraph.addEdge(46,12);
            USGraph.addEdge(36,12);
            USGraph.addEdge(32,12);
            USGraph.addEdge(3,31);
            USGraph.addEdge(31,5);
            USGraph.addEdge(32,43);
            USGraph.addEdge(43,5);
            USGraph.addEdge(43,49);
            USGraph.addEdge(5,49);
            USGraph.addEdge(49,25);
            USGraph.addEdge(12,49);
            USGraph.addEdge(12,25);
            USGraph.addEdge(25,27);
            USGraph.addEdge(27,40);
            USGraph.addEdge(40,25);
            USGraph.addEdge(49,40);
            USGraph.addEdge(49,28);
            USGraph.addEdge(28,40);
            USGraph.addEdge(28,5);
            USGraph.addEdge(28,15);
            USGraph.addEdge(15,5);
            USGraph.addEdge(5,35);
            USGraph.addEdge(15,35);
            USGraph.addEdge(31,35);
            USGraph.addEdge(31,42);
            USGraph.addEdge(35,42);
            USGraph.addEdge(42,2);
            USGraph.addEdge(2,17);
            USGraph.addEdge(17,42);
            USGraph.addEdge(17,18);
            USGraph.addEdge(18,2);
            USGraph.addEdge(2,35);
            USGraph.addEdge(35,23);
            USGraph.addEdge(23,15);
            USGraph.addEdge(23,28);
            USGraph.addEdge(23,2);
            USGraph.addEdge(23,11);
            USGraph.addEdge(11,28);
            USGraph.addEdge(11,40);
            USGraph.addEdge(11,22);
            USGraph.addEdge(22,40);
            USGraph.addEdge(22,27);
            USGraph.addEdge(22,47);
            USGraph.addEdge(47,11);
            USGraph.addEdge(47,13);
            USGraph.addEdge(13,11);
            USGraph.addEdge(13,23);
            USGraph.addEdge(23,16);
            USGraph.addEdge(23,41);
            USGraph.addEdge(41,2);
            USGraph.addEdge(41,24);
            USGraph.addEdge(24,1);
            USGraph.addEdge(24,2);
            USGraph.addEdge(24,17);
            USGraph.addEdge(1,41);
            USGraph.addEdge(41,16);
            USGraph.addEdge(16,13);
            USGraph.addEdge(16,14);
            USGraph.addEdge(14,21);
            USGraph.addEdge(14,13);
            USGraph.addEdge(21,47);
            USGraph.addEdge(21,34);
            USGraph.addEdge(34,14);
            USGraph.addEdge(34,16);
            USGraph.addEdge(34,48);
            USGraph.addEdge(48,16);
            USGraph.addEdge(16,44);
            USGraph.addEdge(44,48);
            USGraph.addEdge(44,41);
            USGraph.addEdge(44,19);
            USGraph.addEdge(44,26);
            USGraph.addEdge(26,41);
            USGraph.addEdge(41,9);
            USGraph.addEdge(9,1);
            USGraph.addEdge(1,8);
            USGraph.addEdge(8,9);
            USGraph.addEdge(9,39);
            USGraph.addEdge(39,26);
            USGraph.addEdge(26,9);
            USGraph.addEdge(48,19);
            USGraph.addEdge(19,37);
            USGraph.addEdge(37,34);
            USGraph.addEdge(37,48);
            USGraph.addEdge(37,7);
            USGraph.addEdge(7,19);
            USGraph.addEdge(7,30);
            USGraph.addEdge(30,37);
            USGraph.addEdge(30,33);
            USGraph.addEdge(33,37);
            USGraph.addEdge(33,45);
            USGraph.addEdge(33,18);
            USGraph.addEdge(18,45);
            USGraph.addEdge(45,29);
            USGraph.addEdge(29,18);
            USGraph.addEdge(29,20);
            USGraph.addEdge(18,38);
            USGraph.addEdge(38,6);
            USGraph.addEdge(6,33);
            USGraph.addEdge(6,18);

            return USGraph;
        }
        else if(Country.equals("Australia")){
            Graph AusGraph=new Graph(7);
            AusGraph.addEdge(0,2);
            AusGraph.addEdge(0,5);
            AusGraph.addEdge(0,3);

            AusGraph.addEdge(3,1);
            AusGraph.addEdge(3,6);
            AusGraph.addEdge(3,2);

            AusGraph.addEdge(1,2);
            AusGraph.addEdge(1,6);

            return AusGraph;
        }
        else {
            return new Graph(0);
        }


    }

    private void dropPinEffect(final Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;

        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();

        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 5);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }

}
