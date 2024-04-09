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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialmedia.socialmedia.GroupCreateActivity;
import com.socialmedia.socialmedia.MainActivity;
import com.socialmedia.socialmedia.R;
import com.socialmedia.socialmedia.SettingsActivity;
import com.socialmedia.socialmedia.adapters.AdapterUsers;
import com.socialmedia.socialmedia.models.ModelUser;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantsFragment extends Fragment {
    private static final String TAG = "USERS_TAG";

    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    private String myUid = "";
    private String myNeighbourhood = "";

    Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public RestaurantsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();

        //init recyclerview
        recyclerView = view.findViewById(R.id.users_recyclerView);
        //set it's properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        //init user list
        userList = new ArrayList<>();


        loadMyInfo();

        return view;
    }


    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(myUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        myNeighbourhood = "" + snapshot.child("UserNeighborhood").getValue();

                        Log.d(TAG, "onDataChange: myNeighbourhood: " + myNeighbourhood);

                        getAllUsers();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getAllUsers() {
        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    try {
                        ModelUser modelUser = ds.getValue(ModelUser.class);

                        String userNeighborhood = ""+ds.child("UserNeighborhood").getValue();
                        String category = "" + ds.child("category").getValue();
                        modelUser.setUserNeighborhood(userNeighborhood);
                        Log.d(TAG, "onDataChange: userNeighborhood: "+userNeighborhood);

                        //get all users except currently signed in user
                        if (!modelUser.getUid().equals(firebaseAuth.getUid()) && userNeighborhood.equals(myNeighbourhood) && category.equals("3")) {
                            userList.add(modelUser);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: ", e);
                    }

                    //adapter
                    adapterUsers = new AdapterUsers(mContext, userList);
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void searchUsers(final String query) {

        //get current user
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named "Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    try {
                        ModelUser modelUser = ds.getValue(ModelUser.class);

                        /*Conditions to fulfil search:
                         * 1) User not current user
                         * 2) The user name or email contains text entered in SearchView (case insensitive)*/

                        //get all searched users except currently signed in user
                        if (!modelUser.getUid().equals(fUser.getUid())) {

                            if (modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                                    modelUser.getEmail().toLowerCase().contains(query.toLowerCase())) {
                                userList.add(modelUser);
                            }

                        }
                    } catch (Exception e) {

                    }


                    //adapter
                    adapterUsers = new AdapterUsers(mContext, userList);
                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    //set adapter to recycler view
                    recyclerView.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
            startActivity(new Intent(mContext, MainActivity.class));
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

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);

        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);

        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim())) {
                    //search text contains text, search it
                    searchUsers(s);
                } else {
                    //search text empty, get all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called whenever user press any single letter
                //if search query is not empty then search
                if (!TextUtils.isEmpty(s.trim())) {
                    //search text contains text, search it
                    searchUsers(s);
                } else {
                    //search text empty, get all users
                    getAllUsers();
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
        } else if (id == R.id.action_settings) {
            //go to settings activity
            startActivity(new Intent(mContext, SettingsActivity.class));
        } else if (id == R.id.action_create_group) {
            //go to GroupCreateActivity activity
            startActivity(new Intent(mContext, GroupCreateActivity.class));
        }


        return super.onOptionsItemSelected(item);
    }

}
