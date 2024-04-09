package com.socialmedia.socialmedia.fragments;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import com.socialmedia.socialmedia.AddBusPostActivity;
import com.socialmedia.socialmedia.AddPostActivity;
import com.socialmedia.socialmedia.MainActivity;
import com.socialmedia.socialmedia.R;
import com.socialmedia.socialmedia.SettingsActivity;
import com.socialmedia.socialmedia.adapters.AdapterBusPosts;
import com.socialmedia.socialmedia.adapters.AdapterPosts;
import com.socialmedia.socialmedia.models.ModelPost;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */

public class BusinessesFragment extends Fragment{

    private static final String TAG = "BUSINESSES_TAG";

    ImageView mPlumbers, mDoctors, mRestaurants, mHair_salon, mContractors, mMechanics, mLegal, mTeachers, mSee_all;

    //firebase auth
    FirebaseAuth firebaseAuth;
    private String myUid = "";
    private String myNeighbourhood = "";
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    RecyclerView recyclerView;
    List<ModelPost> postList;
    AdapterBusPosts adapterBusPosts;

    public BusinessesFragment() {
        // Required empty public constructor
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_businesses, container, false);

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = "" + firebaseAuth.getUid();

        //recycler view and its properties
        recyclerView = view.findViewById(R.id.postsRecyclerviewBus);

        //init post list
        postList = new ArrayList<>();

        loadMyInfo();


        mPlumbers = view.findViewById(R.id.Plumbers);
        mDoctors = view.findViewById(R.id.Doctors);
        mRestaurants = view.findViewById(R.id.restaurants);
        mHair_salon = view.findViewById(R.id.hair_salon);
        mContractors = view.findViewById(R.id.contractors);
        mMechanics = view.findViewById(R.id.mechanics);
        mLegal = view.findViewById(R.id.legal);
        mTeachers = view.findViewById(R.id.teachers);
        mSee_all = view.findViewById(R.id.see_all);


        mPlumbers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlumbersFragment fragment2 = new PlumbersFragment();
                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                ft2.replace(R.id.Buscontent, fragment2, "");
                ft2.commit();
            }
        });

        mDoctors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoctorsFragment fragment2 = new DoctorsFragment();
                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                ft2.replace(R.id.Buscontent, fragment2, "");
                ft2.commit();
            }
        });

        mRestaurants.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestaurantsFragment fragment2 = new RestaurantsFragment();
                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                ft2.replace(R.id.Buscontent, fragment2, "");
                ft2.commit();
            }
        });

        mHair_salon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HairSalonFragment fragment2 = new HairSalonFragment();
                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                ft2.replace(R.id.Buscontent, fragment2, "");
                ft2.commit();
            }
        });

        mContractors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContractorsFragment fragment2 = new ContractorsFragment();
                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                ft2.replace(R.id.Buscontent, fragment2, "");
                ft2.commit();
            }
        });

        mMechanics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MechanicsFragment fragment2 = new MechanicsFragment();
                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                ft2.replace(R.id.Buscontent, fragment2, "");
                ft2.commit();
            }
        });

        mLegal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LegalFragment fragment2 = new LegalFragment();
                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                ft2.replace(R.id.Buscontent, fragment2, "");
                ft2.commit();
            }
        });

        mTeachers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TeachersFragment fragment2 = new TeachersFragment();
                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                ft2.replace(R.id.Buscontent, fragment2, "");
                ft2.commit();
            }
        });

        mSee_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SeeAllFragment fragment2 = new SeeAllFragment();
                FragmentTransaction ft2 = getFragmentManager().beginTransaction();
                ft2.replace(R.id.Buscontent, fragment2, "");
                ft2.commit();
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

                        Log.d(TAG, "onDataChange: myNeighbourhood: " + myNeighbourhood);
                        loadPosts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadPosts() {
        //adapter
        adapterBusPosts = new AdapterBusPosts(getActivity(), postList);
        //set adapter to recyclerview
        recyclerView.setAdapter(adapterBusPosts);

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BusPosts");        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost model = ds.getValue(ModelPost.class);

                    String pUid = ""+model.getUid();

                    DatabaseReference refUser = FirebaseDatabase.getInstance().getReference("Users");
                    refUser.child(pUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String postUserNeighborhood = "" + snapshot.child("UserNeighborhood").getValue();

                                    if (postUserNeighborhood.equals(myNeighbourhood)) {
                                        postList.add(model);
                                        adapterBusPosts.notifyDataSetChanged();
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
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPosts(final String searchQuery){

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BusPosts");        //get all data from this ref
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost modelPost = ds.getValue(ModelPost.class);


                    if (modelPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            modelPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        postList.add(modelPost);
                    }

                    //adapter
                    adapterBusPosts = new AdapterBusPosts(getActivity(), postList);
                    //set adapter to recyclerview
                    recyclerView.setAdapter(adapterBusPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error
                Toast.makeText(getActivity(), ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


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
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_create_group).setVisible(false);


        //searchview to search posts by post title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }
                else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)){
                    searchPosts(s);
                }
                else {
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
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if (id == R.id.action_add_post){
            FirebaseUser user = firebaseAuth.getCurrentUser();
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference("Users");
            storageReference = getInstance().getReference(); //firebase storage reference


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
                        String isBusinessOwner  = "" + ds.child("isBusinessOwner").getValue();
                        if(isBusinessOwner.equals("1")) {
                            startActivity(new Intent(getActivity(), AddBusPostActivity.class));
                        }else if (isBusinessOwner.equals("2")){
                            Toast.makeText(getActivity(), "هذه الصفحة مخصصة لإنشاء منشورات المتاجر", Toast.LENGTH_SHORT).show();
                        }

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else if (id== R.id.action_settings){
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }
}
