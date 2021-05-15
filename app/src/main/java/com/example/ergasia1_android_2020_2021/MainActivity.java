package com.example.ergasia1_android_2020_2021;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener {

    SharedPreferences preferences;
    EditText editText;
    TextView textView;
    LocationManager locationManager;
    MyTts myTts;
    TextView textView2;
    TextView textView3;
    SQLiteDatabase db;
    private static final int REC_RESULT = 653;
    Button button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editText = findViewById(R.id.editTextTextPersonName);
        myTts = new MyTts(this);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        button3 = findViewById(R.id.button3);
        button3.setEnabled(false);//Aπενεργοποίηση του κουμπιού SAVE EXCESS DATA
        db = openOrCreateDatabase("LocationInfo", Context.MODE_PRIVATE,null);//Δημιουργία ή άνοιγμα βάσης δεδομένων
        db.execSQL("CREATE TABLE IF NOT EXISTS LocInfo(Speed TEXT,Coordinates TEXT,Timestamp INT)");//Δημιουργία πίνακα LocInfo
    }

    public void gps(View view){//Έγκριση εύρεσης τοποθεσίας της συσκευής
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.
                    requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},234);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                this);
        //locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        float speed = location.getSpeed();//Εύρεση ταχύτητας συσκευής
        float x = (float)(speed * 3.6);//Μετατροπή ταχύτητας σε km
        textView.setText(String.valueOf(x) + "km/h");//Εκχώρηση ταχύτητας στο TextView
        double  m = location.getLatitude();//Εύρεση γεωγραφικής συντεταγμένης Χ
        double n = location.getLongitude();//Εύρεση γεωγραφικής συντεταγμένης Υ
        textView2.setText(String.valueOf(m)+","+String.valueOf(n));//Εκχώρηση γεωγραφικών συντεταγμένων στο TextView
        long tsLong = System.currentTimeMillis()/1000;//Εύρεση timestamp
        textView3.setText(String.valueOf(tsLong));//Εκχώρηση timestamp στο TextView
        int y = preferences.getInt("mykey1", Integer.parseInt(editText.getText().toString()));//Eκχώρηση του Share Preference σε μία μεταβλητή y
        if (x > y) {
            setActivityBackgroundColor();//Κόκκιος χρωματισμός background
            speak();//Φωνητικό μήνυμα κινδύνου
            showMessage();//Εμφάνιση μυνήματος κινδύνου
            button3.setEnabled(true);//Ενεργοποίηση του κουμπιού SAVE EXCESS DATA

        }
        else {
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);//Λευκός χρωματισμός background
            button3.setEnabled(false);//Aπενεργοποίηση του κουμπιού SAVE EXCESS DATA
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void save_limit(View view){
        try {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("mykey1", Integer.parseInt(editText.getText().toString()));//Μετά από cast εκχώρηση του κειμένου του EditText στο Share Preference
            editor.apply();
            Toast.makeText(this, "Limit saved!", Toast.LENGTH_LONG).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Not a number!", Toast.LENGTH_LONG).show();//Εμφάνιση μηνύματος λανθασμένης εισόδου
        }
    }

    public void setActivityBackgroundColor() {
        getWindow().getDecorView().setBackgroundColor(Color.RED);
    }

    public void speak(){
        String s = "High speed";
        myTts.speak(s);
    }
    public void showMessage(){
        Toast.makeText(this, "High speed!", Toast.LENGTH_LONG).show();
    }

    public void save_data(View view){
        String speed = textView.getText().toString();
        String coordinates = textView2.getText().toString();
        int timestamp = Integer.parseInt(textView3.getText().toString());
        db.execSQL("INSERT INTO LocInfo VALUES('"+speed+"','"+coordinates+"','"+timestamp+"')");//Εκχώρηση εγγραφής στον πίνακα LocInfo
        Toast.makeText(this,"Data saved!",Toast.LENGTH_LONG).show();
    }

    public void read_data(View view){
            Cursor cursor = db.rawQuery("SELECT * FROM LocInfo",null);//Ερώτημα στην βάση δεδομένων
        if (cursor.getCount()>0){
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()){
                builder.append("Speed:").append(cursor.getString(0)).append("\n");
                builder.append("Coordinates:").append(cursor.getString(1)).append("\n");
                builder.append("Timestamp:").append(cursor.getInt(2)).append("\n");
                builder.append("-----------------------------------\n");
            }
                showMessage("Available Info",builder.toString());//Εμφάνιση ερωτήματος της βάσης δεδομένων
        }
    }

    public void read_data2(View view){
        Cursor cursor = db.rawQuery("SELECT * FROM LocInfo ORDER BY Timestamp DESC LIMIT 3",null);//Ερώτημα στην βάση δεδομένων
        if (cursor.getCount()>0){
            StringBuilder builder = new StringBuilder();
            while (cursor.moveToNext()){
                builder.append("Speed:").append(cursor.getString(0)).append("\n");
                builder.append("Coordinates:").append(cursor.getString(1)).append("\n");
                builder.append("Timestamp:").append(cursor.getInt(2)).append("\n");
                builder.append("-----------------------------------\n");
            }
            showMessage("Available Info",builder.toString());//Εμφάνιση ερωτήματος της βάσης δεδομένων
        }
    }

    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setCancelable(true)
                .setTitle(title)
                .setMessage(message)
                .show();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {//Εκτύπωση της ομιλίας στο EditText
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REC_RESULT && resultCode==RESULT_OK){
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            editText.setText(matches.get(0));
            if (matches.contains("red"))
                getWindow().getDecorView().setBackgroundColor(Color.RED);
            if (matches.contains("blue"))
                getWindow().getDecorView().setBackgroundColor(Color.BLUE);
            if (matches.contains("yellow"))
                getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
        }

    }

    public void recognize(View view){ //Ενεργοποίηση του κουμπιού SET SPEED LIMIT και της λειτουργίας αναγνώρισης ομιλίας
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Please set limit!");
        startActivityForResult(intent,REC_RESULT);
    }
}