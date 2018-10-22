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
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    // don't know why should we have this constant
    private static int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 11235813;
    private boolean processingConnect = false;
    private List<DataSet> weeklyDataSets;
    private DataReadResponse weeklyResponse;
    private DataSet dailyDataSet;


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
                subscribeToFitnessData();
                readStepsData();
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
        TextView stepsText = findViewById(R.id.stepsTextView);
        TextView noDailyStepsText = findViewById(R.id.noDailySteps);
        TextView dailyStepsText = findViewById(R.id.dailyStepsTextView);
        Button readDataButton = findViewById(R.id.readData);
        Button connectButton = findViewById(R.id.connectButton);
        Button disconnectButton = findViewById(R.id.disconnectButton);
        View dailyStepsView = findViewById(R.id.dailySteps);

        statusText.setVisibility(View.VISIBLE);
        if (hasPermissions()) {
            readDataButton.setVisibility(View.VISIBLE);
            dailyStepsView.setVisibility(View.VISIBLE);
            connectButton.setVisibility(View.GONE);
            disconnectButton.setVisibility(View.VISIBLE);
            statusText.setText(R.string.google_fit_connected);

            if (weeklyResponse != null) {
                DateFormat dateFormat = DateFormat.getTimeInstance();
                String results = "";
                for (Bucket b : weeklyResponse.getBuckets()) {
                    for (DataSet ds : b.getDataSets()) {
                        for (DataPoint dp: ds.getDataPoints()) {
                        String type = dp.getDataType().getName();
                        String start = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
                        String end = dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS));
                        String steps = dp.getValue(Field.FIELD_STEPS).toString();

                        results += start + " " + end + " " + type + "\n" + steps + "\n";
                        }
                    }
                }
                stepsText.setText(results);
                log(results);
            }

//           x if (weeklyDataSets != null) {
//                log("weekly datasets " + weeklyDataSets.toString());
//            }
//
//            if (weeklyDataSets != null && weeklyDataSets.size() > 0) {
//                stepsText.setVisibility(View.VISIBLE);
//                DateFormat dateFormat = DateFormat.getTimeInstance();
//                String results = "";
//                for (DataSet ds : weeklyDataSets) {
//                    log("ds " + ds.toString());
//                    if (ds.getDataPoints().size() == 0) {
//                        continue;
//                    }
//                    for (DataPoint dp : ds.getDataPoints()) {
//                        String type = dp.getDataType().getName();
//                        String start = dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS));
//                        String end = dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS));
//                        String steps = dp.getValue(Field.FIELD_STEPS).toString();
//
//                        results += start + " " + end + " " + type + "\n" + steps + "\n";
//                    }
//
//                    results += "\nData set ends\n\n";
//                }
//                log("read results " + results);
//                stepsText.setText(results);
//            } else {
//                log("no data");
//                stepsText.setVisibility(View.GONE);
//            }

            if (dailyDataSet != null && dailyDataSet.getDataPoints().size() > 0) {
                noDailyStepsText.setVisibility(View.GONE);
                dailyStepsText.setVisibility(View.VISIBLE);
                dailyStepsText.setText(dailyDataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).toString());
            } else {
                noDailyStepsText.setVisibility(View.VISIBLE);
                dailyStepsText.setVisibility(View.GONE);
            }
        } else {
            readDataButton.setVisibility(View.GONE);
            dailyStepsView.setVisibility(View.GONE);
            connectButton.setVisibility(View.VISIBLE);
            stepsText.setVisibility(View.GONE);
            disconnectButton.setVisibility(View.GONE);
            statusText.setText(R.string.google_fit_disconnected);
        }
    }

    //
    // BLOCK START: subscriptions to fitness data
    //
    public void subscribeToFitnessData() {
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        log("Subscribe to fitness data ok");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        log("Subscribe to fitness data failure " + e.getMessage());
                    }
                });
    }

    // BLOCK END

    //
    // BLOCK START: read steps data
    //

    public void readStepsData() {
        readWeeklyStepsData();
        readDailyStepsData();
    }

    public void readWeeklyStepsData() {
        if (!hasPermissions()) {
            return; // safe method
        }

        // Setting a start and end date using a range of 10 days before this moment.
        Calendar calendar = Calendar.getInstance();
        Date now = new Date();
        calendar.setTime(now);
        long endTime = calendar.getTimeInMillis();
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = calendar.getTimeInMillis();

        DataReadRequest request = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Task<DataReadResponse> response = Fitness
                .getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(request)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        log("read steps failure " + e.getMessage());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        log("read steps success");
                        weeklyResponse = dataReadResponse;
//                        log(dataReadResponse.toString());
//                        log(dataReadResponse.getBuckets().toString());
//                        weeklyDataSets = dataReadResponse.getDataSets();
                        render();
                    }
                });
    }

    public void readDailyStepsData() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotalFromLocalDevice(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        log("Read daily steps ok");
                        log(dataSet.toString());
                        dailyDataSet = dataSet;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        log("Read daily steps failure " + e.getMessage());
                    }
                });
    }

    public void onReadClick(View v) {
        readStepsData();
    }
}
