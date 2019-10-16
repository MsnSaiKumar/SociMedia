package com.example.socimedia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private TextView userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfImage;

    private FirebaseAuth mauth;
    private DatabaseReference ProfileUserRef,FriendsRef,PostsRef;

    private Button NoOfPostsBtn,NoOfFriendsBtn;

    private String CurrentUserID;
    private int countFriends=0,countPosts=0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mauth=FirebaseAuth.getInstance();
        CurrentUserID=mauth.getCurrentUser().getUid();
        ProfileUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserID);
        FriendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts");


        NoOfFriendsBtn=(Button)findViewById(R.id.no_of_friends_btn);
        NoOfPostsBtn=(Button)findViewById(R.id.no_of_posts_btn);

        userProfImage=(CircleImageView)findViewById(R.id.profile_profiPic);
        userName=(TextView) findViewById(R.id.profile_Username);
        userProfName=(TextView) findViewById(R.id.profile_ProfileName);
        userStatus=(TextView) findViewById(R.id.profile_status);
        userCountry=(TextView) findViewById(R.id.profile_country);
        userGender=(TextView) findViewById(R.id.profile_gender);
        userRelation=(TextView) findViewById(R.id.profile_relation);
        userDOB=(TextView) findViewById(R.id.profile_Dob);



FriendsRef.child(CurrentUserID).addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
    {
        if (dataSnapshot.exists())

        {

            countFriends =(int) dataSnapshot.getChildrenCount();
            NoOfFriendsBtn.setText(Integer.toString(countFriends) + "Friends");
        }
        else
        {
            NoOfFriendsBtn.setText("0 friends");
        }
    }


    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});


PostsRef.orderByChild("uid").startAt(CurrentUserID).endAt(CurrentUserID)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    countPosts=(int) dataSnapshot.getChildrenCount();
                    NoOfPostsBtn.setText(Integer.toString(countPosts) + "Posts");
                }
                else
                {
                    NoOfPostsBtn.setText("0 Posts");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        NoOfFriendsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToFriendsActivity();
            }
        });

        NoOfPostsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToPostsActivity();
            }
        });


        ProfileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    // Retriving user data from Firebasedatabase //

//                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String profileImage2 = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(getApplicationContext()).load(profileImage2).into(userProfImage);
                    }
                    String myUserName = dataSnapshot.child("userName").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullName").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDob = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("countryName").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipStatus").getValue().toString();


                    // Retrived data is ready to set/display on settings Activity//

//                    Picasso.with(ProfileActivity.this).load(myProfileImage).into(userProfImage);
                    userProfName.setText(myProfileName);
                    userName.setText(myUserName);
                    userCountry.setText("Country -  " + myCountry);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DOB -  " + myDob);
                    userGender.setText("Gender -  " + myGender);
                    userRelation.setText("Relation -  " + myRelationStatus);
                }
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void SendUserToFriendsActivity()
    {
        Intent fintent = new Intent(ProfileActivity.this ,FriendsActivity.class);
        startActivity(fintent);

    }
    private void SendUserToPostsActivity()
    {
        Intent fintent = new Intent(ProfileActivity.this ,MyPostsActivity.class);
        startActivity(fintent);


    }
}
