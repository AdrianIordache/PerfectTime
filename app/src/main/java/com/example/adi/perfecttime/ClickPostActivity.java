package com.example.adi.perfecttime;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView postImage;
    private TextView postDescription;
    private Button deletePostButton, editPostButton;

    private String PostKey, description, image, currentUserID, userID;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        postImage = (ImageView) findViewById(R.id.click_post_image);
        postDescription = (TextView) findViewById(R.id.click_post_description);
        deletePostButton = (Button) findViewById(R.id.delete_post_button);
        editPostButton = (Button) findViewById(R.id.edit_post_button);

        deletePostButton.setVisibility(View.INVISIBLE);
        editPostButton.setVisibility(View.INVISIBLE);

        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    description = dataSnapshot.child("PostDescription").getValue().toString();
                    image = dataSnapshot.child("Image").getValue().toString();
                    userID = dataSnapshot.child("UserID").getValue().toString();

                    postDescription.setText(description);
                    Picasso.get().load(image).into(postImage);

                    if(currentUserID.equals(userID))
                    {
                        deletePostButton.setVisibility(View.VISIBLE);
                        editPostButton.setVisibility(View.VISIBLE);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        deletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                deleteCurrentPost();
            }
        });

        editPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                editCurrentPost(description);
            }
        });

    }

    private void editCurrentPost(String description)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");

        final EditText inputField = new EditText(ClickPostActivity.this);

        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                clickPostRef.child("PostDescription").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "The Post has been updated successfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }

    private void deleteCurrentPost()
    {
        clickPostRef.removeValue();
        sendUserToMainActivity();
        Toast.makeText(this, "The Post has been deleted", Toast.LENGTH_SHORT).show();
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }
}
