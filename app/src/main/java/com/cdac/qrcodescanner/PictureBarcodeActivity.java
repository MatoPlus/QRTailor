package com.cdac.qrcodescanner;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
//import com.google.android.gms.common.api.Response;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.json.*;


public class PictureBarcodeActivity extends AppCompatActivity implements View.OnClickListener {

    TextView textViewResultBody;
    ImageView imageView;
    private BarcodeDetector detector;
    private Uri imageUri;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private static final int CAMERA_REQUEST = 101;
    private static final String TAG = "QR_CODE_SCANNER";
    private String manufacturer;
    private String decodedID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_barcode);

        // Get variables from main activity, where this activity is called.
        Intent intent = getIntent();

        // Save appropriate variables retrieved from main to display current user settings.
        manufacturer = intent.getStringExtra("manufacturer");



        initComponents();
    }

    private void initComponents(){
        textViewResultBody = findViewById(R.id.itemName);
        imageView = findViewById(R.id.imageView);
        findViewById(R.id.buttonOpenCamera).setOnClickListener(this);

        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        if (!detector.isOperational()) {
            textViewResultBody.setText("Detector initialisation failed");
            return;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonOpenCamera:


                ActivityCompat.requestPermissions(PictureBarcodeActivity.this, new
                        String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    takeBarcodePicture();
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void takeBarcodePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), "barcode.jpg");
        imageUri = FileProvider.getUriForFile(PictureBarcodeActivity.this,
                BuildConfig.APPLICATION_ID + ".provider", photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_REQUEST);
    }

    private void launchMediaScanIntent() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(imageUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private Bitmap decodeBitmapUri(Context ctx, Uri uri) throws FileNotFoundException {
        int targetW = 600;
        int targetH = 600;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(ctx.getContentResolver().openInputStream(uri), null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        return BitmapFactory.decodeStream(ctx.getContentResolver()
                .openInputStream(uri), null, bmOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            launchMediaScanIntent();
            try {
                Bitmap bitmap = decodeBitmapUri(this, imageUri);
                if (detector.isOperational() && bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<Barcode> barCodes = detector.detect(frame);
                    setBarCode(barCodes);
                } else {
                    textViewResultBody.setText("Detector initialisation failed");
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to load Image", Toast.LENGTH_SHORT)
                        .show();
                Log.e(TAG, e.toString());
            }
        }
    }

    private String getName(String response, String manufacturer, String itemID) {

        try {
            JSONObject obj = new JSONObject(response);
            String name = obj.getJSONArray(manufacturer).getJSONObject(0).getJSONArray(itemID).getJSONObject(0).getString("name");
            Log.i("json", name);
            return name;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";

    }

    private String getDescription(String response, String manufacturer, String itemID) {

        try {
            JSONObject obj = new JSONObject(response);
            String description = obj.getJSONArray(manufacturer).getJSONObject(0).getJSONArray(itemID).getJSONObject(0).getString("description");
            Log.i("json", description);
            return description;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";

    }

    private String getQuantity(String response, String manufacturer, String itemID) {

        try {
            JSONObject obj = new JSONObject(response);
            String quantity = obj.getJSONArray(manufacturer).getJSONObject(0).getJSONArray(itemID).getJSONObject(0).getString("quantity");
            Log.i("json", quantity);
            return quantity;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";

    }


    private String getColour(String response, String manufacturer, String itemID) {

        try {
            JSONObject obj = new JSONObject(response);
            String colour = obj.getJSONArray(manufacturer).getJSONObject(0).getJSONArray(itemID).getJSONObject(0).getString("colour");
            Log.i("json", colour);
            return colour;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";

    }
    private String getReview(String response, String manufacturer, String itemID) {

        try {
            JSONObject obj = new JSONObject(response);
            String review = obj.getJSONArray(manufacturer).getJSONObject(0).getJSONArray(itemID).getJSONObject(0).getString("review");
            Log.i("json", review);
            return review;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";

    }

    private String getStatus(String response, String manufacturer, String itemID) {

        try {
            JSONObject obj = new JSONObject(response);
            String status = obj.getJSONArray(manufacturer).getJSONObject(0).getJSONArray(itemID).getJSONObject(0).getString("online");
            Log.i("json", status);
            return status;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";

    }

    private String getPrice(String response, String manufacturer, String itemID) {

        try {
            JSONObject obj = new JSONObject(response);
            String price = obj.getJSONArray(manufacturer).getJSONObject(0).getJSONArray(itemID).getJSONObject(0).getString("price");
            Log.i("json", price);
            return price;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";

    }

    private String getSize(String response, String manufacturer, String itemID) {

        try {
            JSONObject obj = new JSONObject(response);
            String size = obj.getJSONArray(manufacturer).getJSONObject(0).getJSONArray(itemID).getJSONObject(0).getString("size");
            Log.i("json", size);
            return size;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";

    }

    private String getOtherColours(String response, String manufacturer, String itemID) {

        try {
            JSONObject obj = new JSONObject(response);
            String colours = obj.getJSONArray(manufacturer).getJSONObject(0).getJSONArray(itemID).getJSONObject(0).getString("otherColours");
            Log.i("json", colours);
            return colours;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";

    }

    private String getImageURL(String response, String manufacturer, String itemID) {

        try {
            JSONObject obj = new JSONObject(response);
            String imageURL = obj.getJSONArray(manufacturer).getJSONObject(0).getJSONArray(itemID).getJSONObject(0).getString("image");
            Log.i("json", imageURL);
            return imageURL;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return "";

    }

    private void setBarCode(SparseArray<Barcode> barCodes){

        if (barCodes.size() == 0) {
            textViewResultBody.setText("No barcode could be detected. Please try again.");
            return;
        }
        for (int index = 0; index < barCodes.size(); index++) {
            Barcode code = barCodes.valueAt(index);
            textViewResultBody.setText(code.displayValue);
            decodedID = code.displayValue;
            copyToClipBoard(code.displayValue);
            int type = barCodes.valueAt(index).valueFormat;
            switch (type) {
                case Barcode.CONTACT_INFO:
                    Log.i(TAG, code.contactInfo.title);
                    break;
                case Barcode.EMAIL:
                    Log.i(TAG, code.displayValue);
                    break;
                case Barcode.ISBN:
                    Log.i(TAG, code.rawValue);
                    break;
                case Barcode.PHONE:
                    Log.i(TAG, code.phone.number);
                    break;
                case Barcode.PRODUCT:
                    Log.i(TAG, code.rawValue);
                    break;
                case Barcode.SMS:
                    Log.i(TAG, code.sms.message);
                    break;
                case Barcode.TEXT:
                    Log.i(TAG, code.displayValue);
                    break;
                case Barcode.URL:
                    Log.i(TAG, "url: " + code.displayValue);
                    break;
                case Barcode.WIFI:
                    Log.i(TAG, code.wifi.ssid);
                    break;
                case Barcode.GEO:
                    Log.i(TAG, code.geoPoint.lat + ":" + code.geoPoint.lng);
                    break;
                case Barcode.CALENDAR_EVENT:
                    Log.i(TAG, code.calendarEvent.description);
                    break;
                case Barcode.DRIVER_LICENSE:
                    Log.i(TAG, code.driverLicense.licenseNumber);
                    break;
                default:
                    Log.i(TAG, code.rawValue);
                    break;
            }

            Log.i("test", "Entered");

            final TextView result = findViewById(R.id.textViewResultsHeader);
            final TextView itemName = findViewById(R.id.itemName);
            final TextView itemDescription = findViewById(R.id.itemDescription);
            final TextView itemQuantity = findViewById(R.id.itemQuantity);
            final TextView itemColour = findViewById(R.id.itemColour);
            final TextView itemReview = findViewById(R.id.itemReview);
            final TextView itemStatus = findViewById(R.id.itemStatus);
            final TextView itemPrice = findViewById(R.id.itemPrice);
            final TextView itemSize = findViewById(R.id.itemSize);
            final TextView itemOtherColours = findViewById(R.id.itemOtherColours);

            result.setVisibility(View.VISIBLE);
            itemName.setVisibility(View.VISIBLE);
            itemDescription.setVisibility(View.VISIBLE);
            itemQuantity.setVisibility(View.VISIBLE);
            itemColour.setVisibility(View.VISIBLE);
            itemReview.setVisibility(View.VISIBLE);
            itemStatus.setVisibility(View.VISIBLE);
            itemPrice.setVisibility(View.VISIBLE);
            itemSize.setVisibility(View.VISIBLE);
            itemOtherColours.setVisibility(View.VISIBLE);

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
                            itemName.setText("Name: " + getName(response, manufacturer, decodedID));
                            itemDescription.setText("Description: " + getDescription(response, manufacturer, decodedID));
                            itemQuantity.setText("Quantity: " + getQuantity(response, manufacturer, decodedID));
                            itemColour.setText("Colour: " + getColour(response, manufacturer, decodedID));
                            itemReview.setText("Review: " + getReview(response, manufacturer, decodedID));
                            itemStatus.setText("Status: " + getStatus(response, manufacturer, decodedID));
                            itemPrice.setText("Price: " + getPrice(response, manufacturer, decodedID));
                            itemSize.setText("Size: " + getSize(response, manufacturer, decodedID));
                            itemOtherColours.setText("Available Colours: " + getOtherColours(response, manufacturer, decodedID));
                            Picasso.get().load(getImageURL(response, manufacturer, decodedID)).into(imageView);

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
    }

    private void copyToClipBoard(String text){
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("QR code Scanner", text);
        clipboard.setPrimaryClip(clip);
    }
}