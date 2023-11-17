package com.example.custom_listview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText etRegEmail;
    TextInputEditText etRegPassword;
    TextInputEditText userNameInput;
    TextView tvLoginHere;
    Button btnRegister;

    FirebaseAuth mAuth;
    DatabaseReference myRef;
    DatabaseReference authUsers;
    FirebaseDatabase database;

    List<String> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPass);
        userNameInput = findViewById(R.id.userNameInput);
        tvLoginHere = findViewById(R.id.tvLoginHere);
        btnRegister = findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();
        userList = new ArrayList<String>();

        btnRegister.setOnClickListener(view ->{
            createUser();
        });

        database = FirebaseDatabase.getInstance();
        authUsers = database.getReference("Authorized_Users");
        myRef = database.getReference("Users");

        tvLoginHere.setOnClickListener(view ->{
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });

        // Read from the database
        authUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
                ArrayList<String> users = (ArrayList<String>) dataSnapshot.getValue();
                for (int i = 0; i < users.size(); i++) {
                    userList.add(users.get(i));
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("FB", "Failed to read value.", error.toException());
            }
        });
    }

    private void createUser(){
        String email = etRegEmail.getText().toString();
        String password = etRegPassword.getText().toString();
        String userName = userNameInput.getText().toString();

        if (TextUtils.isEmpty(email)){
            etRegEmail.setError("Email cannot be empty");
            etRegEmail.requestFocus();
        }
        else if (!userList.contains(email)) {
            etRegEmail.setError("You've not been invited");
            etRegEmail.requestFocus();
        }
        else if (TextUtils.isEmpty(password)){
            etRegPassword.setError("Password cannot be empty");
            etRegPassword.requestFocus();
        }
        else{
//            ActionCodeSettings actionCodeSettings =
//                    ActionCodeSettings.newBuilder()
//                            // URL you want to redirect back to. The domain (www.example.com) for this
//                            // URL must be whitelisted in the Firebase Console.
//                            // This must be true
//                            .setUrl("https://www.transcribeglass.com")
//                            .setHandleCodeInApp(true)
//                            .setAndroidPackageName(
//                                    "com.example.custom_listview",
//                                    true, /* installIfNotAvailable */
//                                    "12"    /* minimumVersion */)
//                            .setDynamicLinkDomain("yaleevents.page.link")
//                            .build();
//
//            mAuth.sendSignInLinkToEmail(email, actionCodeSettings)
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if (task.isSuccessful()) {
//                                Log.d("Firebase", "Email sent.");
//                            }
//                        }
//                    });
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName)
                                .build();
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("Firebase", "User profile updated.");
                                        }
                                    }
                                });
                        myRef.child(user.getUid()).child("Email").setValue(user.getEmail());
                        myRef.child(user.getUid()).child("Registered").setValue(Calendar.getInstance().getTime().toString());
                        startActivity(new Intent(RegisterActivity.this, RegisterSuccessful.class));
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}