package com.cdac.qrcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] items = new String[]{"hollister", "nike", "tommyHilfiger"};

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://10.35.129.199:5000/products";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i("test", "Response is: "+ response.substring(0,500));



                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("test", "Didn't work");
            }
        });

        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.spinner);
        //create a list of items for the spinner.

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);

        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        initComponents();
    }

    private void initComponents(){
        findViewById(R.id.buttonTakePicture).setOnClickListener(this);
//        findViewById(R.id.buttonScanBarcode).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
//            case R.id.buttonScanBarcode:
//                startActivity(new Intent(this,ScannerBarcodeActivity.class));
//                break;
            case R.id.buttonTakePicture:
                Spinner dropdown = findViewById(R.id.spinner);
                Log.i("initial", dropdown.getSelectedItem().toString());

                Intent intent = new Intent(this,PictureBarcodeActivity.class);
                intent.putExtra("manufacturer", dropdown.getSelectedItem().toString());

                startActivity(intent);
                break;
        }
    }
}