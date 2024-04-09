package com.socialmedia.socialmedia.fragments;

import android.app.ProgressDialog;
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
import com.socialmedia.socialmedia.adapters.AdapterPosts;
import com.socialmedia.socialmedia.models.ModelPost;

import java.util.ArrayList;
import java.util.List;

public class PostFragment extends Fragment {


    //firebase
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    RecyclerView postsRecyclerView;
    ArrayList<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;
    //storage
    StorageReference storageReference;
    //progress dialog
    ProgressDialog pd;

    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        //init firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Posts");

        postsRecyclerView = view.findViewById(R.id.recyclerview_posts);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        postsRecyclerView.setLayoutManager(layoutManager);

        //init progress dialog
        pd = new ProgressDialog(getActivity());

        postList = new ArrayList<>();

        loadMyPosts();
        return view;
    }

    public void loadMyPosts() {
        //linear layout for recyclerview
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first, for this load from last
        //layoutManager.setStackFromEnd(true);
        //layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        //postsRecyclerView.setLayoutManager(layoutManager);


        //init posts list
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //qurey to load posts
        /*whenever user publishes a post the uid of this user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user*/
        //Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uid").equalTo(user.getUid());
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //init posts list
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //qurey to load posts
        /*whenever user publishes a post the uid of this user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user*/
        //Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        Query query1 = FirebaseDatabase.getInstance().getReference("BusPosts").orderByChild("uid").equalTo(user.getUid());
        //get all data from this ref
        query1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               // postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    /*public void searchMyPosts(final String searchQuery) {
        //linear layout for recyclerview
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //show newest post first, for this load from last
        //layoutManager.setStackFromEnd(true);
        //layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        //postsRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        //DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //qurey to load posts
        // *whenever user publishes a post the uid of this user is also saved as info of post
        // * so we're retrieving posts having uid equals to uid of current user
        Query query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("uid").equalTo(user.getUid());
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost myPosts = ds.getValue(ModelPost.class);


                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        //add to list
                        postList.add(myPosts);
                    }

                    //adapter
                    adapterPosts = new AdapterPosts(getActivity(), postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    } */

}