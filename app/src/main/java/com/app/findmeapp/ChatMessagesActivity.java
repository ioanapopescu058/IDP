package com.app.findmeapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.app.findmeapp.model.ChatMessage;
import com.app.findmeapp.model.User;
import com.app.findmeapp.util.MessagesAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ChatMessagesActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private DatabaseReference messagesDBRef;
    private DatabaseReference usersDBRef;
    private List<ChatMessage> messagesList = new ArrayList<>();

    private String receiverId;
    private String receiverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_messages);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chatRecyclerView = (RecyclerView)findViewById(R.id.messagesRecyclerView);
        messageEditText = (EditText) findViewById(R.id.edit_message_text);
        ImageButton mSendImageButton = (ImageButton) findViewById(R.id.send_message_button);
        chatRecyclerView.setAdapter(new MessagesAdapter(new ArrayList<ChatMessage>(), this));

        chatRecyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(mLayoutManager);

        messagesDBRef = FirebaseDatabase.getInstance().getReference().child("messages");
        usersDBRef = FirebaseDatabase.getInstance().getReference().child("users");

        receiverId = getIntent().getStringExtra("USER_ID");

        mSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = messageEditText.getText().toString();
                String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                if(message.isEmpty()){
                    Toast.makeText(ChatMessagesActivity.this, "Can't send empty message", Toast.LENGTH_SHORT).show();
                }else {
                    sendMessageToFirebase(message, senderId, receiverId);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadMessages();
        queryFriendName(receiverId);
    }

    private void sendMessageToFirebase(String message, String senderId, String receiverId){
        messagesList.clear();

        ChatMessage chatMessage = new ChatMessage(message, senderId, receiverId);
        messagesDBRef.push().setValue(chatMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()){
                    Toast.makeText(ChatMessagesActivity.this, "Error " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }else{
                    messageEditText.setText(null);
                }
            }
        });
    }


    private void loadMessages(){

        messagesDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messagesList.clear();

                for(DataSnapshot snap: dataSnapshot.getChildren()){
                    ChatMessage chatMessage = snap.getValue(ChatMessage.class);
                    if(chatMessage.getSenderId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) &&
                            chatMessage.getReceiverId().equals(receiverId) || chatMessage.getSenderId().equals(receiverId) &&
                            chatMessage.getReceiverId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        messagesList.add(chatMessage);
                    }

                }
                populateMessagesRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void populateMessagesRecyclerView(){
        MessagesAdapter adapter = new MessagesAdapter(messagesList, this);
        chatRecyclerView.setAdapter(adapter);

    }

    private void queryFriendName(final String receiverId){

        usersDBRef.child(receiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User friendName = dataSnapshot.getValue(User.class);
                assert friendName != null;
                receiverName = friendName.getUsername();

                try {
                    Objects.requireNonNull(getSupportActionBar()).setTitle(receiverName);
                    Objects.requireNonNull(getActionBar()).setTitle(receiverName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
