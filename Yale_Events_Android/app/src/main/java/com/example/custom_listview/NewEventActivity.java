package com.example.custom_listview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NewEventActivity extends AppCompatActivity {

    TextInputEditText eventName;
    TextInputEditText eventDate;
    TextInputEditText eventTime;
    Button btnLogin;
    Button addImageButton;
    DatabaseReference myRef;
    FirebaseDatabase database;
    DatePickerDialog datePicker;
    TimePickerDialog timePicker;
    FirebaseStorage storage;
    StorageReference storageRef;
    UploadTask uploadTask;
    InputStream iStream;

    FirebaseAuth mAuth;
    SimpleDateFormat df;

    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            df = new SimpleDateFormat("EE, MMM dd, YYYY", Locale.getDefault());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        eventName = findViewById(R.id.eventName);

        eventDate = findViewById(R.id.eventDate);
        eventTime = findViewById(R.id.eventTime);
        btnLogin = findViewById(R.id.createEventButton);
        addImageButton = findViewById(R.id.addImageButton);

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Events");
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("images");

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(view -> {
            checkIfEmailVerified();
        });

        addImageButton.setOnClickListener(view -> {
            imageChooser();
        });

        eventDate.setOnClickListener(view -> {
            eventName.clearFocus();
            InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            final Calendar cldr = Calendar.getInstance();
            int day = cldr.get(Calendar.DAY_OF_MONTH);
            int month = cldr.get(Calendar.MONTH);
            int year = cldr.get(Calendar.YEAR);
            // date picker dialog
            datePicker = new DatePickerDialog(NewEventActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(year,monthOfYear,dayOfMonth);
                            Date myDate = calendar.getTime();
                            String finalDate = df.format(myDate);
                            eventDate.setText(finalDate);
                        }
                    }, year, month, day);
            datePicker.show();


        });

        eventTime.setOnClickListener(view -> {
            eventName.clearFocus();
            final Calendar cldr = Calendar.getInstance();
            int hour = cldr.get(Calendar.HOUR_OF_DAY);
            int minutes = cldr.get(Calendar.MINUTE);
            // time picker dialog
            timePicker = new TimePickerDialog(NewEventActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(0, 0, 0, sHour, sMinute);
                            Date myTime = calendar.getTime();
                            SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
                            eventTime.setText(formatter.format(myTime));
                        }
                    }, hour, minutes, false);
            timePicker.show();
        });

    }

    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    try {
                        iStream = getContentResolver().openInputStream(selectedImageUri);
                        System.out.println("iStream SET");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

//    public byte[] getBytes(InputStream inputStream) throws IOException {
//        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
//        int bufferSize = 1024;
//        byte[] buffer = new byte[bufferSize];
//
//        int len = 0;
//        while ((len = inputStream.read(buffer)) != -1) {
//            byteBuffer.write(buffer, 0, len);
//        }
//        return byteBuffer.toByteArray();
//    }

    private void checkIfEmailVerified()
    {
        FirebaseUser user = mAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified())
            {
                // user is verified, so you can finish this activity or send user to activity which you want.
                String eventNameText = eventName.getText().toString();
                String eventDateText = eventDate.getText().toString();
                String eventTimeText = eventTime.getText().toString();

                if (TextUtils.isEmpty(eventNameText)){
                    eventName.setError("Event name be empty");
                    eventName.requestFocus();
                }
                else if (TextUtils.isEmpty(eventDateText)){
                    eventDate.setError("Event date cannot be empty");
                    eventDate.requestFocus();
                }
                else if (TextUtils.isEmpty(eventTimeText)){
                    eventTime.setError("Event time cannot be empty");
                    eventTime.requestFocus();
                }
                else {
                    myRef.child(eventNameText).child("Date").setValue(eventDateText);
                    myRef.child(eventNameText).child("Time").setValue(eventTimeText);
                    startActivity(new Intent(NewEventActivity.this, MainActivity.class));
                }

                if (iStream != null) {
                    uploadTask = storageRef.child(eventNameText.replace(" ", "_")).putStream(iStream);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            System.out.println("Upload succeeded");
                            storageRef.child(eventNameText.replace(" ", "_")).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    myRef.child(eventNameText).child("Image").setValue(uri.toString());
//                                    System.out.println("URL: " + uri.toString());
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });
                        }
                    });
                }
                else {
                    System.out.println("ISTREAM NULL");
                }

            }
            else
            {
                // email is not verified, so just prompt the message to the user and restart this activity.
                // NOTE: don't forget to log out the user.
                Log.d("Firebase", "Not verified");
                startActivity(new Intent(NewEventActivity.this, NotVerifiedActivity.class));
                FirebaseAuth.getInstance().signOut();
                //restart this activity

            }

        }

        else {
            startActivity(new Intent(NewEventActivity.this, LoginActivity.class));
        }

    }

}