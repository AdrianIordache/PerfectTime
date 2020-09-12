package com.example.adi.perfecttime;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;
    private Toolbar mToolbar;

    private DatabaseReference friendsReference, userReference;
    private FirebaseAuth mAuth;
    private String currentUserID;

    RecyclerView.Adapter adapter;
    List<MyFriends> list = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserID);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList = (RecyclerView) findViewById(R.id.my_friends_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        mToolbar = (Toolbar) findViewById(R.id.my_friends_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("My Friends List");


        displayMyFriendsList();
    }


    public void updateUserState(String state)
    {
        String saveCurrentDate, saveCurrentTime;

        Calendar callForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(callForDate.getTime());

        Calendar callForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(callForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("Time", saveCurrentTime);
        currentStateMap.put("Date", saveCurrentDate);
        currentStateMap.put("Type", state);

        userReference.child(currentUserID).child("State").updateChildren(currentStateMap);


    }

    @Override
    protected void onStart()
    {
        super.onStart();

        updateUserState("Online");
    }


    @Override
    protected void onStop()
    {
        super.onStop();

        updateUserState("Offline");
    }

    private void displayMyFriendsList()
    {
        friendsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        final String userId = dataSnapshot1.getKey();
                        final String Date = dataSnapshot1.child("Date").getValue().toString();
                        final String profileImage = dataSnapshot1.child("Profile Image").getValue().toString();
                        final String fullName = dataSnapshot1.child("Full Name").getValue().toString();
                        final String status = dataSnapshot1.child("Status").getValue().toString();
                        String state = "Unknown";

                        if(dataSnapshot1.hasChild("State"))
                        {
                            state = dataSnapshot1.child("Status").child("Type").getValue().toString();
                        }

                        MyFriends myFriend = new MyFriends(profileImage, fullName, status, userId, Date, state);
                        list.add(myFriend);

                    }
                    adapter = new RecycleViewAdapterFriends(FriendsActivity.this, list);
                    myFriendList.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class RecycleViewAdapterFriends extends RecyclerView.Adapter<RecycleViewAdapterFriends.ViewHolder> {

        Context context;
        List<MyFriends> Array;

        public RecycleViewAdapterFriends(Context context, List<MyFriends> TempList) {
            this.Array = TempList;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_my_friends_layout, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final MyFriends model = Array.get(position);


            holder.setFullName(model.getFullName());
            holder.setProfileImage(model.getProfileImage());
            holder.setStatus(model.getStatus());
            holder.setDate(model.getDate());


            if((model.getState()).equals("Online"))
            {
                holder.onlineView.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.onlineView.setVisibility(View.INVISIBLE);
            }

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {

                    CharSequence options[] = new CharSequence[]
                            {
                                    model.getFullName() + "'s Profile",
                                    "Send Message"
                            };

                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                    builder.setTitle("Select Option");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            if(i == 0)
                            {
                                String visitID = model.getUserID().toString();
                                Intent profileIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                profileIntent.putExtra("VisitID", visitID);
                                startActivity(profileIntent);
                            }
                            if(i == 1)
                            {
                                String visitID = model.getUserID().toString();
                                String username = model.getFullName().toString();
                                Intent messageIntent = new Intent(FriendsActivity.this, MessageActivity.class);
                                messageIntent.putExtra("VisitID", visitID);
                                messageIntent.putExtra("Username", username);
                                startActivity(messageIntent);
                            }
                        }
                    });
                    builder.show();
                }
            });
        }

        @Override
        public int getItemCount() {

            return Array.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {


            View mView;
            ImageView onlineView;

            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;

                onlineView = (ImageView) itemView.findViewById(R.id.my_friends_online_icon);

            }

            public void setFullName(String FullName) {
                TextView username = (TextView) mView.findViewById(R.id.my_friends_full_name);
                username.setText(FullName);
            }

            public void setProfileImage(String ProfileImage) {
                CircleImageView image = (CircleImageView) mView.findViewById(R.id.my_old_friends_image);
                Picasso.get().load(ProfileImage).into(image);
            }

            public void setStatus(String Status) {
                TextView userStatus = (TextView) mView.findViewById(R.id.my_friends_status);
                userStatus.setText(Status);
            }

            public void setDate(String date) {
                TextView userDate = (TextView) mView.findViewById(R.id.my_friends_date);
                userDate.setText("Friends Since " + date);
            }

        }

    }
}
