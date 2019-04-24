package com.example.manalighare.mapColoring;


import android.graphics.Color;
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
import java.time.LocalDateTime;
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

    int[] stackFc=new int[7];
    int[] stackUSAFc=new int[50];

    int fcColor[];
    int USAfcColor[];

    HashMap<Integer, Float> stateColors=new HashMap<Integer, Float>();

    private ArrayList<Point> Map_points=new ArrayList<>();
    private ArrayList<PointFc> Map_pointsFC=new ArrayList<>();
    private ArrayList<PointUSAFc> Map_pointsUSAFc=new ArrayList<>();

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

               Map_points.clear();
               stateColors=new HashMap<>();
               Map_pointsFC.clear();
               Map_pointsUSAFc.clear();
               googleMap.clear();

               if (isEverythingFilled()){

                   String jsonString=LoadJSONfromAsset();
                   String fcjsonString=LoadJSONfromAssetFC();
                   String USAFcjsonString=LoadJSONfromAssetUSAFC();


                   ParseJsonString(jsonString,country_spinner.getSelectedItem().toString());
                   ParseJsonStringFC(fcjsonString,"Australia");
                   ParseJsonStringUSAFC(USAFcjsonString,"USA");



                   switch (algorithm_spinner.getSelectedItemPosition()){

                       case 1:
                           long startTime = System.currentTimeMillis();

                           Graph g=createGraph(country_spinner.getSelectedItem().toString());
                           function_first_algo(country_spinner.getSelectedItem().toString(),g,v);
                           long stopTime = System.currentTimeMillis();
                           long elapsedTime = stopTime - startTime;
                           Log.d("elaspedalgo1", "onClick: "+elapsedTime);
                           showColors(stateColors,country_spinner.getSelectedItem().toString());

                           break;

                       case 2:
                           long startTime1 = System.currentTimeMillis();
                           function_second_algo(country_spinner.getSelectedItem().toString());
                           long stopTime1 = System.currentTimeMillis();
                           long elapsedTime1 = stopTime1 - startTime1;
                           Log.d("elaspedalgo2", "onClick: "+elapsedTime1);
                           showColorsFC(country_spinner.getSelectedItem().toString());

                           break;

                       case 3:
                           long startTime2 = System.currentTimeMillis();
                           function_third_algo(country_spinner.getSelectedItem().toString());
                           //showColorsFC(country_spinner.getSelectedItem().toString());
                           long stopTime2 = System.currentTimeMillis();
                           long elapsedTime2 = stopTime2 - startTime2;
                           Log.d("elaspedalgo3", "onClick: "+elapsedTime2);
                           showColorsFCSingleton(country_spinner.getSelectedItem().toString());

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



    private void ParseJsonStringUSAFC(String usaFcjsonString, String selected_country) {

        try {
            JSONObject root=new JSONObject(usaFcjsonString);
            JSONArray points=root.getJSONArray(selected_country);

            Gson gson=new Gson();
            PointUSAFc pfc;

            for (int i=0;i<points.length();i++){

                pfc=new PointUSAFc();
                pfc.setStateID(points.getJSONObject(i).getInt("stateID"));
                pfc.setState(points.getJSONObject(i).getString("state"));
                pfc.setColor(points.getJSONObject(i).getInt("color"));
                pfc.setLatitude(points.getJSONObject(i).getDouble("latitude"));
                pfc.setLongitude(points.getJSONObject(i).getDouble("longitude"));
                //Log.d("json ", "ParseJsonStringFC: "+points.getJSONObject(i).getString("domain"));
                JSONArray domainRoot = new JSONArray(points.getJSONObject(i).getString("domain"));
                Log.d("domain", "ParseJsonStringFC: "+domainRoot.getJSONObject(0).getString("r"));
                DomainUSA d1=new DomainUSA();
                d1.setR(domainRoot.getJSONObject(0).getString("r"));
                d1.setG(domainRoot.getJSONObject(1).getString("g"));
                d1.setB(domainRoot.getJSONObject(2).getString("b"));
                d1.setY(domainRoot.getJSONObject(3).getString("y"));
                d1.setY(domainRoot.getJSONObject(4).getString("o"));

                pfc.setDomain(d1);
//
//                for(int j=0; j<domainValues.length();j++) {
//                    pfc.setDomain(domainValues.getJSONObject(j).getString("domain"));
//                }
                Map_pointsUSAFc.add(pfc);



            }

        } catch (JSONException e) {
            e.printStackTrace();
        }



    }


    private void ParseJsonStringFC(String jsonString, String selected_country) {

        try {
            JSONObject root=new JSONObject(jsonString);
            JSONArray points=root.getJSONArray(selected_country);

            Gson gson=new Gson();
            PointFc pfc;

            for (int i=0;i<points.length();i++){

                pfc=new PointFc();
                pfc.setStateID(points.getJSONObject(i).getInt("stateID"));
                pfc.setState(points.getJSONObject(i).getString("state"));
                pfc.setColor(points.getJSONObject(i).getInt("color"));
                pfc.setLatitude(points.getJSONObject(i).getDouble("latitude"));
                pfc.setLongitude(points.getJSONObject(i).getDouble("longitude"));
                pfc.setSingletonDomain(points.getJSONObject(i).getInt("singletonDomain"));
                //Log.d("json ", "ParseJsonStringFC: "+points.getJSONObject(i).getString("domain"));
                JSONArray domainRoot = new JSONArray(points.getJSONObject(i).getString("domain"));
                Log.d("domain", "ParseJsonStringFC: "+domainRoot.getJSONObject(0).getString("r"));
                Domain d1=new Domain();
                d1.setR(domainRoot.getJSONObject(0).getString("r"));
                d1.setG(domainRoot.getJSONObject(1).getString("g"));
                d1.setB(domainRoot.getJSONObject(2).getString("b"));
                d1.setY(domainRoot.getJSONObject(3).getString("y"));

              pfc.setDomain(d1);
//
//                for(int j=0; j<domainValues.length();j++) {
//                    pfc.setDomain(domainValues.getJSONObject(j).getString("domain"));
//                }
                Map_pointsFC.add(pfc);



            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private int[][] createGraphFC(String selected_country) {


        if (selected_country.equals("Australia")){
            int Ausgraph[][] = {{0,1,0,0,0,0,1},
                    {1,0,0,1,0,1,1},
                    {0,0,0,1,0,0,1},
                    {0,1,1,0,0,1,1},
                    {0,0,0,0,0,1,0},
                    {0,1,0,1,1,0,1},
                    {1,1,1,1,0,1,0},
            };

            return Ausgraph;
        }else{

            int USGraph[][]={
                    {0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0}, //Alabama
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Alaska
                    {0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0}, //Arizona
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,1,0,0,0,0,0,0,0}, //Arkansas
                    {0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0}, //California
                    {0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,1}, //Colorado
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0}, //Connecticut
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0}, //Delaware
                    {1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Florida
                    {1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0}, //Georgia
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Hawaii
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,1,0,0,1}, //Idaho
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,1,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0}, //Illinois
                    {0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Indiana
                    {0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0}, //Iowa
                    {0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Kansas
                    {0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,1,0,1,0,0}, //Kentucky
                    {0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0}, //Louisiana
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Maine
                    {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,1,0,0}, //Maryland
                    {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0}, //Massachusetts
                    {0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0}, //Michigan
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0}, //Minnesota
                    {1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0}, //Mississippi
                    {0,0,0,1,0,0,0,0,0,0,0,0,1,0,1,1,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0}, //Missouri
                    {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1}, //Montana
                    {0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1}, //Nebraska
                    {0,0,1,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0}, //Nevada
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0}, //New Hampshire
                    {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0}, //New jersey
                    {0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0}, //New Mexico
                    {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,1,0,0,0,0,0,1,0,0,0,0,0}, //New york
                    {0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0}, //North Carolina
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0}, //North Dakota
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0}, //Ohio
                    {0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0}, //Oklahoma
                    {0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0}, //Oregon
                    {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Pennsylvania
                    {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Rhode Island
                    {0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, // South Carolina
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1}, //South Dakota
                    {1,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0}, //Tennessee
                    {0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Texas
                    {0,0,1,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Utah
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Vermont
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0}, //Virginia
                    {0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Washington
                    {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0}, //West Virginia
                    {0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, //Wisconsin
                    {0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0}, //Wyoming

            };

            return USGraph;
        }




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
    private void showColorsFCSingleton(String s) {



        Marker marker;

        if (s.equals("USA")) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(39.0646, -105.3272)));
            ArrayList<PointUSAFc> allStates=Map_pointsUSAFc;
            for (int i=0;i< allStates.size();i++){

                if(allStates.get(i).getColor()==1) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(0.0f)));
                }
                else  if(allStates.get(i).getColor()==2) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(120.0f))
                    );
                }
                else if(allStates.get(i).getColor()==3) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(240.0f))
                    );
                }
                else if(allStates.get(i).getColor()==4) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(60.0f))
                    );
                }
                else
                {
                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(30.0f))
                    );

                }

                dropPinEffect(marker);

            }

        }else if (s.equals("Australia")){
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(-34.28293455, 140.6000378)));

            ArrayList<PointFc> allStates=Map_pointsFC;
            for (int i=0;i< allStates.size();i++){

                if(allStates.get(i).getColor()==1) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(0.0f)));
                }
                else  if(allStates.get(i).getColor()==2) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(120.0f))
                    );
                }
                else
                if(allStates.get(i).getColor()==3) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(240.0f))
                    );
                }

                else
                {
                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(60.0f))
                    );

                }

                dropPinEffect(marker);

            }
        }


    }



    private void showColorsFC(String s) {



        Marker marker;

        if (s.equals("USA")) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(39.0646, -105.3272)));
            ArrayList<PointUSAFc> allStates=Map_pointsUSAFc;
            for (int i=0;i< allStates.size();i++){

                if(allStates.get(i).getColor()==1) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(0.0f)));
                }
                else  if(allStates.get(i).getColor()==2) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(120.0f))
                    );
                }
                else if(allStates.get(i).getColor()==3) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(240.0f))
                    );
                }
                else if(allStates.get(i).getColor()==4) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(60.0f))
                    );
                }
                else
                {
                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(30.0f))
                    );

                }

                dropPinEffect(marker);

            }

        }else if (s.equals("Australia")){
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(-34.28293455, 140.6000378)));

            ArrayList<PointFc> allStates=Map_pointsFC;
            for (int i=0;i< allStates.size();i++){

                if(allStates.get(i).getColor()==1) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(0.0f)));
                }
                else  if(allStates.get(i).getColor()==2) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(120.0f))
                    );
                }
                else if(allStates.get(i).getColor()==3) {

                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(240.0f))
                    );
                }

                else
                {
                    marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(allStates.get(i).getLatitude()
                            ,allStates.get(i).getLongitude())).title(allStates.get(i).getState()).icon(BitmapDescriptorFactory.defaultMarker(60.0f))
                    );

                }

                dropPinEffect(marker);

            }
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
    private void function_second_algo(String selected_country) {

        int[][] g=createGraphFC(selected_country);

        if (selected_country.equals("Australia")){
            graphColoringAusFC(g,4);
        }else if(selected_country.equals("USA")){
            graphColoringforUSAFC(g,5);
        }


        /*for (int i =0;i<Map_pointsFC.size();i++){
            Log.d("demo", "function_second_algo: "+Map_pointsFC.get(i).toString());
        }*/


    }


    //Depth first search + forward checking + propagation through singleton domains
    private void function_third_algo(String selected_country) {

        int[][] g=createGraphFC(selected_country);

        if (selected_country.equals("Australia")){
            fcColor = new int[7];
            for (int i = 0; i < 7; i++)
                fcColor[i] = 0;
            singletonDomainAus(g, 4,fcColor, 0);
            }else if(selected_country.equals("USA")){
            USAfcColor = new int[50];
            for (int i = 0; i < 50; i++)
                USAfcColor[i] = 0;
            singletonDomainUS(g, 5,USAfcColor, 0);
            //graphColoringforUSAFC(g,5);
        }
    }



    private Boolean singletonDomainUS(int[][] graph, int m, int[] fcColor, int v)
    {
        if (v == 50){
            return true;
        }
        for(int c=1;c<=5;c++)
        {
            if (isSafe1(v, graph, fcColor,  c))
            {

//            Map_pointsFC.get(v).setColor(Map_pointsFC.get(v).getSingletonDomain());
                int colorAssigned =  c;
                // Map_pointsFC.get(v).setColor(colorAssigned);
                for (int i = 0; i < 50; i++) {
                    if (graph[v][i] == 1  && !stackUSAFc.equals(colorAssigned))
                    {
                        fcColor[v] = c;
                        Map_pointsUSAFc.get(i).setColor(colorAssigned);
                        if (colorAssigned == 1) {
                            Map_pointsUSAFc.get(i).getDomain().setR("r");
                        } else if (colorAssigned == 2) {
                            Map_pointsUSAFc.get(i).getDomain().setG("g");
                        } else if (colorAssigned == 3) {
                            Map_pointsUSAFc.get(i).getDomain().setB("b");
                        } else  if(colorAssigned==4)
                        {
                            Map_pointsUSAFc.get(i).getDomain().setY("y");
                        }
                        else
                        {
                            Map_pointsUSAFc.get(i).getDomain().setO("o");
                        }
                        stackUSAFc[i] = colorAssigned;
                    }
                    if((Map_pointsUSAFc.get(i).getDomain()==null )
                            && Map_pointsUSAFc.get(i).getColor()==-1) {
                        singletonUS(graph,i,fcColor,c);
                    }
                }

                if (singletonDomainUS(graph, m,
                        fcColor, v + 1))
                    return true;
            }
        }
        return false;
    }
    private void singletonUS(int[][] graph, int v,int[] fcColor,int c) {

        int colorAssigned = c;
        for (int i = 0; i < 50; i++) {
            if (graph[v][i] == 1 && !stackUSAFc.equals(colorAssigned))  {
                Map_pointsUSAFc.get(i).setColor(colorAssigned);
                fcColor[v] = c;
                if (colorAssigned == 1) {
                    Map_pointsUSAFc.get(i).getDomain().setR("r");
                } else if (colorAssigned == 2) {
                    Map_pointsUSAFc.get(i).getDomain().setG("g");
                } else if (colorAssigned == 3) {
                    Map_pointsUSAFc.get(i).getDomain().setB("b");
                } else if (colorAssigned == 4){
                    Map_pointsUSAFc.get(i).getDomain().setY("y");
                }
                else {
                    Map_pointsUSAFc.get(i).getDomain().setO("o");
                }
                stackUSAFc[i] = colorAssigned;
            }
            if((Map_pointsUSAFc.get(i).getDomain()==null)
                    && Map_pointsUSAFc.get(i).getColor()==-1) {
                singletonUS(graph,i,fcColor, c);
            }
        }
    }

    private Boolean singletonDomainAus(int[][] graph, int m, int[] fcColor, int v)
    {

        if (v == 7){
            return true;
        }

for(int c=1;c<=4;c++)
{
        if (isSafe(v, graph, fcColor,  c))
        {

//            Map_pointsFC.get(v).setColor(Map_pointsFC.get(v).getSingletonDomain());
            int colorAssigned =  c;
           // Map_pointsFC.get(v).setColor(colorAssigned);
            for (int i = 0; i < 7; i++) {
                if (graph[v][i] != 1 )
                {
                    fcColor[v] = c;
                    Map_pointsFC.get(i).setColor(colorAssigned);
                            if (colorAssigned == 1) {
                                Map_pointsFC.get(i).getDomain().setR("r");
                            } else if (colorAssigned == 2) {
                                Map_pointsFC.get(i).getDomain().setG("g");
                            } else if (colorAssigned == 3) {
                                Map_pointsFC.get(i).getDomain().setB("b");
                            } else {
                                Map_pointsFC.get(i).getDomain().setY("y");
                            }
                            stackFc[i] = colorAssigned;
                }
                if((Map_pointsFC.get(i).getDomain()==null )
                && Map_pointsFC.get(i).getColor()==-1) {
                    singletonAus(graph,i,fcColor,c);
                }
            }

            if (singletonDomainAus(graph, m,
                    fcColor, v + 1))
                return true;
        }
}
        return false;
    }

    private void singletonAus(int[][] graph, int v,int[] fcColor,int c) {

        int colorAssigned = c;
        for (int i = 0; i < 7; i++) {
            if (graph[v][i] != 1)  {
                Map_pointsFC.get(i).setColor(colorAssigned);
                fcColor[v] = c;
                if (colorAssigned == 1) {
                    Map_pointsFC.get(i).getDomain().setR("r");
                } else if (colorAssigned == 2) {
                    Map_pointsFC.get(i).getDomain().setG("g");
                } else if (colorAssigned == 3) {
                    Map_pointsFC.get(i).getDomain().setB("b");
                } else {
                    Map_pointsFC.get(i).getDomain().setY("y");
                }
                stackFc[i] = colorAssigned;
            }
            if((Map_pointsFC.get(i).getDomain()==null)
                    && Map_pointsFC.get(i).getColor()==-1) {

                singletonAus(graph,i,fcColor, c);



    }
        }
    }


    //Function for Depth first search + forward checking + propagation through reduced domain
    private void function_fourth_algo(String parsedStateData, String country) {


    }


    boolean graphColoringforUSAFC(int graph[][], int m)
    {
        // Initialize all color values as 0. This
        // initialization is needed correct functioning
        // of isSafe()
        USAfcColor = new int[50];
        for (int i = 0; i < 50; i++)
            USAfcColor[i] = 0;

        // Call graphColoringUtil() for vertex 0
        if (!graphColoringUtilforUSAFC(graph, m, USAfcColor, 0))
        {
            System.out.println("Solution does not exist");
            return false;
        }

        // Print the solution
        //printSolution(fcColor);
        return true;
    }



    boolean graphColoringAusFC(int graph[][], int m)
    {
        // Initialize all color values as 0. This
        // initialization is needed correct functioning
        // of isSafe()
        fcColor = new int[7];
        for (int i = 0; i < 7; i++)
            fcColor[i] = 0;

        // Call graphColoringUtil() for vertex 0
        if (!graphColoringUtil(graph, m, fcColor, 0))
        {
            System.out.println("Solution does not exist");
            return false;
        }

        // Print the solution
        //printSolution(fcColor);
        return true;
    }


    void printSolution(int color[])
    {

        for (int i = 0; i < 7; i++)
            Log.d("print", "graphColoring: "+fcColor[i]);
    }

    boolean isSafe(int v, int graph[][], int color[],
                   int c)
    {
        for (int i = 0; i < 7; i++)
            if (graph[v][i] == 1 && c == color[i])
                return false;
        return true;
    }
    boolean isSafe1(int v, int graph[][], int color[],
                   int c)
    {
        for (int i = 0; i < 50; i++)
            if (graph[v][i] == 1 && c == color[i])
                return false;
        return true;
    }



    /* A recursive utility function to solve m
    coloring problem */
    boolean graphColoringUtilforUSAFC(int graph[][], int m,
                              int color[], int v) {
        /* base case: If all vertices are assigned
        a color then return true */
        if (v == 50)
            return true;

       // Log.d("vertexxx", "graphColoringUtil: " + Map_pointsFC.get(0));
        if (Map_pointsUSAFc.get(v) == null) {

            //v--;
            int colortorestore = Map_pointsUSAFc.get(v).getColor();
            for (int i = 0; i < 50; i++) {
                if (graph[v][i] == 1) {
                    stackUSAFc[i] = colortorestore;

                    if (!Map_pointsUSAFc.get(i).getDomain().getR().equals(String.valueOf(colortorestore))
                            || !Map_pointsUSAFc.get(i).getDomain().getG().equals(String.valueOf(colortorestore))
                            || !Map_pointsUSAFc.get(i).getDomain().getB().equals(String.valueOf(colortorestore))
                            || !Map_pointsUSAFc.get(i).getDomain().getY().equals(String.valueOf(colortorestore))
                            || !Map_pointsUSAFc.get(i).getDomain().getO().equals(String.valueOf(colortorestore))) {

                        switch (colortorestore) {
                            case 'r':
                                Map_pointsUSAFc.get(i).getDomain().setR("r");
                                break;

                            case 'g':
                                Map_pointsUSAFc.get(i).getDomain().setG("g");
                                break;

                            case 'b':
                                Map_pointsUSAFc.get(i).getDomain().setB("b");
                                break;

                            case 'y':
                                Map_pointsUSAFc.get(i).getDomain().setY("y");
                                break;

                            case 'o':
                                Map_pointsUSAFc.get(i).getDomain().setO("o");
                                break;
                        }
                    }
                }
            }

        } else {

        /* Consider this vertex v and try different
        colors */

            for (int c = 1; c <= 5; c++) {
            /* Check if assignment of color c to v
            is fine*/

                if (isSafe1(v, graph, color, c)) {
                    color[v] = c;
                    int colorAssigned = c;

                    if (Map_pointsUSAFc.get(v).getColor() >= 0) {
                        int colortorestore = Map_pointsUSAFc.get(v).getColor();
                        Log.d("colorretsore", "graphColoringUtilforUSAFC: "+colortorestore);
                        for (int i = 0; i < 50; i++) {
                            if (graph[v][i] == 1) {
                                stackUSAFc[i] = colorAssigned;


                                if (!Map_pointsUSAFc.get(i).getDomain().getR().equals(String.valueOf(colortorestore))
                                        || !Map_pointsUSAFc.get(i).getDomain().getG().equals(String.valueOf(colortorestore))
                                        || !Map_pointsUSAFc.get(i).getDomain().getB().equals(String.valueOf(colortorestore))
                                        || !Map_pointsUSAFc.get(i).getDomain().getY().equals(String.valueOf(colortorestore))
                                        || !Map_pointsUSAFc.get(i).getDomain().getO().equals(String.valueOf(colortorestore))) {

                                    switch (colortorestore) {
                                        case 'r':
                                            Map_pointsUSAFc.get(i).getDomain().setR("r");
                                            break;

                                        case 'g':
                                            Map_pointsUSAFc.get(i).getDomain().setG("g");
                                            break;

                                        case 'b':
                                            Map_pointsUSAFc.get(i).getDomain().setB("b");
                                            break;

                                        case 'y':
                                            Map_pointsUSAFc.get(i).getDomain().setY("y");
                                            break;


                                        case 'o':
                                            Map_pointsUSAFc.get(i).getDomain().setO("o");
                                            break;
                                    }
                                }
                                // Log.d("map points: ", "graphColoringUtil: " + Map_pointsUSAFc.get(i).getDomain().toString());
                            }
                        }
                    }
                    Map_pointsUSAFc.get(v).setColor(colorAssigned);
                    for (int i = 0; i < 50; i++) {
                        if (graph[v][i] == 1) {
                            if (colorAssigned == 1) {
                                Map_pointsUSAFc.get(i).getDomain().setR("r");
                            } else if (colorAssigned == 2) {
                                Map_pointsUSAFc.get(i).getDomain().setG("g");
                            } else if (colorAssigned == 3) {
                                Map_pointsUSAFc.get(i).getDomain().setB("b");
                            } else if (colorAssigned == 4) {
                                Map_pointsUSAFc.get(i).getDomain().setY("y");
                            } else {
                                Map_pointsUSAFc.get(i).getDomain().setO("o");
                            }

                            stackUSAFc[i] = colorAssigned;
                        }
                    }
                    Log.d("hello", "graphColoringUtil: " + Map_pointsUSAFc.toString());

                /* recur to assign colors to rest
                of the vertices */
                    if (graphColoringUtilforUSAFC(graph, m,
                            color, v + 1))
                        return true;

                }
                    color[v] = -1;
                }
            }
            Log.d("hello1", "graphColoringUtil: " + Map_pointsUSAFc.toString());
        return false;
    }







    /* A recursive utility function to solve m
    coloring problem */
    boolean graphColoringUtil(int graph[][], int m,
                              int color[], int v) {
        /* base case: If all vertices are assigned
        a color then return true */
        if (v == 7)
            return true;

        Log.d("vertexxx", "graphColoringUtil: " + Map_pointsFC.get(0));
        if (Map_pointsFC.get(v) == null) {

            //v--;
            int colortorestore = Map_pointsFC.get(v).getColor();
            for (int i = 0; i < 7; i++) {
                if (graph[v][i] == 1) {
                    stackFc[i] = colortorestore;

                    if (!Map_pointsFC.get(i).getDomain().getR().equals(String.valueOf(colortorestore)) || !Map_pointsFC.get(i).getDomain().getG().equals(String.valueOf(colortorestore))
                            || !Map_pointsFC.get(i).getDomain().getB().equals(String.valueOf(colortorestore)) || !Map_pointsFC.get(i).getDomain().getY().equals(String.valueOf(colortorestore))) {
                        char a;
                        switch (colortorestore) {
                            case 'r':
                                Map_pointsFC.get(i).getDomain().setR("r");
                                break;

                            case 'g':
                                Map_pointsFC.get(i).getDomain().setG("g");
                                break;

                            case 'b':
                                Map_pointsFC.get(i).getDomain().setB("b");
                                break;

                            case 'y':
                                Map_pointsFC.get(i).getDomain().setY("y");
                                break;
                        }
                    }
                }
            }

        } else {

        /* Consider this vertex v and try different
        colors */

            for (int c = 1; c <= 4; c++) {
            /* Check if assignment of color c to v
            is fine*/

                if (isSafe(v, graph, color, c)) {
                    color[v] = c;
                    int colorAssigned = c;

                    if (Map_pointsFC.get(v).getColor() >= 0) {
                        int colortorestore = Map_pointsFC.get(v).getColor();

                        for (int i = 0; i < 7; i++) {
                            if (graph[v][i] == 1) {
                                stackFc[i] = colorAssigned;


                                if (!Map_pointsFC.get(i).getDomain().getR().equals(String.valueOf(colortorestore)) || !Map_pointsFC.get(i).getDomain().getG().equals(String.valueOf(colortorestore))
                                        || !Map_pointsFC.get(i).getDomain().getB().equals(String.valueOf(colortorestore)) || !Map_pointsFC.get(i).getDomain().getY().equals(String.valueOf(colortorestore))) {
                                    char a;
                                    switch (colortorestore) {
                                        case 'r':
                                            Map_pointsFC.get(i).getDomain().setR("r");
                                            break;

                                        case 'g':
                                            Map_pointsFC.get(i).getDomain().setG("g");
                                            break;

                                        case 'b':
                                            Map_pointsFC.get(i).getDomain().setB("b");
                                            break;

                                        case 'y':
                                            Map_pointsFC.get(i).getDomain().setY("y");
                                            break;
                                    }
                                }
                                Log.d("map points: ", "graphColoringUtil: " + Map_pointsFC.get(i).getDomain().toString());
                            }
                        }
                    }
                    Map_pointsFC.get(v).setColor(colorAssigned);
                    for (int i = 0; i < 7; i++) {
                        if (graph[v][i] == 1) {
                            if (colorAssigned == 1) {
                                Map_pointsFC.get(i).getDomain().setR("r");
                            } else if (colorAssigned == 2) {
                                Map_pointsFC.get(i).getDomain().setG("g");
                            } else if (colorAssigned == 3) {
                                Map_pointsFC.get(i).getDomain().setB("b");
                            } else {
                                Map_pointsFC.get(i).getDomain().setY("y");
                            }

                            stackFc[i] = colorAssigned;
                        }
                    }
                    //  Log.d("hello", "graphColoringUtil: " + Map_pointsFC.toString());

                /* recur to assign colors to rest
                of the vertices */
                    if (graphColoringUtil(graph, m,
                            color, v + 1))
                        return true;

                }
                    color[v] = -1;

            }

            Log.d("hello", "graphColoringUtil: " + Map_pointsFC.toString());
        }
        return false;
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


    private String LoadJSONfromAssetUSAFC() {

        String json = null;
        try {

            InputStream is = getContext().getAssets().open("USAFcJson.json");
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


    private String LoadJSONfromAssetFC() {

        String json = null;
        try {

            InputStream is = getContext().getAssets().open("fcJson.json");
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
