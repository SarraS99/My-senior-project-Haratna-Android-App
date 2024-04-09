package com.socialmedia.socialmedia.fragments;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.socialmedia.socialmedia.BusinessOwnerDashboardActivity;
import com.socialmedia.socialmedia.GroupChatsFragment;
import com.socialmedia.socialmedia.IndividualDashboardActivity;
import com.socialmedia.socialmedia.MainActivity;
import com.socialmedia.socialmedia.R;
import com.socialmedia.socialmedia.SettingsActivity;
import com.squareup.picasso.Picasso;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoreFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    TextView profile, savePost, logOut, UserName;
    // new
    TextView chatsTv, groupChatsTv, notifiSettingsTv, usersTv;
    ImageView UserImage;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    //progress dialog
    ProgressDialog pd;

    public MoreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MoreFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MoreFragment newInstance(String param1, String param2) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);


        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        firebaseAuth = FirebaseAuth.getInstance();

        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");
        storageReference = getInstance().getReference(); //firebase storage reference


        //init views
        UserImage = view.findViewById(R.id.profile_user);
        UserName = view.findViewById(R.id.display_name);

        //init progress dialog
        pd = new ProgressDialog(getActivity());

        /*We have to get info of currently signed in user. We can get it using user's email or uid
          I'm gonna retrieve user detail using email*/
        /*By using orderByChild query we will Show the detail from a node
        whose key named email has value equal to currently signed in email.
        It will search all nodes, where the key matches it will get its detail*/
        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String name  = "" + ds.child("name").getValue();
                    String image = "" + ds.child("image").getValue();

                    //set data
                    UserName.setText(name);

                    try {
                        //if image is received then set
                        Picasso.get().load(image).into(UserImage);
                    } catch (Exception e) {
                        //if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.user1_icon).into(UserImage);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profile = view.findViewById(R.id.edit_profile);
        //new
        usersTv = view.findViewById(R.id.usersTv);
        chatsTv = view.findViewById(R.id.chatsTv);
        groupChatsTv = view.findViewById(R.id.group_chatsTv);
        notifiSettingsTv = view.findViewById(R.id.notifi_settingsTv);

        usersTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                            //get data

                            String isBusinessOwner  = "" + ds.child("isBusinessOwner").getValue();
                            if(isBusinessOwner.equals("1")) {
                                //chat list fragment transaction
                                ((BusinessOwnerDashboardActivity) getActivity()).setActionBarTitle("الاشعارات");
                                NotificationsFragment fragment5 = new NotificationsFragment();
                                FragmentTransaction ft5 = getFragmentManager().beginTransaction();
                                ft5.replace(R.id.Buscontent, fragment5, "");
                                ft5.commit();
                            }else if (isBusinessOwner.equals("2")){
                                //chat list fragment transaction
                                ((IndividualDashboardActivity) getActivity()).setActionBarTitle("الاشعارات");
                                NotificationsFragment fragment5 = new NotificationsFragment();
                                FragmentTransaction ft5 = getFragmentManager().beginTransaction();
                                ft5.replace(R.id.content, fragment5, "");
                                ft5.commit();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        chatsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                            //get data

                            String isBusinessOwner  = "" + ds.child("isBusinessOwner").getValue();
                            if(isBusinessOwner.equals("1")) {
                                //chat list fragment transaction
                                //((BusinessOwnerDashboardActivity) getActivity()).setActionBarTitle("الدردشات");
                                ChatListFragment fragment5 = new ChatListFragment();
                                FragmentTransaction ft5 = getFragmentManager().beginTransaction();
                                ft5.replace(R.id.Buscontent, fragment5, "");
                                ft5.commit();
                            }else if (isBusinessOwner.equals("2")){
                                //chat list fragment transaction
                                //((IndividualDashboardActivity) getActivity()).setActionBarTitle("الدردشات");
                                ChatListFragment fragment5 = new ChatListFragment();
                                FragmentTransaction ft5 = getFragmentManager().beginTransaction();
                                ft5.replace(R.id.content, fragment5, "");
                                ft5.commit();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        groupChatsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                            //get data

                            String isBusinessOwner  = "" + ds.child("isBusinessOwner").getValue();
                            if(isBusinessOwner.equals("1")) {
                                //go to group chat activity
                                ((BusinessOwnerDashboardActivity) getActivity()).setActionBarTitle("الدردشات الجماعية");
                                GroupChatsFragment fragment6 = new GroupChatsFragment();
                                FragmentTransaction ft6 = getFragmentManager().beginTransaction();
                                ft6.replace(R.id.Buscontent, fragment6, "");
                                ft6.commit();
                            }else if (isBusinessOwner.equals("2")){
                                //go to group chat activity
                                ((IndividualDashboardActivity) getActivity()).setActionBarTitle("الدردشات الجماعية");
                                GroupChatsFragment fragment6 = new GroupChatsFragment();
                                FragmentTransaction ft6 = getFragmentManager().beginTransaction();
                                ft6.replace(R.id.content, fragment6, "");
                                ft6.commit();
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        notifiSettingsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });


        logOut = view.findViewById(R.id.logout);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //profile fragment transaction
                Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //check until required data get
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            //get data
                            String isBusinessOwner  = "" + ds.child("isBusinessOwner").getValue();
                            if(isBusinessOwner.equals("1")) {
                                //user is BusinessOwner
                                ((BusinessOwnerDashboardActivity) getActivity()).setActionBarTitle("الملف الشخصي");
                                BusinessOwnerProfileFragment fragment1 = new BusinessOwnerProfileFragment();
                                FragmentTransaction ft1 = getFragmentManager().beginTransaction();
                                ft1.replace(R.id.Buscontent, fragment1, "");
                                ft1.commit();
                            }else if (isBusinessOwner.equals("2")){
                                //user is Individual
                                ((IndividualDashboardActivity) getActivity()).setActionBarTitle("الملف الشخصي");
                                ProfileFragment fragment2 = new ProfileFragment();
                                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                                ft2.replace(R.id.content, fragment2, "");
                                ft2.commit();
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                checkUserStatus();
            }
        });
        return view;
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null){
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        }
        else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

}