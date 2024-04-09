package com.socialmedia.socialmedia;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.socialmedia.socialmedia.fragments.BusinessesFragment;
import com.socialmedia.socialmedia.fragments.ForsaleFragment;
import com.socialmedia.socialmedia.fragments.HomeFragment;
import com.socialmedia.socialmedia.fragments.IndividualBusinessesFragment;
import com.socialmedia.socialmedia.fragments.MoreFragment;
import com.socialmedia.socialmedia.fragments.NotificationsFragment;
import com.socialmedia.socialmedia.fragments.UsersFragment;
import com.socialmedia.socialmedia.notifications.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class IndividualDashboardActivity extends AppCompatActivity {

    //firebase auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    String mUID;

    private  BottomNavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_dashboard);

        //Actionbar and its title
        actionBar = getSupportActionBar();
        actionBar.setTitle("ملف التعريف");

        //init
        firebaseAuth = FirebaseAuth.getInstance();

        //bottom navigation
        navigationView = findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //home fragment transaction (default, on star)
        actionBar.setTitle("الواجهة الرئيسية");//change actionbar title
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1, "");
        ft1.commit();

        checkUserStatus();



    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //handle item clicks
                    switch (menuItem.getItemId()) {
                        case R.id.nav_home:
                            //home fragment transaction
                            actionBar.setTitle("الواجهة الرئيسية");//change actionbar title
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1, "");
                            ft1.commit();
                            return true;
                        case R.id.nav_profile:
                            //For Sale fragment transaction
                            actionBar.setTitle("للبيع");//change actionbar title
                            ForsaleFragment fragment2 = new ForsaleFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2, "");
                            ft2.commit();
                            return true;
                        case R.id.nav_users:
                            //Bus fragment transaction
                            actionBar.setTitle("متاجر");//change actionbar title
                            IndividualBusinessesFragment fragment3 = new IndividualBusinessesFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3, "");
                            ft3.commit();
                            return true;
                        case R.id.nav_chat:
                            //Notification fragment transaction
                            actionBar.setTitle("المستخدمين");//change actionbar title
                            UsersFragment fragment4 = new UsersFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, fragment4, "");
                            ft4.commit();
                            return true;
                        case R.id.nav_more:
                            //showMoreOptions();
                            actionBar.setTitle("المزيد");//change actionbar title
                            MoreFragment fragment5 = new MoreFragment();
                            FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                            ft5.replace(R.id.content, fragment5, "");
                            ft5.commit();
                            return true;
                    }

                    return false;
                }
            };

    private void showMoreOptions() {
        //popup menu to show more options
        PopupMenu popupMenu = new PopupMenu(this, navigationView, Gravity.END);
        //items to show in menu
        popupMenu.getMenu().add(Menu.NONE,0,0, "Notifications");
        popupMenu.getMenu().add(Menu.NONE,1,0, "Group Chats");
        popupMenu.getMenu().add(Menu.NONE,2,0, "Pinned Posts");

        //menu clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == 0){
                    //notifications clicked

                    //Notifications fragment transaction
                    actionBar.setTitle("الإشعارات");//change actionbar title
                    NotificationsFragment fragment5 = new NotificationsFragment();
                    FragmentTransaction ft5 = getSupportFragmentManager().beginTransaction();
                    ft5.replace(R.id.content, fragment5, "");
                    ft5.commit();
                }
                else if (id == 1){
                    //group chats clicked

                    //Notifications fragment transaction
                    actionBar.setTitle("الدردشات الجماعية");//change actionbar title
                    GroupChatsFragment fragment6 = new GroupChatsFragment();
                    FragmentTransaction ft6 = getSupportFragmentManager().beginTransaction();
                    ft6.replace(R.id.content, fragment6, "");
                    ft6.commit();
                }

                else if (id == 2){
                    //Pinned Posts clicked
                    startActivity(new Intent(IndividualDashboardActivity.this, MapPinnedPostsActivity.class));
                }
                return false;

            }
        });
        popupMenu.show();
    }

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            //user is signed in stay here
            //set email of logged in user
            //mProfileTv.setText(user.getEmail());
            mUID = user.getUid();

            //save uid of currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();

        } else {
            //user not signed in, go to main acitivity
            startActivity(new Intent(IndividualDashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatus();
        super.onStart();
    }

    public void setActionBarTitle(String title){
        actionBar.setTitle(title);
    }

}