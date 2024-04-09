package com.socialmedia.socialmedia.fragments;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.socialmedia.socialmedia.AddSaleitemActivity;
import com.socialmedia.socialmedia.MainActivity;
import com.socialmedia.socialmedia.R;
import com.socialmedia.socialmedia.adapters.AdapterSaleItems;
import com.socialmedia.socialmedia.models.ModelSaleItems;

import java.util.ArrayList;
import java.util.List;


public class ForsaleFragment extends Fragment {

    private static final String TAG = "FOR_SALE_TAG";
    //firebase auth
    private FirebaseAuth firebaseAuth;
    private String myUid = "";
    private String myNeighbourhood = "";

    RecyclerView recyclerView;
    List<ModelSaleItems> saleList;
    AdapterSaleItems adapterSaleItems;
    ToggleButton btnDecor, btnKitchen, btnElectro, btnOffice;

    public ForsaleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forsale, container, false);

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        myUid = firebaseAuth.getUid();

        //recycler view and its properties
        recyclerView = view.findViewById(R.id.saleRecyclerview);

        //init post list
        saleList = new ArrayList<>();

        loadMyInfo();

        btnDecor = view.findViewById(R.id.btnDecor);
        btnKitchen = view.findViewById(R.id.btnKitchen);
        btnElectro = view.findViewById(R.id.btnElectro);
        btnOffice = view.findViewById(R.id.btnOffice);

        btnDecor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked) {
                    btnKitchen.setChecked(false);
                    btnElectro.setChecked(false);
                    btnOffice.setChecked(false);
                    Drawable img = getContext().getResources().getDrawable(R.drawable.ic_baseline_close_24);
                    img.setBounds(0, 0, 60, 60);
                    btnDecor.setCompoundDrawables(img, null, null, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        btnDecor.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.yellow));
                    }
                    searchPosts("ديكور المنزل", false);

                } else {
                    btnDecor.setCompoundDrawables(null, null, null, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        btnDecor.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.gray4));
                    }
                    loadPosts();

                }
            }
        });
        btnKitchen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked) {
                    btnDecor.setChecked(false);
                    btnElectro.setChecked(false);
                    btnOffice.setChecked(false);
                    Drawable img = getContext().getResources().getDrawable(R.drawable.ic_baseline_close_24);
                    img.setBounds(0, 0, 60, 60);
                    btnKitchen.setCompoundDrawables(img, null, null, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        btnKitchen.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.yellow));
                    }
                    searchPosts("أواني طبخ", false);
                } else {
                    btnKitchen.setCompoundDrawables(null, null, null, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        btnKitchen.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.gray4));
                    }
                    loadPosts();


                }
            }
        });
        btnElectro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked) {
                    btnKitchen.setChecked(false);
                    btnDecor.setChecked(false);
                    btnOffice.setChecked(false);
                    Drawable img = getContext().getResources().getDrawable(R.drawable.ic_baseline_close_24);
                    img.setBounds(0, 0, 60, 60);
                    btnElectro.setCompoundDrawables(img, null, null, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        btnElectro.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.yellow));
                    }
                    searchPosts("أدوات كهربائية", false);
                } else {
                    btnElectro.setCompoundDrawables(null, null, null, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        btnElectro.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.gray4));
                    }
                    loadPosts();

                }
            }
        });
        btnOffice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton toggleButton, boolean isChecked) {
                if (isChecked) {
                    btnKitchen.setChecked(false);
                    btnElectro.setChecked(false);
                    btnDecor.setChecked(false);
                    Drawable img = getContext().getResources().getDrawable(R.drawable.ic_baseline_close_24);
                    img.setBounds(0, 0, 60, 60);
                    btnOffice.setCompoundDrawables(img, null, null, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        btnOffice.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.yellow));
                    }
                    searchPosts("أدوات مكتبية", false);
                } else {
                    btnOffice.setCompoundDrawables(null, null, null, null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        btnOffice.setBackgroundTintList(getContext().getResources().getColorStateList(R.color.gray4));
                    }
                    loadPosts();
                }
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

                        //loadPosts();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadPosts() {

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Saleitems");
        //get all data from this ref
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                saleList.clear();
                //adapter
                adapterSaleItems = new AdapterSaleItems(getActivity(), saleList);
                //set adapter to recyclerview
                recyclerView.setAdapter(adapterSaleItems);

                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    try {
                        ModelSaleItems modelSale = ds.getValue(ModelSaleItems.class);
                        String sellerUid = "" + modelSale.getUid();

                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                        userRef.child(sellerUid)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String userNeighborhood = "" + snapshot.child("UserNeighborhood").getValue();

                                        if (userNeighborhood.equals(myNeighbourhood)) {
                                            saleList.add(modelSale);
                                            adapterSaleItems.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    } catch (Exception e) {
                        Log.e(TAG, "onDataChange: ", e);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchPosts(final String searchQuery, boolean notFilter) {

        //path of all posts
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Saleitems");
        //get all data from this ref
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                saleList.clear();
                //adapter
                adapterSaleItems = new AdapterSaleItems(getActivity(), saleList);
                //set adapter to recyclerview
                recyclerView.setAdapter(adapterSaleItems);

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ModelSaleItems modelSale = ds.getValue(ModelSaleItems.class);

                    String sellerUid = "" + modelSale.getUid();

                    if (notFilter) {
                        if (modelSale.getsTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                                modelSale.getsDesc().toLowerCase().contains(searchQuery.toLowerCase())) {

                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                            userRef.child(sellerUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String userNeighborhood = "" + snapshot.child("UserNeighborhood").getValue();

                                            if (userNeighborhood.equals(myNeighbourhood)) {
                                                saleList.add(modelSale);
                                                adapterSaleItems.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }
                    } else {
                        if (modelSale.getsCategory().toLowerCase().contains(searchQuery.toLowerCase())) {


                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
                            userRef.child(sellerUid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String userNeighborhood = "" + snapshot.child("UserNeighborhood").getValue();

                                            if (userNeighborhood.equals(myNeighbourhood)) {
                                                saleList.add(modelSale);
                                                adapterSaleItems.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //in case of error
                Toast.makeText(getActivity(), "" + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


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
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_logout).setVisible(false);


        //searchview to search posts by post title/description
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s, true);
                } else {
                    loadPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called as and when user press any letter
                if (!TextUtils.isEmpty(s)) {
                    searchPosts(s, true);
                } else {
                    loadPosts();
                }
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
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

    /*handle menu item clicks*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //get item id
        int id = item.getItemId();

        if (id == R.id.action_add_post) {
            startActivity(new Intent(getActivity(), AddSaleitemActivity.class));
        }

        return true;
    }


}