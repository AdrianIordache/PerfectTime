package com.example.adi.perfecttime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private EditText userName, userFullName, userStatus, userCountry, userGender, userDateOfBirth, userRelation;
    private CircleImageView userProfileImage;
    private Button updateSettingsButton;

    private DatabaseReference settingsReference;
    private DatabaseReference postReference;
    private StorageReference userProfileImageRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private String currentUserID;
    final static int galleryPick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        postReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        settingsReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");


        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userName = (EditText) findViewById(R.id.settings_username);
        userFullName = (EditText) findViewById(R.id.settings_full_name);
        userRelation = (EditText) findViewById(R.id.settings_relationship_status);
        userGender = (EditText) findViewById(R.id.settings_gender);
        userStatus = (EditText) findViewById(R.id.settings_status);
        userDateOfBirth = (EditText) findViewById(R.id.settings_date_of_birth);
        userCountry = (EditText) findViewById(R.id.settings_country);
        loadingBar = new ProgressDialog(this);

        userProfileImage = (CircleImageView) findViewById(R.id.settings_profile_image);
        updateSettingsButton = (Button) findViewById(R.id.update_settings_button);

        settingsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String personProfileImage = dataSnapshot.child("Profile Image").getValue().toString();
                    String personUsername = dataSnapshot.child("Username").getValue().toString();
                    String personGender = dataSnapshot.child("Gender").getValue().toString();
                    String personRelationshipStatus = dataSnapshot.child("Relationship Status").getValue().toString();
                    String personStatus = dataSnapshot.child("Status").getValue().toString();
                    String personFullName = dataSnapshot.child("Full Name").getValue().toString();
                    String personCountry = dataSnapshot.child("Country").getValue().toString();
                    String personBirthday = dataSnapshot.child("Birthday").getValue().toString();

                    userDateOfBirth.setText(personBirthday);
                    userGender.setText(personGender);
                    userStatus.setText(personStatus);
                    userFullName.setText(personFullName);
                    userName.setText(personUsername);
                    userCountry.setText(personCountry);
                    userRelation.setText(personRelationshipStatus);

                    Picasso.get().load(personProfileImage).into(userProfileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                validateAccountInfo();
            }
        });

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });
    }

    private void validateAccountInfo()
    {
        String user_name = userName.getText().toString();
        String user_full_name = userFullName.getText().toString();
        String status = userStatus.getText().toString();
        String country = userCountry.getText().toString();
        String relation = userRelation.getText().toString();
        String date_of_birth = userDateOfBirth.getText().toString();
        String gender = userGender.getText().toString();

        if(TextUtils.isEmpty(user_name))
        {
            Toast.makeText(this, "Please write your username", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(user_full_name))
        {
            Toast.makeText(this, "Please write your full name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender))
        {
            Toast.makeText(this, "Please write your gender", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(date_of_birth))
        {
            Toast.makeText(this, "Please write your Birthday", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "Please write your status", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please write your country", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(relation))
        {
            Toast.makeText(this, "Please write your relation", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Updating Information");
            loadingBar.setMessage("Please wait, while we are updating your information...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            updateAccountInfo(user_name, user_full_name, status, country, relation, date_of_birth, gender);
        }
    }

    private void updateAccountInfo(String user_name, final String user_full_name, String status, String country, String relation, String date_of_birth, String gender)
    {
        HashMap userMap = new HashMap();
        userMap.put("Birthday", date_of_birth);
        userMap.put("Country", country);
        userMap.put("Full Name", user_full_name);
        userMap.put("Gender", gender);
        userMap.put("Relationship Status", relation);
        userMap.put("Status", status);
        userMap.put("Username", user_name);

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                {
                    if(dataSnapshot1.child("UserID").getValue().toString().equals(currentUserID))
                    {
                        postReference.child(dataSnapshot1.getKey()).child("FullName").setValue(user_full_name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        settingsReference.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task)
            {
                if(task.isSuccessful())
                {
                    sendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Account Information Updated Successfully...", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else
                {
                    Toast.makeText(SettingsActivity.this, "Error Occurred while updating account information...", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
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
                loadingBar.setTitle("Profile Image Update");
                loadingBar.setMessage("Please wait, while we are updating your image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultIUri = result.getUri();

                final StorageReference filePath = userProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultIUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Toast.makeText(SettingsActivity.this, "Image stored successfully to Storage!", Toast.LENGTH_SHORT).show();

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri)
                            {
                                final String downloadUrl = uri.toString();

                                postReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                    {
                                        for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                                        {
                                            if(dataSnapshot1.child("UserID").getValue().toString().equals(currentUserID))
                                            {
                                                postReference.child(dataSnapshot1.getKey()).child("ProfileImage").setValue(downloadUrl);
                                            }
                                        }
                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                                settingsReference.child("Profile Image").setValue(downloadUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                    startActivity(selfIntent);

                                                    Toast.makeText(SettingsActivity.this, "Image stored successfully to Database", Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                } else {
                                                    String message = task.getException().getMessage();
                                                    Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SettingsActivity.this, "Error: Image can't be cropped!", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }

        }

    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
