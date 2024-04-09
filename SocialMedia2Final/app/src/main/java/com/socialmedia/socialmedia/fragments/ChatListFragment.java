package com.socialmedia.socialmedia.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.socialmedia.socialmedia.GroupCreateActivity;
import com.socialmedia.socialmedia.MainActivity;
import com.socialmedia.socialmedia.R;
import com.socialmedia.socialmedia.SettingsActivity;
import com.socialmedia.socialmedia.adapters.AdapterChatlist;
import com.socialmedia.socialmedia.models.ModelChat;
import com.socialmedia.socialmedia.models.ModelChatlist;
import com.socialmedia.socialmedia.models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    //firebase auth
    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatlist> chatlistList;
    List<ModelUser> userList;
    DatabaseReference reference;
    FirebaseUser currentUser;
    AdapterChatlist adapterChatlist;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_chat_list, container, false);

        //init
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerView = view.findViewById(R.id.recyclerView);

        chatlistList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(currentUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatlistList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChatlist chatlist = ds.getValue(ModelChatlist.class);
                    chatlistList.add(chatlist);
                }
                loadChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void loadChats() {
        userList = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    String UserNeighborhood = ""+ ds.child("UserNeighborhood").getValue();
                    String bio = ""+ ds.child("bio").getValue();
                    String cover = ""+ ds.child("cover").getValue();
                    String email = ""+ ds.child("email").getValue();
                    String image = ""+ ds.child("image").getValue();
                    String isBusinessOwner = ""+ ds.child("isBusinessOwner").getValue();
                    String name = ""+ ds.child("name").getValue();
                    String onlineStatus = ""+ ds.child("onlineStatus").getValue();
                    String phone = ""+ ds.child("phone").getValue();
                    String typingTo = ""+ ds.child("typingTo").getValue();
                    String uid = ""+ ds.child("uid").getValue();

                    ModelUser user = new ModelUser();
                    user.setUserNeighborhood(UserNeighborhood);
                    user.setBio(bio);
                    user.setCover(cover);
                    user.setEmail(email);
                    user.setImage(image);
                    user.setisBusinessOwner(isBusinessOwner);
                    user.setName(name);
                    user.setOnlineStatus(onlineStatus);
                    user.setPhone(phone);
                    user.setTypingTo(typingTo);
                    user.setUid(uid);
                    user.setBlocked(false);

                    for (ModelChatlist chatlist: chatlistList){
                        if (user.getUid() != null && user.getUid().equals(chatlist.getId())){
                            userList.add(user);
                            break;
                        }
                    }
                    //adapter
                    adapterChatlist = new AdapterChatlist(getContext(), userList);
                    //setadapter
                    recyclerView.setAdapter(adapterChatlist);
                    //set last message
                    for (int i=0; i<userList.size(); i++){
                        lastMessage(userList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String theLastMessage = "default";
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if (chat==null){
                        continue;
                    }
                    String sender = chat.getSender();
                    String receiver = chat.getReceiver();
                    if (sender == null || receiver == null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentUser.getUid()) &&
                            chat.getSender().equals(userId) ||
                    chat.getReceiver().equals(userId) &&
                    chat.getSender().equals(currentUser.getUid())){
                        //instead of displaying url in message show "sent photo"
                        if (chat.getType().equals("image")){
                            theLastMessage = "إرسال صورة";
                        }
                        else {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }
                adapterChatlist.setLastMessageMap(userId, theLastMessage);
                adapterChatlist.notifyDataSetChanged();
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

        //hide addpost icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);

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
        }
        else if (id==R.id.action_settings){
            //go to settings activity
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        else if (id==R.id.action_create_group){
            //go to GroupCreateActivity activity
            startActivity(new Intent(getActivity(), GroupCreateActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}
