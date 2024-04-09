package com.socialmedia.socialmedia.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.socialmedia.socialmedia.AddPostActivity;
import com.socialmedia.socialmedia.MainActivity;
import com.socialmedia.socialmedia.MapPinnedPostsActivity;
import com.socialmedia.socialmedia.R;
import com.socialmedia.socialmedia.SettingsActivity;
import com.socialmedia.socialmedia.adapters.AdapterPosts;
import com.socialmedia.socialmedia.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialmedia.socialmedia.models.ModelUser;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HOME_TAG";

    //firebase auth
    private FirebaseAuth firebaseAuth;
    private String myUid = "";
    private String myNeighbourhood = "";
    private boolean isBusinessOwner = false;

    FloatingActionButton pinnedPostsMapFab;
    RecyclerView recyclerView;
    private ArrayList<ModelPost> postList;
    AdapterPosts adapterPosts;
    boolean once = true;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = "" + firebaseAuth.getUid();

        //recycler view and its properties
        pinnedPostsMapFab = view.findViewById(R.id.pinnedPostsMapFab);
        recyclerView = view.findViewById(R.id.postsRecyclerview);

        pinnedPostsMapFab.setVisibility(View.GONE);

        loadMyInfo();
        pinnedPostsMapFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, MapPinnedPostsActivity.class));
            }
        });

        return view;
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myNeighbourhood = "" + snapshot.child("UserNeighborhood").getValue();
                        String isBusinessOwnerString  = "" + snapshot.child("isBusinessOwner").getValue();
                        if(isBusinessOwnerString.equals("1")) {
                            //Business
                            isBusinessOwner = true;
                            //pinnedPostsMapFab.setVisibility(View.GONE);
                            pinnedPostsMapFab.setVisibility(View.VISIBLE);
                        }else if (isBusinessOwnerString.equals("2")){
                            //Individual
                            isBusinessOwner = false;
                            pinnedPostsMapFab.setVisibility(View.VISIBLE);
                        }

                        Log.d(TAG, "onDataChange: myNeighbourhood: " + myNeighbourhood);
                        loadPosts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadPosts() {
        postList = new ArrayList<>();
        postList.clear();

        //adapter
        adapterPosts = new AdapterPosts(getActivity(), postList);
        //set adapter to recyclerview
        recyclerView.setAdapter(adapterPosts);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String s) {
                ModelPost model = snapshot.getValue(ModelPost.class);
                /*postList.add(model);
                adapterPosts.notifyItemInserted(postList.size() - 1);*/
                String pUid = ""+model.getUid();

                DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("Users");
                refUser.child(pUid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String postUserNeighborhood = "" + snapshot.child("UserNeighborhood").getValue();

                                if (postUserNeighborhood.equals(myNeighbourhood)) {
                                    postList.add(model);
                                    adapterPosts.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String s) {
                Log.i("mytag", "child changed");
                ModelPost model = snapshot.getValue(ModelPost.class);
                for (int i = 0; i < postList.size(); i++) {
                    try{
                    if (postList.get(i).getpId().equals(model.getpId())) {
                        postList.set(i, model);
                        Log.i("mytag", "changed child found at position: " + i);
                        adapterPosts.notifyItemChanged(i);
                        break;
                    }
                }catch (Exception e){
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Log.i("mytag", "child removed");
                ModelPost model = snapshot.getValue(ModelPost.class);
                for (int i = 0; i < postList.size(); i++) {
                    if (postList.get(i).getpId().equals(model.getpId())) {
                        postList.remove(i);
                        Log.i("mytag", "removed child found at position: " + i);
                        adapterPosts.notifyItemRemoved(i);
                        break;
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void searchPosts(final String searchQuery) {
        postList = new ArrayList<>();
        //adapter
        adapterPosts = new AdapterPosts(getActivity(), postList);
        //set adapter to recyclerview
        recyclerView.setAdapter(adapterPosts);

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //get all data from this ref
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("Users");
                    refUser.child(modelPost.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String postUserNeighborhood = "" + snapshot.child("UserNeighborhood").getValue();

                                    if (postUserNeighborhood.equals(myNeighbourhood)) {
                                        try {
                                            if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                                    modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                                                postList.add(modelPost);
                                                adapterPosts.notifyDataSetChanged();
                                            }
                                        }catch (Exception e){

                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("BusPosts");
        //get all data from this ref
        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelPost modelPost = ds.getValue(ModelPost.class);

                    DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("Users");
                    refUser.child(modelPost.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String postUserNeighborhood = "" + snapshot.child("UserNeighborhood").getValue();

                                    if (postUserNeighborhood.equals(myNeighbourhood)) {
                                        try {
                                            if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                                    modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                                                postList.add(modelPost);
                                                adapterPosts.notifyDataSetChanged();
                                            }
                                        }catch (Exception e){

                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//to show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    /*inflate options menu*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflating menu
        inflater.inflate(R.menu.menu_main, menu);

        //hide some options
        menu.findItem(R.id.action_create_group).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);

        //searchview to search posts by post title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setQuery("", true);
        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s);
                } else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s);
                } else{
                    loadPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    /*handle menu item clicks*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        } else if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        } else if (id == R.id.action_settings) {
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
