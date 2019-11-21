package com.example.sabrine.discovertunisia.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sabrine.discovertunisia.ChatActivity;
import com.example.sabrine.discovertunisia.ChatBotActivity;
import com.example.sabrine.discovertunisia.Model.Conversation;
import com.example.sabrine.discovertunisia.R;
import com.example.sabrine.discovertunisia.firebase.FirebaseEntities;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
public class ChatsFragment extends Fragment {

    private RecyclerView mConversationList;

    private DatabaseReference mConversationDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrentUserId;

    private FloatingActionButton mChatBotButton;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chats, container, false);

        mConversationList = rootView.findViewById(R.id.fragment_chats_conversation_list);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null) {
            mCurrentUserId = mAuth.getCurrentUser().getUid();
        }

        mConversationDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseEntities.Chat.ENTITY_NAME).child(mCurrentUserId);

        mConversationDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseEntities.Users.ENTITY_NAME);
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child(FirebaseEntities.Messages.ENTITY_NAME).child(mCurrentUserId);
        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConversationList.setHasFixedSize(true);
        mConversationList.setLayoutManager(linearLayoutManager);

        mChatBotButton = rootView.findViewById(R.id.fragment_chats_chat_bot_floating_button);
        mChatBotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ChatBotActivity.class);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConversationDatabase.orderByChild(FirebaseEntities.Chat.FIELD_TIMESTAMP);
        FirebaseRecyclerOptions<Conversation> options =
                new FirebaseRecyclerOptions.Builder<Conversation>()
                        .setQuery(conversationQuery, Conversation.class)
                        .setLifecycleOwner(this)
                        .build();
        FirebaseRecyclerAdapter<Conversation, ConversationViewHolder> firebaseConversationAdapter = new FirebaseRecyclerAdapter<Conversation, ConversationViewHolder>(options) {
            @NonNull
            @Override
            public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.item_view_conversation, null);
                return new ConversationViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ConversationViewHolder holder, int position, @NonNull Conversation model) {
                populateViewHolder(holder, model, position);
            }

            private void populateViewHolder(final ConversationViewHolder viewHolder, final Conversation conversation, int position) {



                final String senderId = getRef(position).getKey();

                Query lastMessageQuery = mMessageDatabase.child(senderId).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String data = dataSnapshot.child(FirebaseEntities.Messages.ENTITY_NAME).getValue().toString();
                        viewHolder.setMessage(data, conversation.isSeen());

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                mUsersDatabase.child(senderId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child(FirebaseEntities.Users.FIELD_NAME).getValue().toString();
                        String userThumb = dataSnapshot.child(FirebaseEntities.Users.FIELD_THUMB).getValue().toString();

                        if(dataSnapshot.hasChild(FirebaseEntities.Users.FIELD_ONLINE)) {

                            String userOnline = dataSnapshot.child(FirebaseEntities.Users.FIELD_ONLINE).getValue().toString();
                            viewHolder.setOnlineStatus(userOnline);

                        }

                        viewHolder.setConversationTitle(userName);
                        viewHolder.setAvatar(userThumb, getContext());

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {


                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra(ChatActivity.USER_ID, senderId);
                                chatIntent.putExtra(ChatActivity.USER_NAME, userName);
                                startActivity(chatIntent);

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        mConversationList.setAdapter(firebaseConversationAdapter);

    }

    public static class ConversationViewHolder extends RecyclerView.ViewHolder {

        View mView;

        ConversationViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen){

            TextView conversationSummary = mView.findViewById(R.id.item_view_conversation_summary);
            conversationSummary.setText(message);

            if(!isSeen){
                conversationSummary.setTypeface(conversationSummary.getTypeface(), Typeface.BOLD);
            } else {
                conversationSummary.setTypeface(conversationSummary.getTypeface(), Typeface.NORMAL);
            }

        }

        void setConversationTitle(String title){

            TextView conversationNameView = mView.findViewById(R.id.item_view_conversation_name);
            conversationNameView.setText(title);

        }

        void setAvatar(String thumb_image, Context ctx){

            CircleImageView avatarImageView = mView.findViewById(R.id.item_view_conversation_avatar);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(avatarImageView);

        }

        void setOnlineStatus(String onlineStatus) {

            ImageView onlineStatusView = mView.findViewById(R.id.item_view_conversation_status);

            if(onlineStatus.equals(FirebaseEntities.Users.ONLINE_VALUE_AVAILABLE)){
                onlineStatusView.setVisibility(View.VISIBLE);
            } else {
                onlineStatusView.setVisibility(View.INVISIBLE);
            }

        }

    }

}
