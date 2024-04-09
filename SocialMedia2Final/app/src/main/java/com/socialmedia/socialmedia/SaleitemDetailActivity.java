package com.socialmedia.socialmedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

public class SaleitemDetailActivity extends AppCompatActivity {


    //to get detail of user and post
    String hisUid, myUid, myEmail,
            postId, hisName, sImage, sPrice;



    //progress bar
    ProgressDialog pd;

    //views
    ImageView sImageIv;
    TextView sNameTv, sPriceTv, sTitleTv, sDescriptionTv;
    ImageButton moreBtn;
    Button sContact;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salteitem_detail);

        //Actionbar and its properties
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("تفاصيل المنشور");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //get id of post using intent
        Intent intent = getIntent();
        postId = intent.getStringExtra("postId");

        //init views
        sImageIv = findViewById(R.id.sImageIv);
        sNameTv = findViewById(R.id.sNameTv);
        sTitleTv = findViewById(R.id.sTitleTv);
        sPriceTv = findViewById(R.id.sPriceTv);
        sDescriptionTv = findViewById(R.id.sDescriptionTv);
        moreBtn = findViewById(R.id.moreBtn);
        sContact = findViewById(R.id.sContact);



        loadPostInfo();

        checkUserStatus();

        //loadUserInfo();

        sContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imBlockedORNot(hisUid);
            }
        });
        //more button click handle
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(moreBtn,  sImage);}

        });


    }

    private void addToHisNotifications(String hisUid, String pId, String notification){
        //timestamp for time and notification id
        String timestamp = ""+System.currentTimeMillis();

        //data to put in notification in firebase
        HashMap<Object, String> hashMap = new HashMap<>();
        hashMap.put("pId", pId);
        hashMap.put("timestamp", timestamp);
        hashMap.put("pUid", hisUid);
        hashMap.put("notification", notification);
        hashMap.put("sUid", myUid);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUid).child("Notifications").child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //added successfully
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed
                    }
                });
    }

    private void shareTextOnly(String pTitle, String pDescription) {
        //concatenate title and description to share
        String shareBody = pTitle +"\n"+ pDescription;

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.setType("text/plain");
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here"); //in case you share via an email app
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody); //text to share
        startActivity(Intent.createChooser(sIntent, "Share Via")); //message to show in share dialog

    }

    private void shareImageAndText(String pTitle, String pDescription, Bitmap bitmap) {
        //concatenate title and description to share
        String shareBody = pTitle +"\n"+ pDescription;

        //first we will save this image in cache, get the saved image uri
        Uri uri = saveImageToShare(bitmap);

        //share intent
        Intent sIntent = new Intent(Intent.ACTION_SEND);
        sIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        sIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here");
        sIntent.setType("image/png");
        startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs(); //create if not exists
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(this, "com.socialmedia.socialmedia.fileprovider", file);
        }
        catch (Exception e){
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        return uri;
    }



    private void showMoreOptions(ImageButton moreBtn, final String sImage) {
        //creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(this, moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user

        //add items in menu
        popupMenu.getMenu().add(Menu.NONE, 0, 0, "حذف");
        popupMenu.getMenu().add(Menu.NONE, 1, 0, "تعديل المنشور");

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==0){
                    //delete is clicked
                    beginDelete();
                }
                else if (id==1){
                    //Edit is clicked
                    //start AddPostActivity with key "editPost" and the id of the post clicked
                    Intent intent = new Intent(SaleitemDetailActivity.this, AddSaleitemActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", postId);
                    startActivity(intent);
                }
                return false;
            }
        });
        //show menu
        popupMenu.show();
    }

    private void beginDelete() {
        //post can be with or without image


        //post is with image
        deleteWithImage();

    }

    private void deleteWithImage() {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("جارٍ الحذف ...");

        /*Steps:
          1) Delete Image using url
          2) Delete from database using post id*/

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(sImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database

                        Query fquery = FirebaseDatabase.getInstance().getReference("Saleitems").orderByChild("sId").equalTo(postId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue(); // remove values from firebase where pid matches
                                }
                                //deleted
                                Toast.makeText(SaleitemDetailActivity.this, "حذف بنجاح", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //failed, can't go further
                        pd.dismiss();
                        Toast.makeText(SaleitemDetailActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("جارٍ الحذف ...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(postId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue(); // remove values from firebase where pid matches
                }
                //deleted
                Toast.makeText(SaleitemDetailActivity.this, "حذف بنجاح", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     private void loadUserInfo() {
     //get current user info
     Query myRef = FirebaseDatabase.getInstance().getReference("Users");
     myRef.orderByChild("uid").equalTo(myUid).addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    for (DataSnapshot ds: dataSnapshot.getChildren()){
    myName = ""+ds.child("name").getValue();

    //set data
    try {
    //if image is received then set
    Picasso.get().load(myDp).placeholder(R.drawable.user1_icon).into(cAvatarIv);
    }
    catch (Exception e){
    Picasso.get().load(R.drawable.user1_icon).into(cAvatarIv);
    }
    }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
    });
     }
     **/
    private void loadPostInfo() {
        //get post using the id of the post
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Saleitems");
        Query query = ref.orderByChild("sId").equalTo(postId);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //keep checking the posts until get the required post
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    //get data
                    String sTitle = ""+ds.child("sTitle").getValue();
                    String sDesc = ""+ds.child("sDesc").getValue();
                    sImage = ""+ds.child("sImage").getValue();
                    hisUid = ""+ds.child("uid").getValue();
                    String sEmail = ""+ds.child("sEmail").getValue();
                    hisName = ""+ds.child("sName").getValue();
                    sPrice = ""+ds.child("sPrice").getValue();



                    //set data
                    sTitleTv.setText(sTitle);
                    sDescriptionTv.setText(sDesc);
                    sNameTv.setText(hisName);
                    sPriceTv.setText(sPrice);

                    //set image of the user who posted
                    sImageIv.setVisibility(View.VISIBLE);

                    if (!(hisUid.equals(myUid))) {
                        moreBtn.setVisibility(View.INVISIBLE);
                    }
                    try {
                        Picasso.get().load(sImage).into(sImageIv);
                    }
                    catch (Exception e){

                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void imBlockedORNot(final String hisUID){
        //first check if sender(current user) is blocked by receiver or not
        //Logic: if uid of the sender(current user) exists in "BlockedUsers" of receiver then sender(current user) is blocked, otherwise not
        //if blocked then just display a message e.g. You're blocked by that user, can't send message
        //if not blocked then simply start the chat activity
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(hisUID).child("BlockedUsers").orderByChild("uid").equalTo(myUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            if (ds.exists()){
                                Toast.makeText(SaleitemDetailActivity.this, "لقد حظرك هذا المستخدم ، ولا يمكنك إرسال رسالة", Toast.LENGTH_SHORT).show();
                                //bocked, dont proceed further
                                return;
                            }
                        }
                        //not blocked, start activity
                        Intent intent = new Intent(SaleitemDetailActivity.this, ChatActivity.class);
                        intent.putExtra("hisUid", hisUID);
                        SaleitemDetailActivity.this.startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void checkUserStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!= null){
            //user is signed in
            myEmail = user.getEmail();
            myUid = user.getUid();
        }
        else {
            //user not signed in, go to main activity
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        //hide some menu items
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id = item.getItemId();
        if (id == R.id.action_logout){
            FirebaseAuth.getInstance().signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }
}
