package com.example.adi.perfecttime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView personName, personFullName, personStatus, personCountry, personGender, personDateOfBirth, personRelation;
    private CircleImageView personProfileImage;

    private DatabaseReference profileReference, friendsReference, myPostsReference;
    private FirebaseAuth mAuth;

    private String currentUserID;
    private int countFriends = 0, countPosts = 0;

    private Button myPosts, myFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        profileReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        myPostsReference = FirebaseDatabase.getInstance().getReference().child("Posts");

        personName = (TextView) findViewById(R.id.person_profile_user_name);
        personFullName = (TextView) findViewById(R.id.person_profile_name);
        personRelation = (TextView) findViewById(R.id.person_profile_relationship_status);
        personGender = (TextView) findViewById(R.id.person_profile_gender);
        personStatus = (TextView) findViewById(R.id.person_profile_status);
        personDateOfBirth = (TextView) findViewById(R.id.person_profile_date_of_birth);
        personCountry = (TextView) findViewById(R.id.person_profile_country);
        personProfileImage = (CircleImageView) findViewById(R.id.person_profile_image);
        myFriends = (Button) findViewById(R.id.my_all_friends_button);
        myPosts    = (Button) findViewById(R.id.my_posts_button);

        myFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendUserToFriendsActivity();
            }
        });


        myPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendUserToMyPostsActivity();
            }
        });

        friendsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    countFriends = (int) dataSnapshot.getChildrenCount();
                    myFriends.setText(Integer.toString(countFriends) + " Friends");
                }
                else
                {
                    myFriends.setText("O Friends");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myPostsReference.orderByChild("UserID").startAt(currentUserID).endAt(currentUserID + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            countPosts = (int) dataSnapshot.getChildrenCount();
                            myPosts.setText(Integer.toString(countPosts) + " Posts");
                        }
                        else
                        {
                            myPosts.setText("0 Posts");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        profileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    String userProfileImage = dataSnapshot.child("Profile Image").getValue().toString();
                    String userName = dataSnapshot.child("Username").getValue().toString();
                    String userGender = dataSnapshot.child("Gender").getValue().toString();
                    String userRelationshipStatus = dataSnapshot.child("Relationship Status").getValue().toString();
                    String userStatus = dataSnapshot.child("Status").getValue().toString();
                    String userFullName = dataSnapshot.child("Full Name").getValue().toString();
                    String userCountry = dataSnapshot.child("Country").getValue().toString();
                    String userBirthday = dataSnapshot.child("Birthday").getValue().toString();

                    personFullName.setText(userFullName);
                    personGender.setText("Gender: " + userGender);
                    personCountry.setText("Country: " + userCountry);
                    personRelation.setText("Relationship: " + userRelationshipStatus);
                    personDateOfBirth.setText("Birthday: " + userBirthday);
                    personName.setText("@" + userName);
                    personStatus.setText(userStatus);

                    Picasso.get().load(userProfileImage).into(personProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendUserToFriendsActivity()
    {
        Intent friendsIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }

    private void sendUserToMyPostsActivity()
    {
        Intent myPostsIntent = new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(myPostsIntent);
    }
}
