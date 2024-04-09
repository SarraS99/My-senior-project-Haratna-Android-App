package com.socialmedia.socialmedia.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.socialmedia.socialmedia.R;
import com.socialmedia.socialmedia.adapters.AdapterMenu;
import com.socialmedia.socialmedia.models.ModelMenu;

import java.util.ArrayList;
import java.util.List;

public class MenuBFragment extends Fragment {


    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    //DatabaseReference databaseReference;
    RecyclerView menuRecyclerView;
    List<ModelMenu> menuList;
    AdapterMenu adapterMenu;
    String uid;
    //storage
    StorageReference storageReference;
    //progress dialog
    ProgressDialog pd;
    public MenuBFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu_b, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();

        menuRecyclerView = view.findViewById(R.id.menuRecyclerview);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        menuRecyclerView.setLayoutManager(layoutManager);

        //get uid of clicked user to retrieve his posts
        Intent intent = getActivity().getIntent();
        uid = intent.getStringExtra("uid");

        //init progress dialog
        pd = new ProgressDialog(getActivity());

        menuList = new ArrayList<>();

        loadMyPosts();
        return view;
    }

    public void loadMyPosts() {

        /*whenever user publishes a post the uid of this user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user*/
        //Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        //Query query = FirebaseDatabase.getInstance().getReference("Menu").orderByChild("uid").equalTo(user.getUid());
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Menu");
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                menuList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelMenu myPosts = ds.getValue(ModelMenu.class);

                    //add to list
                    menuList.add(myPosts);

                    //adapter
                    adapterMenu = new AdapterMenu(getActivity(), menuList);
                    //set this adapter to recyclerview
                    menuRecyclerView.setAdapter(adapterMenu);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}