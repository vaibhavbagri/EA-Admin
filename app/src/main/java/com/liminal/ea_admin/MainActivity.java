package com.liminal.ea_admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    private int RC_SIGN_IN = 0;
    private GoogleSignInAccount account;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView loaderView = findViewById(R.id.loadingGifView);
        Glide.with(this).asGif().load(R.drawable.loading_cube).into(loaderView);

        new Handler().postDelayed(this::authenticateUser, 2180);
    }

    // Function to authenticate user using Google sign-in
    private void authenticateUser()
    {
        // Check if user is already signed in
        account = GoogleSignIn.getLastSignedInAccount(this);

        if(account != null)
        {
            Log.d("EAG_MAIN_ACTIVTY","User logged in with account : " + account.getEmail());
            updateUserProfile();
        }
        else
        {
            // Configure Google sign-in
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }
    }

    // Function to update User details in firebase
    private void updateUserProfile()
    {
        Log.d("EAG_USER_DETAILS", "email : " + account.getEmail() + " userName : " + account.getDisplayName() + " id : " + account.getId());
        UserProfile userProfile = new UserProfile(account.getEmail(), account.getGivenName(), account.getFamilyName(), account.getPhotoUrl(), account.getId());
        addUserInFirebase(userProfile);
        setSharedPreferences(userProfile);
        startActivity(new Intent(this, RewardsActivity.class));
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                account = task.getResult(ApiException.class);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("userProfileTable");
                ValueEventListener idListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(Objects.requireNonNull(account.getId())))
                        {
                            Log.d("EAG_GOOGLE_AUTH", "Signed in with account : " + account.getEmail());
                            updateUserProfile();
                        }
                        else
                        {
                            Log.d("EAG_GOOGLE_AUTH", "New user with signed in with account : " + account.getEmail());
                            updateUserProfile();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d("EAG_FIREBASE_DB", "Failed to read data from Firebase : ", databaseError.toException());
                    }
                };
                databaseReference.addListenerForSingleValueEvent(idListener);

            }
            catch (ApiException e) {
                if(e.getStatusCode() == 12501)
                    Toast.makeText(MainActivity.this, "Please sign-in to continue", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "Sign-in failed, please try again", Toast.LENGTH_SHORT).show();
                authenticateUser();
                Log.d("EAG_GOOGLE_AUTH", "SignInResult : failed code = " + e.getStatusCode());
            }
        }
    }

    private void addUserInFirebase(UserProfile userProfile)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("adminTable");
        databaseReference.child(userProfile.ID).child("personalDetails").setValue(userProfile);
    }

    private void setSharedPreferences(UserProfile userProfile)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("User_Details", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", userProfile.ID);
        editor.putString("first_name", userProfile.firstName);
        editor.putString("last_name", userProfile.lastName);
        editor.putString("photo_url", userProfile.photoURL);
        editor.apply();
    }

}
