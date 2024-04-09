package com.socialmedia.socialmedia;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.socialmedia.socialmedia.adapters.AdapterPosts;
import com.socialmedia.socialmedia.fragments.PostBFragment;
import com.socialmedia.socialmedia.models.ModelPost;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileBOActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    //views from xml
    ImageView avatarIv, coverIv;
    TextView nameTv, bioTv;
    //RecyclerView postsRecyclerView;
    Button message, call;

    TabLayout tab;
    ViewPager viewPager;

    ArrayList<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_b_o_profile);

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle("Profile");
        actionBar.setTitle("");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //init views
        avatarIv = findViewById(R.id.avatarIv);
        coverIv = findViewById(R.id.coverIv);
        nameTv = findViewById(R.id.nameTv);
        bioTv = findViewById(R.id.bioTv);
        //postsRecyclerView = findViewById(R.id.recyclerview_posts);
        message = findViewById(R.id.message);
        call = findViewById(R.id.call);
        firebaseAuth = FirebaseAuth.getInstance();

        tab = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        PagerAdapterB adapter = new PagerAdapterB(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        tab.setupWithViewPager(viewPager);

        //get uid of clicked user to retrieve his posts
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");


        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //check until required data get
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    //get data
                    String name  = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String bio   = "" + ds.child("bio").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    //set data
                    nameTv.setText(name);
                    //emailTv.setText(email);
                    bioTv.setText(bio);
                    try {
                        //if image is received then set
                        Picasso.get().load(image).into(avatarIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image then set default
                        Picasso.get().load(R.drawable.user1_icon).into(avatarIv);
                    }

                    try {
                        //if image is received then set
                        Picasso.get().load(cover).into(coverIv);
                    } catch (Exception e) {
                        //if there is any exception while getting image then set default
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //message button click
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThereProfileBOActivity.this, ChatActivity.class);
                intent.putExtra("hisUid", uid);
                startActivity(intent);
            }
        });

        //call button click
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("uid").equalTo(uid);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        //check until required data get
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            //get phone
                            String phone = "" + ds.child("phone").getValue();

                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:"+phone));
                            startActivity(intent);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


        postList = new ArrayList<>();

        checkUserStatus();
        //loadHistPosts();

    }

    /*private void loadHistPosts() {
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postsRecyclerView.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //qurey to load posts
        // *whenever user publishes a post the uid of this user is also saved as info of post
        // * so we're retrieving posts having uid equals to uid of current user
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);

                    //add to list
                    postList.add(myPosts);

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    //set this adapter to recyclerview
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    } */

    private void searchHistPosts(final String searchQuery){
        PostBFragment postBFragment = new PostBFragment();
        //linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileBOActivity.this);
        //show newest post first, for this load from last
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        //set this layout to recyclerview
        postBFragment.postsRecyclerViewB.setLayoutManager(layoutManager);

        //init posts list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");
        //qurey to load posts
        /*whenever user publishes a post the uid of this user is also saved as info of post
         * so we're retrieving posts having uid equals to uid of current user*/
        Query query = ref.orderByChild("uid").equalTo(uid);
        //get all data from this ref
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelPost myPosts = ds.getValue(ModelPost.class);


                    if (myPosts.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPosts.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())){
                        //add to list
                        postList.add(myPosts);
                    }

                    //adapter
                    adapterPosts = new AdapterPosts(ThereProfileBOActivity.this, postList);
                    //set this adapter to recyclerview
                    postBFragment.postsRecyclerViewB.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ThereProfileBOActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        PostBFragment postBFragment = new PostBFragment();
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_add_post).setVisible(false); //hide add post from this activiyt
        menu.findItem(R.id.action_create_group).setVisible(false); //hide add post from this activiyt

        MenuItem item = menu.findItem(R.id.action_search);
        //v7 searchview ot search user specific posts
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s)){
                    //search
                    searchHistPosts(s);
                }
                else {
                    postBFragment.loadHistPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called whenever user type any letter
                if (!TextUtils.isEmpty(s)){
                    //search
                    searchHistPosts(s);
                }
                else {
                    postBFragment.loadHistPosts();
                }
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}