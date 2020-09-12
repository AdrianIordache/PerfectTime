package com.example.adi.perfecttime;

import android.app.ProgressDialog;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class SearchActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchInputText;

    private RecyclerView searchResultList;

    ProgressDialog loadingBar;
    RecyclerView.Adapter adapter;
    List<Friends> list = new ArrayList<>();

    private DatabaseReference friendsReference;
    private FirebaseAuth mAuth;
    private String currentUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        friendsReference = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.find_friends_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Search Friends");

        searchResultList = (RecyclerView) findViewById(R.id.search_friends_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

        searchButton = (ImageButton) findViewById(R.id.search_friends_button);
        searchInputText = (EditText) findViewById(R.id.find_friends_search_box);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchBoxInput = searchInputText.getText().toString();
                searchPeopleOrFriends(searchBoxInput);
            }
        });

    }

    private void searchPeopleOrFriends(String searchBoxInput)
    {
        Toast.makeText(this, "Searching... ", Toast.LENGTH_LONG).show();


        Query searchQuery = friendsReference.orderByChild("Full Name").startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    list.clear();
                    for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
                    {
                        String profileImage = dataSnapshot1.child("Profile Image").getValue().toString();
                        String fullName = dataSnapshot1.child("Full Name").getValue().toString();
                        String status = dataSnapshot1.child("Status").getValue().toString();
                        String userId = dataSnapshot1.getKey();

                        Friends friend = new Friends(profileImage, fullName, status, userId);
                        list.add(friend);
                    }

                    adapter = new RecycleViewAdapterFriends(SearchActivity.this, list);
                    searchResultList.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public class RecycleViewAdapterFriends extends RecyclerView.Adapter<RecycleViewAdapterFriends.ViewHolder> {

        Context context;
        List<Friends> Array;

        public RecycleViewAdapterFriends(Context context, List<Friends> TempList) {
            this.Array = TempList;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_user_layout, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;

        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            final Friends model = Array.get(position);


            holder.setFullName(model.getFullName());
            holder.setProfileImage(model.getProfileImage());
            holder.setStatus(model.getStatus());


            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    String visitID = model.getUserID().toString();
                    Intent profileIntent = new Intent(SearchActivity.this, PersonProfileActivity.class);
                    profileIntent.putExtra("VisitID", visitID);
                    startActivity(profileIntent);
                }
            });

        }

        @Override
        public int getItemCount() {

            return Array.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {


            View mView;

            public ViewHolder(View itemView) {
                super(itemView);
                mView = itemView;

            }

            public void setFullName(String FullName) {
                TextView username = (TextView) mView.findViewById(R.id.user_search_full_name);
                username.setText(FullName);
            }

            public void setProfileImage(String ProfileImage) {
                CircleImageView image = (CircleImageView) mView.findViewById(R.id.all_user_find_friends_image);
                Picasso.get().load(ProfileImage).into(image);
            }

            public void setStatus(String Status) {
                TextView userStatus = (TextView) mView.findViewById(R.id.user_search_status);
                userStatus.setText(Status);
            }

        }
    }
}
