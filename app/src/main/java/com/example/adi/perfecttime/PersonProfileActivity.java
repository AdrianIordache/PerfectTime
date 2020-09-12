package com.example.adi.perfecttime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView personName, personFullName, personStatus, personCountry, personGender, personDateOfBirth, personRelation;
    private CircleImageView personProfileImage;
    private Button sendFriendRequestButton, cancelFrindRequestButton;


    private DatabaseReference friendRequestReference, userReference, friendsReference;
    private FirebaseAuth mAuth;
    private String senderUserID, receiverUserID, currentState, saveCurrentDate;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth = FirebaseAuth.getInstance();

        senderUserID = mAuth.getCurrentUser().getUid();
        receiverUserID = getIntent().getExtras().get("VisitID").toString();

        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRequestReference = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends");

        InitializationOfVariables();

        userReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
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

                    maintainButtons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        cancelFrindRequestButton.setVisibility(View.INVISIBLE);
        cancelFrindRequestButton.setEnabled(false);

        if(!senderUserID.equals(receiverUserID))
        {
            sendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    sendFriendRequestButton.setEnabled(false);

                    if(currentState.equals("Not Friends"))
                    {
                        sendFriendRequest();
                        refreshMain();
                    }
                    if(currentState.equals("Request Sent"))
                    {
                        cancelFriendRequest();
                        refreshMain();
                    }
                    if(currentState.equals("Request Received"))
                    {
                        acceptFriendRequest();
                        refreshMain();
                    }
                    if(currentState.equals("Friends"))
                    {
                        removeFriend();
                        refreshMain();

                    }
                }
            });
        }
        else
        {
            cancelFrindRequestButton.setVisibility(View.INVISIBLE);
            sendFriendRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void refreshMain()
    {
        Intent refresh = new Intent(PersonProfileActivity.this, MainActivity.class);
        startActivity(refresh);
    }


    private void removeFriend()
    {
        friendsReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    friendsReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                sendFriendRequestButton.setEnabled(true);
                                currentState = "Not Friends";
                                sendFriendRequestButton.setText("Send Friend Request");

                                cancelFrindRequestButton.setVisibility(View.INVISIBLE);
                                cancelFrindRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void acceptFriendRequest()
    {
        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd:MMMM:yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());


        userReference.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                HashMap<String, Object> senderDetailsMap = new HashMap<>();
                String profileImage = dataSnapshot.child("Profile Image").getValue().toString();
                String fullName = dataSnapshot.child("Full Name").getValue().toString();
                String status = dataSnapshot.child("Status").getValue().toString();

                senderDetailsMap.put("Profile Image", profileImage);
                senderDetailsMap.put("Full Name", fullName);
                senderDetailsMap.put("Status", status);
                senderDetailsMap.put("UserID", senderUserID);
                senderDetailsMap.put("Date", saveCurrentDate);

                friendsReference.child(receiverUserID).child(senderUserID).setValue(senderDetailsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendRequestReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        sendFriendRequestButton.setEnabled(true);
                                        currentState = "Friends";
                                        sendFriendRequestButton.setText("Remove Friend");

                                        cancelFrindRequestButton.setVisibility(View.INVISIBLE);
                                        cancelFrindRequestButton.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        userReference.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                HashMap<String, Object> receiverDetailsMap = new HashMap<>();
                String profileImage = dataSnapshot.child("Profile Image").getValue().toString();
                String fullName = dataSnapshot.child("Full Name").getValue().toString();
                String status = dataSnapshot.child("Status").getValue().toString();

                receiverDetailsMap.put("Profile Image", profileImage);
                receiverDetailsMap.put("Full Name", fullName);
                receiverDetailsMap.put("Status", status);
                receiverDetailsMap.put("UserID", receiverUserID);
                receiverDetailsMap.put("Date", saveCurrentDate);

                friendsReference.child(senderUserID).child(receiverUserID).setValue(receiverDetailsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            friendRequestReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        sendFriendRequestButton.setEnabled(true);
                                        currentState = "Friends";
                                        sendFriendRequestButton.setText("Remove Friend");

                                        cancelFrindRequestButton.setVisibility(View.INVISIBLE);
                                        cancelFrindRequestButton.setEnabled(false);
                                    }
                                }
                            });
                        }
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    private void cancelFriendRequest()
    {
        friendRequestReference.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    friendRequestReference.child(receiverUserID).child(senderUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                sendFriendRequestButton.setEnabled(true);
                                currentState = "Not Friends";
                                sendFriendRequestButton.setText("Send Friend Request");

                                cancelFrindRequestButton.setVisibility(View.INVISIBLE);
                                cancelFrindRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });
    }

    private void maintainButtons()
    {
        friendRequestReference.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.hasChild(receiverUserID))
                {
                    String requestType = dataSnapshot.child(receiverUserID).child("Request Type").getValue().toString();

                    if(requestType.equals("Sent"))
                    {
                        currentState = "Request Sent";
                        sendFriendRequestButton.setText("Cancel Friend Request");

                        cancelFrindRequestButton.setVisibility(View.INVISIBLE);
                        cancelFrindRequestButton.setEnabled(false);
                    }
                    else if(requestType.equals("Received"))
                    {
                        currentState = "Request Received";
                        sendFriendRequestButton.setText("Accept Friend Request");

                        cancelFrindRequestButton.setVisibility(View.VISIBLE);
                        cancelFrindRequestButton.setEnabled(true);

                        cancelFrindRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                cancelFriendRequest();
                            }
                        });

                    }
                }
                else
                {
                    friendsReference.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if(dataSnapshot.hasChild(receiverUserID))
                            {
                                currentState = "Friends";
                                sendFriendRequestButton.setText("Remove Friend");

                                cancelFrindRequestButton.setVisibility(View.INVISIBLE);
                                cancelFrindRequestButton.setEnabled(false);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendFriendRequest()
    {
        friendRequestReference.child(senderUserID).child(receiverUserID).child("Request Type").setValue("Sent").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    friendRequestReference.child(receiverUserID).child(senderUserID).child("Request Type").setValue("Received").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if(task.isSuccessful())
                            {
                                sendFriendRequestButton.setEnabled(true);
                                currentState = "Request Sent";
                                sendFriendRequestButton.setText("Cancel Friend Request");

                                cancelFrindRequestButton.setVisibility(View.INVISIBLE);
                                cancelFrindRequestButton.setEnabled(false);
                            }
                        }
                    });
                }
            }
        });

    }

    private void InitializationOfVariables()
    {
        personName = (TextView) findViewById(R.id.person_profile_user_name);
        personFullName = (TextView) findViewById(R.id.person_profile_name);
        personRelation = (TextView) findViewById(R.id.person_profile_relationship_status);
        personGender = (TextView) findViewById(R.id.person_profile_gender);
        personStatus = (TextView) findViewById(R.id.person_profile_status);
        personDateOfBirth = (TextView) findViewById(R.id.person_profile_date_of_birth);
        personCountry = (TextView) findViewById(R.id.person_profile_country);
        personProfileImage = (CircleImageView) findViewById(R.id.person_profile_image);
        sendFriendRequestButton = (Button) findViewById(R.id.person_send_friend_request_button);
        cancelFrindRequestButton = (Button) findViewById(R.id.person_cancel_friend_request_button);

        currentState = "Not Friends";


    }
}
