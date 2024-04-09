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
import androidx.cardview.widget.CardView;
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
import com.socialmedia.socialmedia.AddPostActivity;
import com.socialmedia.socialmedia.PostDetailActivity;
import com.socialmedia.socialmedia.R;
import com.socialmedia.socialmedia.SaleitemDetailActivity;
import com.socialmedia.socialmedia.models.ModelSaleItems;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;

public class AdapterSaleItems extends RecyclerView.Adapter<AdapterSaleItems.MyHolder>{

    Context context;
    List<ModelSaleItems> saleList;

    String myUid;

    private DatabaseReference likesRef; //for likes database node
    private DatabaseReference postsRef; //reference of posts

    boolean mProcessLike=false;

    public AdapterSaleItems(Context context, List<ModelSaleItems> postList) {
        this.context = context;
        this.saleList = postList;
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //inflate layout row_post.xml
        View view = LayoutInflater.from(context).inflate(R.layout.row_saleitems, viewGroup, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder myHolder, final int i) {
        //get data
        final String uid = saleList.get(i).getUid();
        String uEmail = saleList.get(i).getsEmail();
        String uName = saleList.get(i).getsName();
        final String sId = saleList.get(i).getsId();
        final String sTitle = saleList.get(i).getsTitle();
        final String sDescription = saleList.get(i).getsDesc();
        final String sImage = saleList.get(i).getsImage();
        final String sPrice = saleList.get(i).getsPrice();
        String priceStr = sPrice + "﷼";

        //set data

        myHolder.sTitleTv.setText(sTitle);
        myHolder.sPriceTv.setText(priceStr);




        //set post image
        //if there is no image i.e. pImage.equals("noImage") then hide ImageView
        if (sImage == null){
            //hide imageview
            myHolder.sImageIv.setVisibility(View.GONE);
        }
        else {
            //show imageview
            myHolder.sImageIv.setVisibility(View.VISIBLE);

            try {
                Picasso.get().load(sImage).into(myHolder.sImageIv);
            }
            catch (Exception e){

            }
        }


        myHolder.itemCardCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start saleitemDetailActivity
                Intent intent = new Intent(context, SaleitemDetailActivity.class);
                intent.putExtra("postId", sId); //will get detail of post using this id, its id of the post clicked
                context.startActivity(intent);
            }
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



    private void showMoreOptions(ImageButton moreBtn, String uid, String myUid, final String pId, final String pImage) {
        //creating popup menu currently having option Delete, we will add more options later
        PopupMenu popupMenu = new PopupMenu(context, moreBtn, Gravity.END);

        //show delete option in only post(s) of currently signed-in user
        if (uid.equals(myUid)){
            //add items in menu
            popupMenu.getMenu().add(Menu.NONE, 0, 0, "حذف");
            popupMenu.getMenu().add(Menu.NONE, 1, 0, "تعديل");
        }
        popupMenu.getMenu().add(Menu.NONE, 2, 0, "مشاهدة التفاصيل");

        //item click listener
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id==0){
                    //delete is clicked
                    beginDelete(pId, pImage);
                }
                else if (id==1){
                    //Edit is clicked
                    //start AddPostActivity with key "editPost" and the id of the post clicked
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("key", "editPost");
                    intent.putExtra("editPostId", pId);
                    context.startActivity(intent);
                }
                else if (id==2){
                    //start PostDetailActivity
                    Intent intent = new Intent(context, PostDetailActivity.class);
                    intent.putExtra("postId", pId); //will get detail of post using this id, its id of the post clicked
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

        if (pImage.equals("noImage")){
            //post is without image
            deleteWithoutImage(pId);
        }
        else {
            //post is with image
            deleteWithImage(pId, pImage);
        }
    }

    private void deleteWithImage(final String pId, String pImage) {
        //progress bar
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("حذف...");

        /*Steps:
          1) Delete Image using url
          2) Delete from database using post id*/

        StorageReference picRef = FirebaseStorage.getInstance().getReferenceFromUrl(pImage);
        picRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //image deleted, now delete database

                        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
                        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds: dataSnapshot.getChildren()){
                                    ds.getRef().removeValue(); // remove values from firebase where pid matches
                                }
                                //deleted
                                Toast.makeText(context, "حذف بنجاح", Toast.LENGTH_SHORT).show();
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

    private void deleteWithoutImage(String pId) {
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("حذف...");

        Query fquery = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("pId").equalTo(pId);
        fquery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ds.getRef().removeValue(); // remove values from firebase where pid matches
                }
                //deleted
                Toast.makeText(context, "حذف بنجاح", Toast.LENGTH_SHORT).show();
                pd.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return saleList.size();
    }

    //view holder class
    class MyHolder extends RecyclerView.ViewHolder{

        //views from row_post.xml
        ImageView sImageIv;
        TextView sTitleTv, sPriceTv;
        CardView itemCardCv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //init views

            sImageIv = itemView.findViewById(R.id.sImageIv);
            sPriceTv = itemView.findViewById(R.id.sPriceTv);
            sTitleTv = itemView.findViewById(R.id.sTitleTv);
            itemCardCv = itemView.findViewById(R.id.itemCardCv);

        }
    }

}