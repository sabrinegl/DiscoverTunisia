package com.example.sabrine.discovertunisia;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{


    private static final int MESSAGE_SENT = 1;
    private static final int MESSAGE_RECEIVED = 2;
    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;

    MessageAdapter(List<Messages> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @Override
    public int getItemViewType(int position) {
        Messages messages = mMessageList.get(position);
        if(Messages.BOX_TYPE_SENT == messages.getBoxType()) {
            return MESSAGE_SENT;
        }else{
            return MESSAGE_RECEIVED;
        }
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType == MESSAGE_SENT){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_message_sent, parent, false);
            return new MessageSentViewHolder(view);
        }else if(viewType == MESSAGE_RECEIVED){
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_message_received, parent, false);
            return new MessageReceivedViewHolder(view);
        }
        return null;
    }

    abstract class MessageViewHolder extends RecyclerView.ViewHolder{
        MessageViewHolder(View view) {
            super(view);
        }

        public abstract void setMessage(Messages message);

        public abstract void updateNameAndPicture(String name, String image);
    }

    class MessageReceivedViewHolder extends MessageViewHolder {

        private TextView date;
        private TextView messageText;
        private CircleImageView profileImage;
        private TextView displayName;
        private ImageView messageImage;

        MessageReceivedViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.view_message_received_text_body);
            profileImage = (CircleImageView) view.findViewById(R.id.view_message_received_profile_picture);
            displayName = (TextView) view.findViewById(R.id.view_message_received_name);
            date = (TextView) view.findViewById(R.id.view_message_received_date);
            messageImage = (ImageView) view.findViewById(R.id.view_message_received_image_layout);

        }

        @Override
        public void setMessage(Messages message) {
            String message_type = message.getType();
            if(message_type.equals("text")) {

                messageText.setText(message.getMessage());
                messageImage.setVisibility(View.INVISIBLE);


            } else {

                messageText.setVisibility(View.INVISIBLE);
                Picasso.with(profileImage.getContext()).load(message.getMessage())
                        .placeholder(R.drawable.default_avatar).into(messageImage);

            }

            long time = message.getTime();
            date.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(time)));

        }

        @Override
        public void updateNameAndPicture(String name, String image) {
            displayName.setText(name);

            Picasso.with(profileImage.getContext()).load(image)
                    .placeholder(R.drawable.default_avatar).into(profileImage);

        }
    }

    class MessageSentViewHolder extends MessageViewHolder {

        private TextView date;
        private TextView messageText;
        private TextView displayName;
        private ImageView messageImage;

        MessageSentViewHolder(View view) {
            super(view);
            messageText = (TextView) view.findViewById(R.id.view_message_sent_text_body);
            displayName = (TextView) view.findViewById(R.id.view_message_sent_name);
            date = (TextView) view.findViewById(R.id.view_message_sent_date);
            messageImage = (ImageView) view.findViewById(R.id.view_message_sent_image_layout);

        }

        @Override
        public void setMessage(Messages message) {
            String message_type = message.getType();
            if(message_type.equals("text")) {

                messageText.setText(message.getMessage());
                messageImage.setVisibility(View.INVISIBLE);


            } else {

                messageText.setVisibility(View.INVISIBLE);
//                Picasso.with(profileImage.getContext()).load(message.getMessage())
//                        .placeholder(R.drawable.default_avatar).into(messageImage);

            }

            long time = message.getTime();
            date.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date(time)));

        }

        @Override
        public void updateNameAndPicture(String name, String image) {
//            displayName.setText(name);

//            Picasso.with(profileImage.getContext()).load(image)
//                    .placeholder(R.drawable.default_avatar).into(profileImage);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {

        Messages c = mMessageList.get(i);

        String from_user = c.getFrom();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                viewHolder.updateNameAndPicture(name, image);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        viewHolder.setMessage(c);

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }






}
