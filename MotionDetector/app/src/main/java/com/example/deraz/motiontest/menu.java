package com.example.deraz.motiontest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by deraz on 08/02/2016.
 */
public class menu extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
    }
    public void startmotion(View view){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
