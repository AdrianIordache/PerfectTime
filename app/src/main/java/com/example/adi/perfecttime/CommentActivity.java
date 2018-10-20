package com.example.adi.perfecttime;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.CollapsibleActionView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private ImageButton postCommentButton;
    private EditText commentInputText;
    private RecyclerView commentsList;

    private String postKey;

    private DatabaseReference usersReference, postReference;
    private FirebaseAuth mAuth;
    private String currentUserID;

    RecyclerView.Adapter adapter;
    long commentCounter;
    List<Comments> list = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        postKey = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");
        postReference  = FirebaseDatabase.getInstance().getReference().child("Posts").child(postKey).child("Comments");

        commentsList = (RecyclerView) findViewById(R.id.comments_views);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);


        commentInputText = (EditText) findViewById(R.id.comments_input);
        postCommentButton = (ImageButton) findViewById(R.id.post_comment_button);

        postCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                usersReference.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            String userName = dataSnapshot.child("Username").getValue().toString();
                            String profileImage = dataSnapshot.child("Profile Image").getValue().toString();

                            validateComment(userName, profileImage);

                            commentInputText.setText("");
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
    protected void onStart() {
        super.onStart();

        displayAllComments();

    }

    private void displayAllComments()
    {
       postReference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot)
           {
               if(dataSnapshot.exists())
               {
                   list.clear();
                   for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                   {
                       String date = dataSnapshot1.child("Date").getValue().toString();
                       String username = dataSnapshot1.child("Username").getValue().toString();
                       String comment = dataSnapshot1.child("Comment").getValue().toString();
                       String profileImage = dataSnapshot1.child("ProfileImage").getValue().toString();
                       String time = dataSnapshot1.child("Time").getValue().toString();
                       String userId = dataSnapshot1.child("UserID").getValue().toString();
                       Comments comments = new Comments(time, date, username, userId, comment, profileImage);
                       //Comments comments = new Comments();
                       list.add(comments);
                   }
                   Collections.reverse(list);
                   adapter = new CommentActivity.RecycleViewAdapterComments(CommentActivity.this, list);
                   commentsList.setAdapter(adapter);
               }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError)
           {

           }
       });
    }


    public class RecycleViewAdapterComments extends RecyclerView.Adapter<RecycleViewAdapterComments.ViewHolder>
    {
        Context context;
        List<Comments> Array;

        public RecycleViewAdapterComments(Context context, List<Comments> TempList)
        {
            this.Array = TempList;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position)
        {

            Comments model = Array.get(position);

            holder.setDate(model.getDate());
            holder.setUserName(model.getUsername());
            holder.setTime(model.getTime());
            holder.setProfileImage(model.getProfileImage());
            holder.setTextInput(model.getComment());

        }

        @Override
        public int getItemCount() {

            return Array.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder
        {
            View mView;

            public ViewHolder(View itemView)
            {
                super(itemView);
                mView = itemView;
            }

            public void setUserName(String userName)
            {
                TextView username = (TextView) mView.findViewById(R.id.comments_user_name);
                username.setText(userName);
            }

            public void setProfileImage(String ProfileImage)
            {
                ImageView image = (ImageView) mView.findViewById(R.id.comments_profile_image);
                Picasso.get().load(ProfileImage).into(image);
            }

            public void setTime(String Time)
            {
                TextView CommentTime = (TextView) mView.findViewById(R.id.comments_time);
                CommentTime.setText(Time);
            }

            public void setDate(String Date)
            {
                TextView CommentsDate = (TextView) mView.findViewById(R.id.comments_date);
                CommentsDate.setText("  "  + Date);
            }

            public void setTextInput(String textInput)
            {
                TextView Input = (TextView) mView.findViewById(R.id.comments_text_input);
                Input.setText(textInput);
            }

        }

    }


    private void validateComment(String userName, String profileImage)
    {
        String commentText = commentInputText.getText().toString();

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    commentCounter = dataSnapshot.getChildrenCount();
                }
                else
                {
                    commentCounter = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please write text to comment...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd:MMMM:yyyy");
            final String saveCurrentDate = currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(callForTime.getTime());

            final String randomKey = currentUserID + saveCurrentDate + saveCurrentTime + commentCounter;

            HashMap<String, Object> commentsMap = new HashMap();
            commentsMap.put("UserID", currentUserID);
            commentsMap.put("Comment", commentText);
            commentsMap.put("Date", saveCurrentDate);
            commentsMap.put("Time", saveCurrentTime);
            commentsMap.put("Username", userName);
            commentsMap.put("ProfileImage", profileImage);
            commentsMap.put("Counter", commentCounter);

            postReference.child(randomKey).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(CommentActivity.this, "Your comments has been added successfully...", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(CommentActivity.this, "Error occurred, try again...", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
