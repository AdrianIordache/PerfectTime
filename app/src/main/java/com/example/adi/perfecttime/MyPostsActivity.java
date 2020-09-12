package com.example.adi.perfecttime;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;

    RecyclerView.Adapter adapter;
    List<Posts> list = new ArrayList<>();

    private FirebaseAuth mAuth;
    private DatabaseReference postsReference, likesReference;

    private String currentUserID;
    boolean likeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        postsReference = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesReference = FirebaseDatabase.getInstance().getReference().child("Likes");


        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostsList = (RecyclerView) findViewById(R.id.my_all_post_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        displayMyAllPosts();

    }



    private void displayMyAllPosts()
    {

        Query myPostsQuery = postsReference.orderByChild("UserID").startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        myPostsQuery.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            if(dataSnapshot.exists())
            {
                list.clear();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
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
                adapter = new RecycleViewAdapterMyPosts(MyPostsActivity.this, list);
                myPostsList.setAdapter(adapter);
            }


        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });

    }


    public class RecycleViewAdapterMyPosts extends RecyclerView.Adapter<RecycleViewAdapterMyPosts.ViewHolder>
    {

        Context context;
        List<Posts> Array;

        public RecycleViewAdapterMyPosts(Context context, List<Posts> TempList)
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

            Posts model = Array.get(position);

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
                    Intent clickPostIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                    clickPostIntent.putExtra("PostKey", PostKey);
                    startActivity(clickPostIntent);
                }
            });

            holder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent commentsIntent = new Intent(MyPostsActivity.this, CommentActivity.class);
                    commentsIntent.putExtra("PostKey", PostKey);
                    startActivity(commentsIntent);
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
                                if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                                {
                                    likesReference.child(PostKey).child(currentUserID).removeValue();
                                    likeChecker = false;
                                }
                                else
                                {
                                    likesReference.child(PostKey).child(currentUserID).setValue(true);
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
            TextView numberOfLikes, numberOfComments;
            int countLikes, countComments;
            String currentUserId;
            DatabaseReference LikesRef, CommentsRef;


            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;

                likePostButton = (ImageButton) mView.findViewById(R.id.like_button);
                commentPostButton = (ImageButton) mView.findViewById(R.id.comment_button);

                numberOfLikes = (TextView) mView.findViewById(R.id.number_of_likes);
                numberOfComments = (TextView) mView.findViewById(R.id.number_of_comments);


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
                TextView username = (TextView) mView.findViewById(R.id.post_user_name);
                username.setText(FullName);
            }

            public void setProfileImage(String ProfileImage)
            {
                CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
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
}
