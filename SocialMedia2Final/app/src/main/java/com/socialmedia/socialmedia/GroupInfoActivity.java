package com.socialmedia.socialmedia;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Query;
import com.google.firebase.storage.StorageReference;
import com.socialmedia.socialmedia.adapters.AdapterParticipantAdd;
import com.socialmedia.socialmedia.models.ModelUser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private static final String TAG = "GROUP_INFO_TAG";

    private String groupId;
    private String myGroupRole = "";

    private FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    private ActionBar actionBar;

    //ui views
    private ImageView groupIconIv;
    private TextView descriptionTv, createdByTv, editGroupTv, addParticipantTv, leaveGroupTv, participantsTv;
    private RecyclerView participantsRv;

    private ArrayList<ModelUser> userList;
    private AdapterParticipantAdd adapterParticipantAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        groupIconIv = findViewById(R.id.groupIconIv);
        descriptionTv = findViewById(R.id.descriptionTv);
        createdByTv = findViewById(R.id.createdByTv);
        editGroupTv = findViewById(R.id.editGroupTv);
        addParticipantTv = findViewById(R.id.addParticipantTv);
        leaveGroupTv = findViewById(R.id.leaveGroupTv);
        participantsTv = findViewById(R.id.participantsTv);
        participantsRv = findViewById(R.id.participantsRv);

        groupId = getIntent().getStringExtra("groupId");

        firebaseAuth = FirebaseAuth.getInstance();
        loadGroupInfo();
        loadMyGroupRole();

        addParticipantTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupParticipantAddActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        editGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GroupInfoActivity.this, GroupEditActivity.class);
                intent.putExtra("groupId", groupId);
                startActivity(intent);
            }
        });

        leaveGroupTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if user is participant/admin: leave group
                //if user is creator: delete group
                String dialogTitle="";
                String dialogDescription="";
                String positiveButtonTitle="";
                if (myGroupRole.equals("creator")){
                    dialogTitle= "حذف المجموعة";
                    dialogDescription = "هل أنت متأكد أنك تريد حذف المجموعة نهائيًا؟";
                    positiveButtonTitle = "حذف";
                }
                else {
                    dialogTitle= "مغادرة المجموعة";
                    dialogDescription = "هل أنت متأكد أنك تريد مغادرة المجموعة نهائيًا؟";
                    positiveButtonTitle = "مغادرة";
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                        .setMessage(dialogDescription)
                        .setPositiveButton(positiveButtonTitle, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (myGroupRole.equals("creator")){
                                    //im creator of group: delete group
                                    deleteGroup();
                                }
                                else {
                                    //im participant/admin: leave group
                                    leaveGroup();
                                }
                            }
                        })
                        .setNegativeButton("إلغاء", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });

    }

    private void leaveGroup() {
        firebaseAuth = FirebaseAuth.getInstance();

        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //firebase storage reference

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String isBusinessOwner  = "" + ds.child("isBusinessOwner").getValue();
                    if(isBusinessOwner.equals("1")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //group left successfully...
                                        Toast.makeText(GroupInfoActivity.this, "غادرت المجموعة بنجاح ...", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(GroupInfoActivity.this, BusinessOwnerDashboardActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to leave group
                                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }else if (isBusinessOwner.equals("2")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //group left successfully...
                                        Toast.makeText(GroupInfoActivity.this, "غادرت المجموعة بنجاح ...", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(GroupInfoActivity.this, IndividualDashboardActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to leave group
                                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void deleteGroup() {
        firebaseAuth = FirebaseAuth.getInstance();

        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //firebase storage reference

        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String isBusinessOwner  = "" + ds.child("isBusinessOwner").getValue();
                    if(isBusinessOwner.equals("1")) {
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                        ref.child(groupId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //group deleted successfully...
                                        Toast.makeText(GroupInfoActivity.this, "تم حذف المجموعة بنجاح ...", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(GroupInfoActivity.this, BusinessOwnerDashboardActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to delete group
                                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }else if (isBusinessOwner.equals("2")){
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
                        ref.child(groupId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //group deleted successfully...
                                        Toast.makeText(GroupInfoActivity.this, "تم حذف المجموعة بنجاح ...", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(GroupInfoActivity.this, IndividualDashboardActivity.class));
                                        finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //failed to delete group
                                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadGroupInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //get group info
                    String groupId = ""+ds.child("groupId").getValue();
                    String groupTitle = ""+ds.child("groupTitle").getValue();
                    String groupDescription = ""+ds.child("groupDescription").getValue();
                    String groupIcon = ""+ds.child("groupIcon").getValue();
                    String createdBy = ""+ds.child("createdBy").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();

                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                    cal.setTimeInMillis(Long.parseLong(timestamp));
                    String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();

                    loadCreatorInfo(dateTime, createdBy);

                    //set group info
                    actionBar.setTitle(groupTitle);
                    descriptionTv.setText(groupDescription);

                    try {
                        Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_white).into(groupIconIv);
                    } catch (Exception e) {
                        groupIconIv.setImageResource(R.drawable.ic_group_white);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadCreatorInfo(final String dateTime, String createBy) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String name = ""+ds.child("name").getValue();
                    createdByTv.setText("تم الإنشاء بواسطة "+name +" في "+dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadMyGroupRole() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            myGroupRole = ""+ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail() +" ("+myGroupRole+")");

                            if (myGroupRole.equals("participant")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.GONE);
                                leaveGroupTv.setText("غادر المجموعة");
                            }
                            else if (myGroupRole.equals("admin")){
                                editGroupTv.setVisibility(View.GONE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("غادر المجموعة");
                            } else  if (myGroupRole.equals("creator")){
                                editGroupTv.setVisibility(View.VISIBLE);
                                addParticipantTv.setVisibility(View.VISIBLE);
                                leaveGroupTv.setText("حذف المجموعة");
                            }
                        }

                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void loadParticipants() {
        userList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //get uid from Group > Participants
                    String uid = ""+ds.child("uid").getValue();

                    //get info of user using uid we got above
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(uid).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds: dataSnapshot.getChildren()){
                                try {
                                    ModelUser modelUser = ds.getValue(ModelUser.class);

                                    userList.add(modelUser);
                                }
                                catch (Exception e){
                                    Log.e(TAG, "onDataChange: ", e);
                                }
                            }
                            //adapter
                            adapterParticipantAdd = new AdapterParticipantAdd(GroupInfoActivity.this, userList, groupId, myGroupRole);
                            //set adapter
                            participantsRv.setAdapter(adapterParticipantAdd);
                            participantsTv.setText("الاعضاء");
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
