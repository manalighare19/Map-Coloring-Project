package com.example.manalighare.mapColoring;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Home");


        getSupportFragmentManager().beginTransaction()
                .add(R.id.FragmentContainer,new MapColoringFragment(),"Map Coloring")
                .commit();


    }

}
