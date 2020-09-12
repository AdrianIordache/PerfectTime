package com.example.adi.perfecttime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private EditText userName;
    private EditText userFullName;
    private EditText countryName;
    private Button saveButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference;
    private StorageReference userProfileImageRef;

    String currentUserID;
    final static int galleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        profileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        userName = (EditText) findViewById(R.id.setup_user_name);
        userFullName = (EditText) findViewById(R.id.setup_full_name);
        countryName = (EditText) findViewById(R.id.setup_user_country);
        saveButton = (Button) findViewById(R.id.save_button);

        loadingBar = new ProgressDialog(this);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccountSetupInformation();
            }
        });


        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });

        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("Profile Image"))
                    {
                        String image = dataSnapshot.child("Profile Image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(profileImage);
                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Please select your profile image first", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we are updating your image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultIUri = result.getUri();

                final StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultIUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Toast.makeText(SetupActivity.this, "Image stored successfully to Storage!", Toast.LENGTH_SHORT).show();

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                final String downloadUrl = uri.toString();

                                        usersReference.child("Profile Image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                    startActivity(selfIntent);

                                                    Toast.makeText(SetupActivity.this, "Image stored successfully to Database", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });


                            }
                        });
                    }
                });

            }

            else
            {
                Toast.makeText(SetupActivity.this, "Error: Image can't be cropped!", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }

        }

    }



    private void saveAccountSetupInformation() {
        String user_name = userName.getText().toString();
        String user_full_name = userFullName.getText().toString();
        String country_name = countryName.getText().toString();

        if (TextUtils.isEmpty(user_name)) {
            Toast.makeText(this, " Please insert your Username!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(user_full_name)) {
            Toast.makeText(this, " Please insert your Full Name!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(country_name)) {
            Toast.makeText(this, " Please insert your Country!", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle("Saving Information...");
            loadingBar.setMessage("Please wait, your new account is being updated...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);


            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("Username", user_name);
            userMap.put("Full Name", user_full_name);
            userMap.put("Country", country_name);
            userMap.put("Status", "Hey, I am using PerfectTime, developed by Adrian Iordache.");
            userMap.put("Gender", "None");
            userMap.put("Birthday", "None");
            userMap.put("Relationship Status", "None");

            usersReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        sendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your Account is created Successfully", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }


    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);

        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(mainIntent);

        finish();
    }

}