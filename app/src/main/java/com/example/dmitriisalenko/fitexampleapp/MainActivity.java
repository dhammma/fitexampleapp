package com.example.dmitriisalenko.fitexampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends Activity {
    // don't know why should we have this constant
    private static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 11235813;
    private boolean processingConnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        render();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                TextView statusTextView = findViewById(R.id.statusTextView);
                statusTextView.setText("Google Fit is connected");
                log("Connected ok");
            } else {
                log("Connected is not ok");
            }
            processingConnect = false;
        }

        render();
    }

    FitnessOptions getFitnessOptions() {
        return FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA)
                .build();
    }

    boolean hasPermissions() {
        return GoogleSignIn.hasPermissions(
                GoogleSignIn.getLastSignedInAccount(this),
                getFitnessOptions());
    }

    public void connectGoogleFit(View button) {
        if (processingConnect || hasPermissions()) {
            return;
        }
        processingConnect = true;
        GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                GoogleSignIn.getLastSignedInAccount(this),
                getFitnessOptions());
    }

    public void disconnectFit(View button) {
        if (processingConnect || !hasPermissions()) {
            return;
        }
        processingConnect = true;
        Task task = Fitness.getConfigClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .disableFit();


//        GoogleSignInOptions signInOptions = (new GoogleSignInOptions.Builder()).addExtension(getFitnessOptions()).build();
//        GoogleSignInClient client = GoogleSignIn.getClient(getApplicationContext(), signInOptions);
//        client.revokeAccess();

        task.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                processingConnect = false;
                GoogleSignInOptions signInOptions = (new GoogleSignInOptions.Builder()).addExtension(getFitnessOptions()).build();
                GoogleSignInClient client = GoogleSignIn.getClient(getApplicationContext(), signInOptions);
                client.revokeAccess();
                render();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                log("Disconnect failure " + e.getMessage());
            }
        });

        task.addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                log("Disconnect ok");
            }
        });
    }

    public void log(String message) {
        Log.v("DMITRII", "BLA BLA " + message);
    }

    public void render() {
        TextView statusText = findViewById(R.id.statusTextView);

        if (hasPermissions()) {
            statusText.setText("Google Fit is connected");
        } else {
            statusText.setText("Google Fit is not connected");
        }
    }
}
