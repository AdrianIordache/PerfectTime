package com.example.adi.perfecttime;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton sendMessageButton, sendImageButton;
    private EditText messageInput;
    private RecyclerView messageList;

    private String messageReceiverID, messageReceiverName, messageSenderID;
    private String saveCurrentDate, saveCurrentTime;

    private TextView receiverName, userLastSeen;
    private CircleImageView receiverProfileImage;
    private DatabaseReference rootReference, usersReference;
    private FirebaseAuth mAuth;

    RecyclerView.Adapter adapter;
    List<Messages> list = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        messageReceiverID = getIntent().getExtras().get("VisitID").toString();
        messageReceiverName = getIntent().getExtras().get("Username").toString();

        rootReference = FirebaseDatabase.getInstance().getReference();
        usersReference = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializationOfVariables();

        displayReceiverDetails();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendMessage();
            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MessageActivity.this, "Not yet developed, coming in future updates", Toast.LENGTH_SHORT).show();
            }
        });

        displayMessage();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        updateUserState("Online");
    }

    private void displayMessage()
    {
        updateUserState("Online");

        Query sortMessagesDescending = rootReference.child("Messages").child(messageSenderID).child(messageReceiverID).orderByChild("Counter");

        rootReference.child("Messages").child(messageSenderID).child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    list.clear();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        final String Date = dataSnapshot1.child("Date").getValue().toString();
                        final String Time = dataSnapshot1.child("Time").getValue().toString();
                        final String From = dataSnapshot1.child("From").getValue().toString();
                        final String Type = dataSnapshot1.child("Type").getValue().toString();
                        final String Message = dataSnapshot1.child("Message").getValue().toString();
                        Messages message = new Messages(Date, From, Message, Time, Type);
                        list.add(message);
                    }

                    Collections.reverse(list);
                    adapter = new RecyclerViewAdapterMessages(MessageActivity.this, list);

                    adapter.notifyDataSetChanged();
                    messageList.setAdapter(adapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private void sendMessage()
    {

        updateUserState("Online");

        String messageText = messageInput.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Please write a message first...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderReference = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverReference = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference usersMessageKey = rootReference.child("Messages").child(messageSenderID).child(messageReceiverID).push();

            String messageID = usersMessageKey.getKey();

            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd:MMMM:yyyy");
            saveCurrentDate = currentDate.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = currentTime.format(callForTime.getTime());

            HashMap<String, Object> Messages = new HashMap<>();
            Messages.put("Message", messageText);
            Messages.put("Time", saveCurrentTime);
            Messages.put("Date", saveCurrentDate);
            Messages.put("Type", "Text");
            Messages.put("From",  messageSenderID);

            HashMap<String, Object> messageDetails = new HashMap<>();
            messageDetails.put(messageSenderReference + "/" + messageID , Messages);
            messageDetails.put(messageReceiverReference + "/" + messageID , Messages);

            rootReference.updateChildren(messageDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        //Toast.makeText(MessageActivity.this, "Your message has been successfully sent", Toast.LENGTH_SHORT).show();
                        messageInput.setText("");
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(MessageActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        messageInput.setText("");
                    }

                }
            });

        }

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

        usersReference.child(messageSenderID).child("State").updateChildren(currentStateMap);


    }

    private void displayReceiverDetails()
    {
        receiverName.setText(messageReceiverName);

        rootReference.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    final String profileImage = dataSnapshot.child("Profile Image").getValue().toString();
                    final String Type = dataSnapshot.child("State").child("Type").getValue().toString();
                    final String Time = dataSnapshot.child("State").child("Time").getValue().toString();
                    final String Date = dataSnapshot.child("State").child("Date").getValue().toString();

                    if(Type.equals("Online"))
                    {
                        userLastSeen.setText("Online");
                    }
                    else
                    {
                        userLastSeen.setText("Last Seen: " + Date + " at " + Time);

                    }

                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void InitializationOfVariables()
    {

        messageList = (RecyclerView) findViewById(R.id.messages_list);
        messageList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        messageList.setLayoutManager(linearLayoutManager);

        mToolbar = (Toolbar) findViewById(R.id.messages_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.message_custom_bar, null);
        actionBar.setCustomView(actionBarView);


        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_message_profile_image);

        receiverName      = (TextView)    findViewById(R.id.custom_message_profile_name);
        userLastSeen      = (TextView)    findViewById(R.id.custom_message_last_seen);
        sendMessageButton = (ImageButton) findViewById(R.id.message_send_button);
        sendImageButton   = (ImageButton) findViewById(R.id.message_send_image_button);
        messageInput      = (EditText)    findViewById(R.id.message_input);



    }

    public class RecyclerViewAdapterMessages extends RecyclerView.Adapter<RecyclerViewAdapterMessages.ViewHolder>
    {
        Context context;
        List<Messages> Array;

        public RecyclerViewAdapterMessages(Context context, List<Messages> TempList) {
            this.Array = TempList;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_users_layout, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;

        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {

            final Messages model = Array.get(position);

            String fromUserID = model.getFrom();
            String messageType = model.getType();

            rootReference.child("Users").child(fromUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists())
                    {
                        String profileImage = dataSnapshot.child("Profile Image").getValue().toString();
                        holder.setProfileImage(profileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if(messageType.equals("Text"))
            {
                holder.ReceiverMessageText.setVisibility(View.INVISIBLE);
                holder.ReceiverProfileImage.setVisibility(View.INVISIBLE);

                if(fromUserID.equals(messageSenderID))
                {
                    holder.SenderMessageText.setBackgroundResource(R.drawable.sender_message_text_background);
                    holder.SenderMessageText.setTextColor(Color.WHITE);
                    holder.SenderMessageText.setGravity(Gravity.LEFT);
                    holder.SenderMessageText.setText(model.getMessage());
                }
                else
                {
                    holder.SenderMessageText.setVisibility(View.INVISIBLE);

                    holder.ReceiverMessageText.setVisibility(View.VISIBLE);
                    holder.ReceiverProfileImage.setVisibility(View.VISIBLE);

                    holder.ReceiverMessageText.setBackgroundResource(R.drawable.receiver_message_text_background);
                    holder.ReceiverMessageText.setTextColor(Color.WHITE);
                    holder.ReceiverMessageText.setGravity(Gravity.LEFT);
                    holder.ReceiverMessageText.setText(model.getMessage());
                }
            }


        }

        @Override
        public int getItemCount() {

            return Array.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {


            public TextView SenderMessageText, ReceiverMessageText;
            public CircleImageView ReceiverProfileImage;

            public ViewHolder(View itemView) {
                super(itemView);

                SenderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
                ReceiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
                ReceiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_user_image);
            }

            public void setProfileImage(String ProfileImage) {
                Picasso.get().load(ProfileImage).into(ReceiverProfileImage);
            }

        }
    }
}
