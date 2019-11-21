package com.example.sabrine.discovertunisia.ui.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sabrine.discovertunisia.ChatActivity;
import com.example.sabrine.discovertunisia.Model.Friendship;
import com.example.sabrine.discovertunisia.ProfileActivity;
import com.example.sabrine.discovertunisia.R;
import com.example.sabrine.discovertunisia.firebase.FirebaseEntities;
import com.example.sabrine.discovertunisia.ui.activities.AllUsersActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private static final String CLASS_NAME = "FriendsFragment";

    private RecyclerView mFriendsRecyclerView;

    private DatabaseReference mUsersDatabase;

    private FloatingActionButton mAddFriendButton;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_friends_contact_list);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            return rootView;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference userFriendsDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseEntities.Friends.ENTITY_NAME).child(userId);
        userFriendsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseEntities.Users.ENTITY_NAME);
        mUsersDatabase.keepSynced(true);


        mFriendsRecyclerView.setHasFixedSize(true);
        mFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAddFriendButton = (FloatingActionButton) rootView.findViewById(R.id.fragment_friends_add_friend_button);
        mAddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AllUsersActivity.class);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();

        Query query = FirebaseDatabase.getInstance().getReference("/"+FirebaseEntities.Friends.ENTITY_NAME);
        FirebaseRecyclerOptions<Friendship> options =
                new FirebaseRecyclerOptions.Builder<Friendship>()
                        .setQuery(query, Friendship.class)
                        .setLifecycleOwner(this)
                        .build();
        FirebaseRecyclerAdapter<Friendship, FriendViewHolder> friendsRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friendship, FriendViewHolder>(options) {

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.item_view_user, null);
                return new FriendViewHolder(view);
            }
            @Override
            protected void onBindViewHolder(@NonNull FriendViewHolder holder, int position, @NonNull Friendship friend) {
                populateViewHolder(holder, friend, position);
            }

            private void populateViewHolder(final FriendViewHolder friendViewHolder, Friendship friend, int position) {

                friendViewHolder.setDate(friend.getDate());

                final String friendKey = getRef(position).getKey();

                mUsersDatabase.child(friendKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child(FirebaseEntities.Users.FIELD_NAME).getValue(String.class);
                        friendViewHolder.setName(userName);

                        String userImageThumb =dataSnapshot.child(FirebaseEntities.Users.FIELD_THUMB).getValue(String.class);
                        friendViewHolder.setUserImage(getContext(), userImageThumb);

                        if(dataSnapshot.hasChild(FirebaseEntities.Users.FIELD_ONLINE)) {

                            String userOnline = String.valueOf(dataSnapshot.child(FirebaseEntities.Users.FIELD_ONLINE).getValue());
                            friendViewHolder.setUserOnline(userOnline);

                        }

                        final String finalUserName = userName;
                        friendViewHolder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String options[] = getResources().getStringArray(R.array.friend_menu_options);

                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                                builder.setTitle(R.string.friend_menu_title);
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        //Click Event for each item.
                                        if(i == 0){

                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra(ProfileActivity.USER_ID, friendKey);
                                            startActivity(profileIntent);
                                        }

                                        if(i == 1){

                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra(ChatActivity.USER_ID, friendKey);
                                            chatIntent.putExtra(ChatActivity.USER_NAME, finalUserName);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });

                                builder.show();

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(CLASS_NAME, databaseError.getMessage());
                    }
                });

            }
        };

        mFriendsRecyclerView.setAdapter(friendsRecyclerViewAdapter);


    }


    public static class FriendViewHolder extends RecyclerView.ViewHolder {

        private ImageView mUserOnlineView;
        private CircleImageView mUserImageView;
        private TextView mUserNameView;
        private TextView mUserStatusView;
        View mView;

        FriendViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mUserStatusView = (TextView) mView.findViewById(R.id.item_view_user_status);
            mUserNameView = (TextView) mView.findViewById(R.id.item_view_user_name);
            mUserImageView = (CircleImageView) mView.findViewById(R.id.item_view_user_image);
            mUserOnlineView = (ImageView) mView.findViewById(R.id.item_view_user_online_icon);

        }

        public void setDate(String date){
            mUserStatusView.setText(date);
        }

        public void setName(String name){
            mUserNameView.setText(name);
        }

        public void setUserImage(Context context, String imagePath){
            Picasso.with(context).load(imagePath).placeholder(R.drawable.default_avatar).into(mUserImageView);
        }

        public void setUserOnline(String status) {



            if(FirebaseEntities.Users.ONLINE_VALUE_AVAILABLE.equals(status)){
                mUserOnlineView.setVisibility(View.VISIBLE);
            } else {
                mUserOnlineView.setVisibility(View.INVISIBLE);
            }

        }

        public void setOnClickListener(View.OnClickListener listener){
            mView.setOnClickListener(listener);
        }


    }


}
