package com.app.findmeapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;


import com.app.findmeapp.model.Coordinates;
import com.app.findmeapp.model.User;
import com.app.findmeapp.util.UsersAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatUsersActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private RecyclerView recyclerView;
    private List<User> userList = new ArrayList<>();
    private Coordinates currentLocation = new Coordinates();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        recyclerView = (RecyclerView)findViewById(R.id.users_recyclerView);
        recyclerView.setAdapter(new UsersAdapter(new ArrayList<User>(), currentLocation,this));
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        addUsersToList();
    }

    private void populateRecyclerView(){
        UsersAdapter usersAdapter = new UsersAdapter(userList, currentLocation,this);
        recyclerView.setAdapter(usersAdapter);
    }

    private void addUsersToList(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() > 0){
                    for(DataSnapshot snap: dataSnapshot.getChildren()){
                        User user = snap.getValue(User.class);
                        try {
                            if(!user.getUserID().equals(mAuth.getCurrentUser().getUid())){
                                userList.add(user);
                            } else {
                                currentLocation = user.getCoordinates();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                populateRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

