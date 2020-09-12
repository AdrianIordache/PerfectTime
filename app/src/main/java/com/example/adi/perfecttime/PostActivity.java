package com.example.adi.perfecttime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;
    private ImageView selectPostImage;
    private Button updatePostButton;
    private EditText postDescription;

    private static final int galleryPick = 1;
    private Uri imageUri;
    private String description;

    private FirebaseAuth mAuth;
    private StorageReference postStorageReference;
    private DatabaseReference usersReferences, postReferences;
    private String saveCurrentDate, saveCurrentTime, downloadUrl, currentUserId;
    private String postRandomName;

    private long postCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();

        postStorageReference = FirebaseStorage.getInstance().getReference();
        usersReferences = FirebaseDatabase.getInstance().getReference().child("Users");
        postReferences  = FirebaseDatabase.getInstance().getReference().child("Posts");

        loadingBar = new ProgressDialog(this);



        selectPostImage = (ImageView) findViewById(R.id.select_post_image);
        updatePostButton = (Button) findViewById(R.id.update_post_button);
        postDescription = (EditText) findViewById(R.id.post_description);

        mToolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                OpenGallery();
            }
        });

        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                validatePostInfo();
            }
        });

    }

    private void validatePostInfo()
    {
        description = postDescription.getText().toString();

        if(imageUri == null)
        {
            Toast.makeText(this, "Please select an image first...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "Please describe your picture..", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            sendImageToStorage();
        }
    }

    private void sendImageToStorage()
    {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd:MMMM:yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        final StorageReference filePath = postStorageReference.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");


        filePath.putFile(imageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                Toast.makeText(PostActivity.this, "Image uploaded successfully to Storage", Toast.LENGTH_SHORT).show();

                filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri)
                    {
                        downloadUrl = uri.toString();
                        savingPostInformationToDatabase();
                    }
                });
            }
        });
    }

    private void savingPostInformationToDatabase()
    {
        postReferences.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    postCounter = dataSnapshot.getChildrenCount();
                }
                else
                {
                    postCounter = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

       usersReferences.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
           {
                if(dataSnapshot.exists())
                {
                    String fullName = dataSnapshot.child("Full Name").getValue().toString();
                    String userProfileImage = dataSnapshot.child("Profile Image").getValue().toString();

                    HashMap<String, Object> postMap = new HashMap();
                    postMap.put("UserID", currentUserId);
                    postMap.put("Date", saveCurrentDate);
                    postMap.put("Time", saveCurrentTime);
                    postMap.put("PostDescription", description);
                    postMap.put("Image", downloadUrl);
                    postMap.put("ProfileImage", userProfileImage);
                    postMap.put("FullName", fullName);
                    postMap.put("Counter", postCounter);

                    postReferences.child(currentUserId + postRandomName + postCounter).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task)
                                {
                                    if (task.isSuccessful()) {
                                        sendUserToMainActivity();
                                        Toast.makeText(PostActivity.this, "New Post was successfully updated to Database", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        Toast.makeText(PostActivity.this, "Error occurred while updating your post", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError)
           {

           }
       });
    }

    private void sendUserToMainActivityKey(String postRandomName)
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.putExtra("postRandomName", postRandomName);
        startActivity(mainIntent);
    }

    private void OpenGallery()
    {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, galleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == galleryPick && resultCode==RESULT_OK && data != null)
        {
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            sendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }
}
