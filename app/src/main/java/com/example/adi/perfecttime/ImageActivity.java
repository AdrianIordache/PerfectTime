package com.example.adi.perfecttime;

import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ImageActivity extends AppCompatActivity {

    ImageView image;
    Toolbar mToolbar;

    DatabaseReference usersReference;
    FirebaseAuth mAuth;

    private String curreentUserID, visitedUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        image = (ImageView) findViewById(R.id.image_view_profile);

        mToolbar = (Toolbar) findViewById(R.id.image_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile Image");

        mAuth = FirebaseAuth.getInstance();

        curreentUserID = mAuth.getCurrentUser().getUid();
        visitedUserID = getIntent().getExtras().get("VisitID").toString();

        usersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(visitedUserID);


        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String profileImage = dataSnapshot.child("Profile Image").getValue().toString();
                    Picasso.get().load(profileImage).into(image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
