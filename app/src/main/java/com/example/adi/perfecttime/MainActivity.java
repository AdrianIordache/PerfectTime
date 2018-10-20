package com.example.adi.perfecttime;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

//import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private CircleImageView navProfileImage;
    private TextView navUsername;
    private ImageButton addNewPost, refreshMainButton;

    private FirebaseAuth mAuth;
    private DatabaseReference usersReference, postReference, likesReference, friendsReference;


    ProgressDialog loadingBar;
    RecyclerView.Adapter adapter;
    List<Posts> list = new ArrayList<>();
    List<String> FriendsIDs = new ArrayList<>();

    String currentUserId;
    int index = 0;
    boolean likeChecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference  = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesReference = FirebaseDatabase.getInstance().getReference().child("Likes");
        friendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(currentUserId);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Perfect Time");

        addNewPost = (ImageButton) findViewById(R.id.add_post_button);
        refreshMainButton = (ImageButton) findViewById(R.id.refresh_main_button);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        postList = (RecyclerView) findViewById(R.id.all_user_posts);
        postList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navUsername = (TextView) navView.findViewById(R.id.nav_user_name);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);

        navProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String visitID = currentUserId;
                Intent profileIntent = new Intent(MainActivity.this, ImageActivity.class);
                profileIntent.putExtra("VisitID", visitID);
                startActivity(profileIntent);
            }
        });


        usersReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("Full Name"))
                    {
                        String userName = dataSnapshot.child("Full Name").getValue().toString();
                        navUsername.setText(userName);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Username doesn't exist", Toast.LENGTH_SHORT).show();
                    }

                    if(dataSnapshot.hasChild("Profile Image"))
                    {
                        String image    = dataSnapshot.child("Profile Image").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(navProfileImage);


                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile Image doesn't exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

        addNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendUserToPostActivity();
            }
        });

        refreshMainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                refreshActivity();
            }
        });

        displayAllUsersPosts();


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

        usersReference.child(currentUserId).child("State").updateChildren(currentStateMap);


    }

    private void refreshActivity()
    {
        finish();
        Intent selfIntent = new Intent(MainActivity.this, MainActivity.class);
        selfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(selfIntent);
    }


    public void displayAllUsersPosts()
    {

        final Query sortPostsDescending = postReference.orderByChild("Counter");
        FriendsIDs.add(currentUserId);

        friendsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot friendSnapshot)
            {
                for(DataSnapshot dataSnapshot1 : friendSnapshot.getChildren())
                {
                    FriendsIDs.add(dataSnapshot1.child("UserID").getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sortPostsDescending.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot postSnapshot)
            {
                list.clear();

                for(DataSnapshot snapshot : postSnapshot.getChildren())
                {
                    if(FriendsIDs.contains(snapshot.child("UserID").getValue().toString()))
                    {
                        String date = snapshot.child("Date").getValue().toString();
                        String fullName = snapshot.child("FullName").getValue().toString();
                        String image = snapshot.child("Image").getValue().toString();
                        String postDescription = snapshot.child("PostDescription").getValue().toString();
                        String profileImage = snapshot.child("ProfileImage").getValue().toString();
                        String time = snapshot.child("Time").getValue().toString();
                        String userId = snapshot.child("UserID").getValue().toString();
                        long counter = (long) snapshot.child("Counter").getValue();
                        Posts post = new Posts(date, fullName, image, postDescription, profileImage, time, userId, counter);
                        list.add(post);
                    }
                }

                adapter = new RecycleViewAdapterPosts(MainActivity.this, list);
                postList.setAdapter(adapter);


                updateUserState("Online");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        /*sortPostsDescending.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    list.clear();
                    for(final DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {

                        String date = dataSnapshot1.child("Date").getValue().toString();
                        String fullName = dataSnapshot1.child("FullName").getValue().toString();
                        String image = dataSnapshot1.child("Image").getValue().toString();
                        String postDescription = dataSnapshot1.child("PostDescription").getValue().toString();
                        String profileImage = dataSnapshot1.child("ProfileImage").getValue().toString();
                        String time = dataSnapshot1.child("Time").getValue().toString();
                        String userId = dataSnapshot1.child("UserID").getValue().toString();
                        long counter = (long) dataSnapshot1.child("Counter").getValue();
                        Posts post = new Posts(date, fullName, image, postDescription, profileImage, time, userId, counter);
                        list.add(post);

                    }

                    adapter = new RecycleViewAdapterPosts(MainActivity.this, list);
                    postList.setAdapter(adapter);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

    }

    public class RecycleViewAdapterPosts extends RecyclerView.Adapter<RecycleViewAdapterPosts.ViewHolder>
    {

        Context context;
        List<Posts> Array;

        public RecycleViewAdapterPosts(Context context, List<Posts> TempList)
        {
            this.Array = TempList;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_post_layout, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {

            final Posts model = Array.get(position);

            final String PostKey = model.getUserID() + model.getDate() + model.getTime() + model.getCounter();

            holder.setDate(model.getDate());
            holder.setFullName(model.getFullName());
            holder.setTime(model.getTime());
            holder.setPostDescription(model.getPostDescription());
            holder.setProfileImage(model.getProfileImage());
            holder.setImage(model.getImage());

            holder.setLikeButtonStatus(PostKey);
            holder.setCommentButtonStatus(PostKey);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                    clickPostIntent.putExtra("PostKey", PostKey);
                    startActivity(clickPostIntent);
                }
            });

            holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent commentsIntent = new Intent(MainActivity.this, CommentActivity.class);
                    commentsIntent.putExtra("PostKey", PostKey);
                    startActivity(commentsIntent);
                }
            });

            holder.username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    String visitID = model.getUserID().toString();
                    Intent profileIntent = new Intent(MainActivity.this, PersonProfileActivity.class);
                    profileIntent.putExtra("VisitID", visitID);
                    startActivity(profileIntent);
                }
            });

            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    String visitID = model.getUserID().toString();
                    Intent profileIntent = new Intent(MainActivity.this, ImageActivity.class);
                    profileIntent.putExtra("VisitID", visitID);
                    startActivity(profileIntent);
                }
            });

            holder.likePostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    likeChecker = true;

                    likesReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            if(likeChecker == true)
                            {
                                if(dataSnapshot.child(PostKey).hasChild(currentUserId))
                                {
                                    likesReference.child(PostKey).child(currentUserId).removeValue();
                                    likeChecker = false;
                                }
                                else
                                {
                                    likesReference.child(PostKey).child(currentUserId).setValue(true);
                                    likeChecker = false;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });



        }

        @Override
        public int getItemCount() {

            return Array.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {



            View mView;
            ImageButton likePostButton, commentPostButton;
            TextView numberOfLikes, numberOfComments, username;
            int countLikes, countComments;
            CircleImageView image;
            String currentUserId;
            DatabaseReference LikesRef, CommentsRef;


            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;

                likePostButton = (ImageButton) mView.findViewById(R.id.like_button);
                commentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);

                numberOfLikes = (TextView) mView.findViewById(R.id.number_of_likes);
                numberOfComments = (TextView) mView.findViewById(R.id.number_of_comments);

                username = (TextView) mView.findViewById(R.id.post_user_name);
                image = (CircleImageView) mView.findViewById(R.id.post_profile_image);


                LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
                CommentsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
                currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }

            public void setLikeButtonStatus(final String PostKey)
            {
                LikesRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                       if(dataSnapshot.child(PostKey).hasChild(currentUserId))
                       {
                            countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                            likePostButton.setImageResource(R.drawable.like);
                            numberOfLikes.setText((Integer.toString(countLikes) + (" Likes")));
                       }
                       else
                       {
                           countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                           likePostButton.setImageResource(R.drawable.dislike);
                           numberOfLikes.setText((Integer.toString(countLikes) + (" Likes")));
                       }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }

            public void setCommentButtonStatus(final String PostKey)
            {
                CommentsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.child(PostKey).hasChild("Comments"))
                        {
                            countComments = (int) dataSnapshot.child(PostKey).child("Comments").getChildrenCount();
                            numberOfComments.setText((Integer.toString(countComments) + (" Comments")));
                        }
                        else
                        {
                            numberOfComments.setText("0 Comments");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }

            public void setFullName(String FullName)
            {
                username.setText(FullName);
            }

            public void setProfileImage(String ProfileImage)
            {

                Picasso.get().load(ProfileImage).into(image);
            }

            public void setTime(String Time)
            {
                TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
                PostTime.setText("  "  + Time);
            }

            public void setDate(String Date)
            {
                TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
                PostDate.setText("  "  + Date);
            }

            public void setPostDescription(String PostDescription)
            {
                TextView Description = (TextView) mView.findViewById(R.id.post_description);
                Description.setText(PostDescription);
            }

            public void setImage(String Image)
            {
                ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
                Picasso.get().load(Image).into(PostImage);
            }

        }
    }



    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        else
        {
            checkUserExistence();
        }

    }

    /*@Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        updateUserState("Offline");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        updateUserState("Offline");
    }*/

    @Override
    protected void onRestart()
    {
        super.onRestart();
        updateUserState("Online");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateUserState("Online");

    }

    private void checkUserExistence()
    {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.hasChild(current_user_id))
                {
                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendUserToPostActivity()
    {
        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(postIntent);

    }

    private void sendUserToFriendsActivity()
    {
        Intent friendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);

    }

    private void sendUserToProfileActivity()
    {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);

    }

    private void sendUserToSearchActivity()
    {
        Intent searchIntent = new Intent(MainActivity.this, SearchActivity.class);
        startActivity(searchIntent);

    }

    private void sendUserToMessageActivity()
    {
        Intent friendsIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(friendsIntent);
    }


    private void sendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);

        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(setupIntent);

        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);

        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(loginIntent);

        finish();

    }

    private void sendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);

        startActivity(settingsIntent);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item){

            switch (item.getItemId())
            {
                case R.id.nav_post:
                    sendUserToPostActivity();
                    break;

                case R.id.nav_profile:
                    sendUserToProfileActivity();
                    // Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_home:
                    Toast.makeText(this, "This is your Home!", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_search:
                    sendUserToSearchActivity();
                    //Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_friends:
                    sendUserToFriendsActivity();
                    //Toast.makeText(this, "Friends", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_messages:
                    sendUserToMessageActivity();
                    //Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_setting:
                    sendUserToSettingsActivity();
                    //Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                    break;

                case R.id.nav_logout:
                    updateUserState("Offline");
                    mAuth.signOut();
                    SendUserToLoginActivity();
                    //Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
}

