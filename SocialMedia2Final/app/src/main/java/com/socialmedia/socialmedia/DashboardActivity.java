package com.socialmedia.socialmedia;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import android.content.Intent;


import androidx.annotation.NonNull;
import androidx.appcompat.app.*;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    public DashboardActivity() {
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            String isBusinessOwner = "" + dataSnapshot.child("isBusinessOwner").getValue();
                            if (isBusinessOwner.equals("1")) {
                                //user is logged in, so start LoginActivity
                                startActivity(new Intent(DashboardActivity.this, BusinessOwnerDashboardActivity.class));
                                finish();
                            } else if (isBusinessOwner.equals("2")) {
                                //user is logged in, so start LoginActivity
                                startActivity(new Intent(DashboardActivity.this, IndividualDashboardActivity.class));
                                finish();
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }


    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatus();
        super.onStart();
    }


    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {

        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }
}