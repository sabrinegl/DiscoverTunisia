package com.example.sabrine.discovertunisia;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GalleryActivity extends AppCompatActivity {

    private static final int  REQUEST_CAMERA =1, SELECT_FILE=0;

    private RecyclerView mRecyclerView;
    private List<GalleryImage> data = new ArrayList<GalleryImage>();
    private GalleryAdapter mAdapter;
    private StorageReference mImageStorage;
    private DatabaseReference mDataReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        mImageStorage = FirebaseStorage.getInstance().getReference();
        mDataReference = FirebaseDatabase.getInstance().getReference("gallery");


        Query query = mDataReference.limitToLast(15);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                GalleryImage galleryImage = dataSnapshot.getValue(GalleryImage.class);
                data.add(galleryImage);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                GalleryImage galleryImage = dataSnapshot.getValue(GalleryImage.class);
                data.add(galleryImage);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true);


        mAdapter = new GalleryAdapter(this, data);
        mRecyclerView.setAdapter(mAdapter);
/*
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {

                    @Override
                    public void onItemClick(View view, int position) {


                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putParcelableArrayListExtra("data", data);
                        intent.putExtra("pos", position);
                        startActivity(intent);


                    }
                }));
                */


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

    }

    private void selectImage() {
        final CharSequence[] items = {"Camera", "Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(GalleryActivity.this);
        builder.setTitle("add image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Camera")) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);

                } else if (items[i].equals("Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);

                } else if (items[i].equals("Cancel")) {

                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            final ProgressDialog progressDialog = new ProgressDialog(GalleryActivity.this);
            progressDialog.setTitle("Uploading Image...");
            progressDialog.setMessage("Please wait while we upload and process the image.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            Uri resultUri = data.getData();
            final StorageReference filepath = mImageStorage.child("gallery").child(UUID.randomUUID() + ".jpg");

            UploadTask uploadTask = filepath.putFile(resultUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();
                        final String path = downloadUri.toString();
                        writeNewImageInfoToDB("test", path);
                        progressDialog.dismiss();



                    } else {
                        Toast.makeText(GalleryActivity.this, "Error in uploading.", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                }
            });

        }
    }

    private void writeNewImageInfoToDB(String name, String url) {
        GalleryImage info = new GalleryImage(name, url);

        String key = mDataReference.push().getKey();
        mDataReference.child(key).setValue(info);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class ImageModel {

        String name, url;

        public ImageModel() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }


    public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyItemHolder> {

        Context context;
        List<GalleryImage> data = new ArrayList<>();

        public GalleryAdapter(Context context, List<GalleryImage> data) {
            this.context = context;
            this.data = data;
        }


        @Override
        public MyItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            MyItemHolder viewHolder;
            View v;
            v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.item_image, parent, false);
            viewHolder = new MyItemHolder(v);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(MyItemHolder holder, int position) {

            Picasso.with(GalleryActivity.this).load(data.get(position).url).placeholder(R.drawable.default_avatar).into(holder.mImg);

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class MyItemHolder extends RecyclerView.ViewHolder {
            ImageView mImg;


            public MyItemHolder(View itemView) {
                super(itemView);

                mImg = (ImageView) itemView.findViewById(R.id.item_img);
            }

        }


    }

}

