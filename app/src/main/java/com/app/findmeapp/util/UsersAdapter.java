package com.app.findmeapp.util;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.findmeapp.ChatMessagesActivity;
import com.app.findmeapp.R;
import com.app.findmeapp.model.Coordinates;
import com.app.findmeapp.model.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private List<User> userList;
    private Coordinates currentLocation;
    private Context mContext;
    private FirebaseAuth mAuth;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;

        public View layout;

        ViewHolder(View view) {
            super(view);
            layout = view;
            username = (TextView) view.findViewById(R.id.user_name);
        }
    }

    public UsersAdapter(List<User> myDataset, Coordinates coordinates, Context context) {
        userList = myDataset;
        currentLocation = coordinates;
        mContext = context;
    }

    public void add(int position, User person) {
        userList.add(position, person);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        userList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.user_row, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final User user = userList.get(position);

        if (currentLocation != null) {
            Location me = new Location("me");

            me.setLatitude(currentLocation.getLatitude());
            me.setLongitude(currentLocation.getLongitude());

            Location friend = new Location("friend");

            friend.setLatitude(user.getCoordinates().getLatitude());
            friend.setLongitude(user.getCoordinates().getLongitude());

            int distance = Math.round(me.distanceTo(friend));

            long diffInMs = currentLocation.getTimestamp() - user.getCoordinates().getTimestamp();
            long diffInSec = TimeUnit.MILLISECONDS.toMinutes(diffInMs);

            viewHolder.username.setText(user.getUsername() + " se afla la o distanta de " + distance + " metri.\n" +
                    "Ultimul update al utilizatorului " + user.getUsername() + ": inregistrat in urma cu " + diffInSec + " minute.");
        } else if (currentLocation == null) {
            viewHolder.username.setText(user.getUsername());
        }

        viewHolder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToChatMessagesActivity(user.getUserID());
            }
        });
    }

    private void goToChatMessagesActivity(String personId){
        Intent intent = new Intent(mContext, ChatMessagesActivity.class);
        intent.putExtra("USER_ID", personId);
        mContext.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}