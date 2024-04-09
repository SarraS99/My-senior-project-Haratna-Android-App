package com.socialmedia.socialmedia.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.socialmedia.socialmedia.AddMenuActivity;
import com.socialmedia.socialmedia.AddPostActivity;
import com.socialmedia.socialmedia.PostDetailActivity;
import com.socialmedia.socialmedia.R;
import com.socialmedia.socialmedia.models.ModelMenu;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

public class AdapterMenu extends RecyclerView.Adapter<AdapterMenu.MyHolder>{

    Context context;
    List<ModelMenu> menuList;

    String myUid;
    boolean mProcessLike=false;

    public AdapterMenu(Context context, List<ModelMenu> postList) {
        this.context = context;
        this.menuList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_menu, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        //get data
        final String uid = menuList.get(i).getUid();
        String uEmail = menuList.get(i).getmEmail();
        String uName = menuList.get(i).getmName();
        final String mId = menuList.get(i).getmId();
        final String mTitle = menuList.get(i).getmTitle();
        final String mDescription = menuList.get(i).getmDesc();
        final String mImage = menuList.get(i).getmImage();
        final String mPrice = menuList.get(i).getmPrice();
        String priceStr = mPrice + "﷼";

        //set data

        myHolder.mTitleTv.setText(mTitle);
        myHolder.mDescription.setText(mDescription);
        myHolder.mPriceTv.setText(priceStr);




        //set post image
        //if there is no image i.e. pImage.equals("noImage") then hide ImageView
        if (mImage == null){
            //hide imageview
            myHolder.mImageIv.setVisibility(View.GONE);
        }
        else {
            //show imageview
            myHolder.mImageIv.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(mImage).into(myHolder.mImageIv);
            }
            catch (Exception e){

            }
        }

        //handle button clicks,
        myHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMoreOptions(myHolder.moreBtn, uid, myUid, mId, mImage);
            }
        });



        //click like count to start PostLikedByActiivty, and pass the post id


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
        hashMap.put("mUid", myUid);

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
        context.startActivity(Intent.createChooser(sIntent, "Share Via")); //message to show in share dialog

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
        context.startActivity(Intent.createChooser(sIntent, "Share Via"));
    }

    private Uri saveImageToShare(Bitmap bitmap) {
        File imageFolder = new File(context.getCacheDir(), "images");
        Uri uri = null;
        try {
            imageFolder.mkdirs(); //create if not exists
            File file = new File(imageFolder, "shared_image.png");

            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            stream.flush();
            stream.close();
            uri = FileProvider.getUriForFile(context, "com.socialmedia.socialmedia.fileprovider",
                    file);
        }
        catch (Exception e){
            Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

        }
        return uri;
    }



    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String mId, final String mImage) {
        //creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        if (uid.equals(myUid)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "حذف");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "تعديل");
        }

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==0){
                    //delete is clicked
                    beginDelete(mId, mImage);
                }
                else if (id==1){
                    //Edit is clicked
                    //start AddPostActivity with key "editPost" and the id of the post clicked
                    Intent intent = new Intent(context, AddMenuActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", mId);
                    context.startActivity(intent);
                }

                return false;
            }
        });
        //show menu
        popupMenu.show();

    }

    private void beginDelete(String pId, String pImage) {
        //post can be with or without image

            deleteWithImage(pId, pImage);

    }

    private void deleteWithImage(final String mId, String mImage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("جارٍ الحذف ...");

        /*Steps:
          1) Delete Image using url
          2) Delete from database using post id*/

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(mImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database

                        Query fquery = FirebaseDatabase.getInstance().getReference("Menu").orderByChild("mId").equalTo(mId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue(); // remove values from firebase where pid matches
                                }
                                //deleted
                                Toast.makeText(context, "تم الحذف بنجاح", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteWithoutImage(String mId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("جارٍ الحذف ...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Menu").orderByChild("mId").equalTo(mId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue(); // remove values from firebase where pid matches
                }
                //deleted
                Toast.makeText(context, "تم الحذف بنجاح", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //views from row_post.xml
        ImageView mImageIv;
        TextView mTitleTv, mPriceTv, mDescription;
        ImageButton moreBtn;


        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views

            mImageIv = itemView.findViewById(R.id.mImageIv);
            mDescription = itemView.findViewById(R.id.mDescription);
            mPriceTv = itemView.findViewById(R.id.mPriceTv);
            mTitleTv = itemView.findViewById(R.id.mTitleTv);
            moreBtn = itemView.findViewById(R.id.moreBtn);

        }
    }

}