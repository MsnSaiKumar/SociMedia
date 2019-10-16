package com.example.socimedia;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity
{
    private RecyclerView myFriendsList;

    private FirebaseAuth mauth;
    private DatabaseReference FriendsRef,UserRef;
    private  String online_User_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);


        mauth=FirebaseAuth.getInstance();
        online_User_id=mauth.getCurrentUser().getUid();
        FriendsRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(online_User_id);
        UserRef=FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendsList =(RecyclerView)findViewById(R.id.friends_list);
        myFriendsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        myFriendsList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();


    }

    private void DisplayAllFriends()
    {
        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> obj =
                new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>
                        (
                                Friends.class,  //model class ,i.e java class sepaarte class called Friends//
                                R.layout.all_users_display_layout, //xml file//
                                FriendsViewHolder.class, //static class name ,present below//
                                FriendsRef

                        )
                {
                    @Override
                    protected void populateViewHolder(final FriendsViewHolder ViewHolder, final Friends model, int position)
                    {
                        ViewHolder.setDate(model.getDate());


                        final String usersIDs = getRef(position).getKey();

                        UserRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    final String username = dataSnapshot.child("fullName").getValue().toString();
                                    if (dataSnapshot.hasChild("profileimage")) {
                                        final String profileimage = dataSnapshot.child("profileimage").getValue().toString();
                                        ViewHolder.setProfileimage(getApplicationContext(),profileimage);
                                    }

                                ViewHolder.setFullName(username);



                                ViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        //to give options on alertDialpg//

                                        CharSequence options[] = new  CharSequence[]
                                                {
                                                  username + " 's Profile","Send Message"

                                                };
                                        AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                        builder.setTitle("Select Options");
                                        builder.setItems(options, new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int which)
                                            {
                                                if (which == 0)
                                                {
                                                    Intent ProfileIntent = new Intent(FriendsActivity.this,PersonProfileActivity.class);
                                                    ProfileIntent.putExtra("visit_user_id",usersIDs);
                                                    startActivity(ProfileIntent);
                                                }
                                                if (which==1)
                                                {
                                                    Intent ChatIntent = new Intent(FriendsActivity.this,ChatActivity.class);
                                                    ChatIntent.putExtra("visit_user_id",usersIDs);
                                                    ChatIntent.putExtra("userName",username);
                                                    startActivity(ChatIntent);
                                                }

                                            }
                                        });
                                        builder.show();
                                    }
                                });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                };
        myFriendsList.setAdapter(obj);
    }

    public static  class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public FriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView=itemView;
        }
        public void setProfileimage(Context ctx , String profileimage)
        {
            CircleImageView myImage = (CircleImageView)mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);


        }

        public void setFullName(String fullName)
        {
            TextView myName = (TextView)mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullName);
        }
        public void setDate(String date)
        {
            TextView friendsDate = (TextView)mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Friends since : " + date);
        }


    }
}
