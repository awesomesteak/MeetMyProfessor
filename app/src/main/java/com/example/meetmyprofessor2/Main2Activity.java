package com.example.meetmyprofessor2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class Main2Activity extends AppCompatActivity {

    private static final String TAG = "Main2Activity";
    public static final String GOOGLE_ACCOUNT = "google_account";
    private TextView profileName, profileEmail;
    private Button signOut, btnAvailable, btnGone, btnOnMyWay, btnBusy, btnInClass, btnRunningLate;
    String email, currentStatus, userID;

    // API Endpoint was hosted locally, which explains the variables below
    String baseUrl = "10.157.225.111";
    String urlGetProf = "http://" + baseUrl + ":5000/getProfessors";
    String urlUpdateStatus = "http://" + baseUrl + ":5000/updateStatus";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        profileName = findViewById(R.id.profile_text);
        profileEmail = findViewById(R.id.profile_email);
        signOut = findViewById(R.id.sign_out);

        btnAvailable = findViewById(R.id.btnAvailable);
        btnGone = findViewById(R.id.btnGone);
        btnOnMyWay = findViewById(R.id.btnOnMayWay);
        btnBusy = findViewById(R.id.btnBusy);
        btnInClass = findViewById(R.id.btnInClass);
        btnRunningLate = findViewById(R.id.btnRunningLate);

        // = Start of Button listener setup =

        signOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                MainActivity.getInstance().signOut();
                Intent intent = new Intent(Main2Activity.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        btnAvailable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "setDataOnView: " + currentStatus + " " + userID);
                try {
                    postRequest("Available",userID);
                }catch(IOException e){
                    Toast.makeText(getApplicationContext(), "Change status failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    postRequest("Gone for the Day",userID);
                }catch(IOException e){
                    Toast.makeText(getApplicationContext(), "Change status failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnOnMyWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    postRequest("On My Way",userID);
                }catch(IOException e){
                    Toast.makeText(getApplicationContext(), "Change status failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnBusy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    postRequest("Busy",userID);
                }catch(IOException e){
                    Toast.makeText(getApplicationContext(), "Change status failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnInClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    postRequest("In Class",userID);
                }catch(IOException e){
                    Toast.makeText(getApplicationContext(), "Change status failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnRunningLate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    postRequest("Running Late",userID);
                }catch(IOException e){
                    Toast.makeText(getApplicationContext(), "Change status failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // = End of button listener setup =

        setDataOnView();

    }

    private void setDataOnView() {
        // We get information of their Google Account from  MainActivity
        GoogleSignInAccount googleSignInAccount = getIntent().getParcelableExtra(GOOGLE_ACCOUNT);
        String greetings = "Welcome back,";
        profileName.setText(greetings + "\n" + googleSignInAccount.getDisplayName());
        email = googleSignInAccount.getEmail();

        try{
            getRequest();
        }catch(IOException e){
            Log.w(TAG, "onCreate: " + e);
        }

    }

    // This method performs a HTTP GET request for the information that we need
    private void getRequest() throws IOException{
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(urlGetProf)
                .header("Accept","application/json")
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w("Failure: ",mMessage );
                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        profileEmail.setText("Current Status: " + "Unknown");
                        Toast.makeText(Main2Activity.this, "Unable to retrieve status", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String mMessage = response.body().string();
                Log.e(TAG, mMessage);
                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initStatus(mMessage);
                    }
                });

            }
        });
    }

    // This method parses a JSON String, looks for the status of the current user, and updates the status on the UI
    private void initStatus(String jsonString) {
        try {
            // The API actually returns a JSON Array, as opposed to a JSON Object
            JSONArray reader = new JSONArray(jsonString);

            // We iterate thru the entire array to find a match with the email
            for (int i = 0; i < reader.length(); i++) {
                try{
                    JSONObject obj = reader.getJSONObject(i);
                    String str = obj.getString("email");

                    Log.e(TAG, "initStatus: email " + email + " mail: " + str);
                    if (str.equals(email)) {
                        this.currentStatus = obj.getString("userStatus");
                        this.userID = obj.getString("userId");
                        profileEmail.setText("Current Status: " + currentStatus);
                        Log.e(TAG, "onCreate: " + currentStatus + " " + userID);
                        break;
                    }
                }catch (Exception e){
                    Log.e(TAG, "initStatus: unknown exception: ",e);
                }

            }

        } catch (JSONException e) {
            Log.e(TAG, "initStatus: unexpected JSON exception", e);
            profileEmail.setText("Current Status: Unable to retrieve");
        }

    }

    // This method performs a POST HTTP request to update information on the database
    private void postRequest(final String status, String userId) throws IOException{
        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        OkHttpClient client = new OkHttpClient();

        // The POST request requires the userId (which we obtained from the GET request) and the status that we want to update to
        JSONObject postData = new JSONObject();
        try {
            postData.put("userId", userId);
            postData.put("userStatus", status);
        }catch(JSONException e){
            Log.e(TAG, "postRequest: unexpected JSON exception",e);
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE, postData.toString());

        Request request = new Request.Builder()
                .url(urlUpdateStatus)
                .post(body)
                .header("Accept","application/json")
                .header("Content-Type","application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                String mMessage = e.getMessage().toString();
                Log.w(TAG, "onFailure: " + mMessage);
                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(Main2Activity.this, "Unable to update status. Check internet connection", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String mMessage = response.body().string();
                Log.e(TAG, "onResponse: " + mMessage);
                Main2Activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateStatus(status);
                        Toast.makeText(Main2Activity.this, "Status update successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void updateStatus(String status){
        profileEmail.setText("Current Status: " + status);
    }

}
