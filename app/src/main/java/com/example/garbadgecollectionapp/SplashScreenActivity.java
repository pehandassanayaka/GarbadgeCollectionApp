package com.example.garbadgecollectionapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.example.garbadgecollectionapp.Model.DriverInfoModel;
import com.example.garbadgecollectionapp.databinding.ActivitySplashScreenBinding;
import com.example.garbadgecollectionapp.databinding.LayoutRegisterBinding;
import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class SplashScreenActivity extends AppCompatActivity {

    private final static int LOGIN_REQUEST_CODE = 7171; // Any number
    private List<AuthUI.IdpConfig> providers;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;

    private ActivitySplashScreenBinding binding;

    FirebaseDatabase database;
    DatabaseReference driverInfoRef;

    @Override
    protected void onStart() {
        super.onStart();
        delaySplashScreen();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null && listener != null)
            firebaseAuth.removeAuthStateListener(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize registerBinding here
        //LayoutRegisterBinding registerBinding = LayoutRegisterBinding.inflate(LayoutInflater.from(this));

        init();
    }

    private void init() {
        database = FirebaseDatabase.getInstance();
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE);

        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        firebaseAuth = FirebaseAuth.getInstance();
        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser();
            if (user != null) {
                checkUserFromFirebase();
            } else {
                showLoginLayout();
            }
        };
    }

    private void checkUserFromFirebase() {
        driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(SplashScreenActivity.this, "User already registered", Toast.LENGTH_SHORT).show();
                } else {
                    showRegisterLayout();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SplashScreenActivity.this, "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void showRegisterLayout() {
//        LayoutRegisterBinding registerBinding = LayoutRegisterBinding.inflate(LayoutInflater.from(this));
//        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
//        builder.setView(registerBinding.getRoot());
//        AlertDialog dialog = builder.create();
//        dialog.show();
//
//        registerBinding.btnRegister.setOnClickListener(view -> {
//            String firstName = registerBinding.edtFirstName.getText().toString();
//            String lastName = registerBinding.edtLastName.getText().toString();
//            String phoneNumber = registerBinding.edtPhoneNumber.getText().toString();
//
//            if (TextUtils.isEmpty(firstName)) {
//                Toast.makeText(this, "Please enter first name", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (TextUtils.isEmpty(lastName)) {
//                Toast.makeText(this, "Please enter last name", Toast.LENGTH_SHORT).show();
//                return;
//            }
//            if (TextUtils.isEmpty(phoneNumber)) {
//                Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            DriverInfoModel model = new DriverInfoModel();
//            model.setFirstName(firstName);
//            model.setLastName(lastName);
//            model.setPhoneNumber(phoneNumber);
//            model.setRating(0.0);
//
//            driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                    .setValue(model)
//                    .addOnFailureListener(e -> {
//                        dialog.dismiss();
//                        Toast.makeText(SplashScreenActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                    })
//                    .addOnSuccessListener(avoid -> {
//                        Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
//                        dialog.dismiss();
//                    });
//        });
//    }

    private void showRegisterLayout() {
        // Inflate the layout using LayoutRegisterBinding
        LayoutRegisterBinding registerBinding = LayoutRegisterBinding.inflate(LayoutInflater.from(this));

        // Create an AlertDialog.Builder to show the registration layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setView(registerBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();

        // Set onClickListener for the registration button inside the layout
        registerBinding.btnRegister.setOnClickListener(view -> {
            try {
                String firstName = registerBinding.edtFirstName.getText().toString();
                String lastName = registerBinding.edtLastName.getText().toString();
                String phoneNumber = registerBinding.edtPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(firstName)) {
                    Toast.makeText(this, "Please enter first name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(lastName)) {
                    Toast.makeText(this, "Please enter last name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a DriverInfoModel object with entered details
                DriverInfoModel model = new DriverInfoModel();
                model.setFirstName(firstName);
                model.setLastName(lastName);
                model.setPhoneNumber(phoneNumber);
                model.setRating(0.0);

                // Store the driver information in Firebase Realtime Database
                driverInfoRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(model)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            // Start NextActivity after successful registration
                            startActivity(new Intent(SplashScreenActivity.this, NextActivity.class));
                            finish(); // Finish the current activity to prevent going back to registration screen
                        })
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(SplashScreenActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } catch (Exception e) {
                // Catch any exceptions that occur during registration process
                e.printStackTrace();
                Toast.makeText(SplashScreenActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void showLoginLayout() {
        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setPhoneButtonId(R.id.btn_phone_sign_in)
                .setGoogleButtonId(R.id.btn_google_sign_in)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.LoginTheme)
                .setAvailableProviders(providers)
                .build(), LOGIN_REQUEST_CODE);
    }

    @SuppressLint("CheckResult")
    private void delaySplashScreen() {
        Completable.timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(() ->
                        firebaseAuth.addAuthStateListener(listener)
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            } else {
                Toast.makeText(this, "[ERROR]" + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
