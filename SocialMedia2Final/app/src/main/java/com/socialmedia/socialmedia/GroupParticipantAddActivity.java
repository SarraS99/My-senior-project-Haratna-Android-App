package com.socialmedia.socialmedia;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.socialmedia.socialmedia.adapters.AdapterParticipantAdd;
import com.socialmedia.socialmedia.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GroupParticipantAddActivity extends AppCompatActivity {
    private static final String TAG = "GROUP_ADD_TAG";

    private RecyclerView usersRv;

    private ActionBar actionBar;

    private FirebaseAuth firebaseAuth;
    private String myUid = "";
    private String myNeighbourhood = "";

    private String groupId;
    private String myGroupRole;

    private ArrayList<ModelUser> userList;
    private AdapterParticipantAdd adapterParticipantAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_participant_add);

        actionBar = getSupportActionBar();
        actionBar.setTitle("إضافة مشاركون");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();

        //init views
        usersRv = findViewById(R.id.usersRv);

        groupId = getIntent().getStringExtra("groupId");
        loadMyInfo();

    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myNeighbourhood = "" + snapshot.child("UserNeighborhood").getValue();

                        Log.d(TAG, "onDataChange: myNeighbourhood: " + myNeighbourhood);

                        loadGroupInfo();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getAllUsers() {
        //init list
        userList = new ArrayList<>();
        //load users from db
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    try {
                        ModelUser modelUser = ds.getValue(ModelUser.class);

                        String userNeighborhood = ""+ds.child("UserNeighborhood").getValue();
                        modelUser.setUserNeighborhood(userNeighborhood);
                        Log.d(TAG, "onDataChange: userNeighborhood: "+userNeighborhood);

                        //get all users accept currently signed in
                        if (!modelUser.getUid().equals(firebaseAuth.getUid()) && userNeighborhood.equals(myNeighbourhood)) {
                            //not my uid
                            userList.add(modelUser);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: ", e);
                    }
                }
                //setup adapter
                adapterParticipantAdd = new AdapterParticipantAdd(GroupParticipantAddActivity.this, userList, "" + groupId, "" + myGroupRole);
                //set adapter to recyclerview
                usersRv.setAdapter(adapterParticipantAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadGroupInfo() {
        final DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("Groups");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String groupId = "" + ds.child("groupId").getValue();
                            final String groupTitle = "" + ds.child("groupTitle").getValue();
                            String groupDescription = "" + ds.child("groupDescription").getValue();
                            String groupIcon = "" + ds.child("groupIcon").getValue();
                            String createdBy = "" + ds.child("createdBy").getValue();
                            String timestamp = "" + ds.child("timestamp").getValue();
                            actionBar.setSubtitle("Add Participants");

                            ref1.child(groupId).child("Participants").child(firebaseAuth.getUid())
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                myGroupRole = "" + dataSnapshot.child("role").getValue();
                                                actionBar.setTitle(groupTitle + "(" + myGroupRole + ")");

                                                getAllUsers();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
