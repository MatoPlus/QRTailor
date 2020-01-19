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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String[] items = null;

    public String[] convertIteratorToArray(Iterator<String> iterator) {
        ArrayList<String> list = new ArrayList<String>();
        // Add each element of iterator to the List
        iterator.forEachRemaining(list::add);

    String arr[] = new String[list.size()];

      for(int i = 0; i < list.size(); ++i) {
        arr[i] = list.get(i);
    }

    // Return the List
      return arr;
}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();


        //Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://10.37.132.191:5000/products";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject obj = new JSONObject(response);
                            Iterator<String> keys = obj.keys();

//                            while (keys.hasNext()) {
//                                Log.i("json", keys.next());
//                            }

                            items = convertIteratorToArray(keys);

                            //get the spinner from the xml.
                            Spinner dropdown = findViewById(R.id.spinner);
                            //create a list of items for the spinner.


                            //create an adapter to describe how the items are displayed, adapters are used in several places in android.
                            //There are multiple variations of this, but this is the basic variant.
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.super.getApplication(), android.R.layout.simple_spinner_dropdown_item, items);

                            //set the spinners adapter to the previously created one.
                            dropdown.setAdapter(adapter);

                            initComponents();

                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("test", "Didn't work");
            }
        });


        // Add the request to the RequestQueue.
        queue.add(stringRequest);

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
