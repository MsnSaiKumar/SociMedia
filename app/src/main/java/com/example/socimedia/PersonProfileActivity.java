package com.example.socimedia;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity
{
    private TextView userName,userProfName1,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfImage;
    private Button SendRequestBtn,DeclineRequestBtn;

    private DatabaseReference FriendRequestRef,UserRef,FriendsRef;
    private FirebaseAuth mauth;

    private String senderuserid,recieveruserid,CURRENT_STATE,  saveCurrentDate;

    private Uri imageuri1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mauth=FirebaseAuth.getInstance();

        //id of the online current user  //
        senderuserid=mauth.getCurrentUser().getUid();

        //id ,of the person to whom we send request  ....findfriendsActivity(95)//
        recieveruserid=getIntent().getExtras().get("visit_user_id").toString();

        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef=FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");

        initialize_id();






        UserRef.child(recieveruserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    // Retriving user data from Firebasedatabase //

                    if (dataSnapshot.hasChild("profileimage")) {
                        String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(PersonProfileActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
                    }
                    else
                    {
                        Picasso.with(PersonProfileActivity.this).load(imageuri1).placeholder(R.drawable.profile).into(userProfImage);
                    }
                    String myUserName = dataSnapshot.child("userName").getValue().toString();
                    String myProfileName1 = dataSnapshot.child("fullName").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDob = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("countryName").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipStatus").getValue().toString();


                    // Retrived data is ready to set/display on settings Activity//


                   userProfName1.setText(myProfileName1);
                    userName.setText(myUserName);
                    userCountry.setText("Country -  " + myCountry);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("DOB -  " + myDob);
                    userGender.setText("Gender -  " + myGender);
                    userRelation.setText("Relation -  " + myRelationStatus);

                    MaintananceofButtons();
                }
            }




            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
// at the begging decline request button vil not display whn u open other users profile in findfriends activity//

DeclineRequestBtn.setVisibility(View.INVISIBLE);
DeclineRequestBtn.setEnabled(false);

if(!senderuserid.equals(recieveruserid))
{
    //mean it appearss sendRequest button whn we find/search other friends to send Friendrequest//


    SendRequestBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view)
        {

      //    after pressing on send requestButton on above we will disable tat button i.e is given below//

            SendRequestBtn.setEnabled(false);

            if(CURRENT_STATE.equals("not_friends"))
            {
                SendFriendRequestToaPerson();
            }

            if(CURRENT_STATE.equals("request_sent"))
            {
                cancelFriendRequest();
            }
            if(CURRENT_STATE.equals("request_recieved"))
            {
                AcceptFriendRequest();
            }
            if(CURRENT_STATE.equals("friends"))
            {
                UnfriendAnExistingFriend();
            }

        }
    });

}
else
{
    //mean it doesnt appear both sendRequestButton & DeclineRequestButton whn user opens his own account only//
    DeclineRequestBtn.setVisibility(View.INVISIBLE);
    SendRequestBtn.setVisibility(View.INVISIBLE);

}

   }

    private void UnfriendAnExistingFriend()
    {
        FriendsRef.child(senderuserid).child(recieveruserid).child("request_type")
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(recieveruserid).child(senderuserid).child("request_type")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                SendRequestBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendRequestBtn.setText("send Friend Request");


                                                DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                                DeclineRequestBtn.setEnabled(false);


                                            }
                                        }
                                    });
                        }

                    }
                });


    }

    private void AcceptFriendRequest()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyyy");
        saveCurrentDate=currentDate.format(calForDate.getTime());

        FriendsRef.child(senderuserid).child(recieveruserid).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    FriendsRef.child(recieveruserid).child(senderuserid).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                FriendRequestRef.child(senderuserid).child(recieveruserid)
                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful())
                                        {
                                            FriendRequestRef.child(recieveruserid).child(senderuserid)
                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                    {
                                                        SendRequestBtn.setEnabled(true);
                                                        CURRENT_STATE ="Friends";
                                                        SendRequestBtn.setText("Unfriend");

                                                        DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                                        DeclineRequestBtn.setEnabled(false);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }

                        }
                    });

                }

            }
        });

    }

    private void SendFriendRequestToaPerson()
    {

        FriendRequestRef.child(senderuserid).child(recieveruserid).child("request_type")
                .setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRequestRef.child(recieveruserid).child(senderuserid).child("request_type")
                                    .setValue("recieved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                SendRequestBtn.setEnabled(true);
                                                CURRENT_STATE = "request_sent";
                                                SendRequestBtn.setText("cancel Friend Request");


                                                DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                                DeclineRequestBtn.setEnabled(false);


                                            }
                                        }
                                    });
                        }

                    }
                });
    }

    private void cancelFriendRequest()
    {
        FriendRequestRef.child(senderuserid).child(recieveruserid).child("request_type")
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRequestRef.child(recieveruserid).child(senderuserid).child("request_type")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                SendRequestBtn.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                SendRequestBtn.setText("send Friend Request");


                                                DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                                DeclineRequestBtn.setEnabled(false);


                                            }
                                        }
                                    });
                        }

                    }
                });

    }

    private void MaintananceofButtons()
    {
        FriendRequestRef.child(senderuserid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(recieveruserid).hasChild("request_type"))
                        {
                            String request_type = dataSnapshot.child(recieveruserid).child("request_type").getValue().toString();

                            if(request_type.equals("sent"))
                            {
                                CURRENT_STATE = "request_sent";
                                SendRequestBtn.setText("cancel Friend Request");

                                DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                DeclineRequestBtn.setEnabled(false);

                            }
                            else if (request_type.equals("recieved"))
                            {
                                CURRENT_STATE = "request_recieved";
                                SendRequestBtn.setText("Accept Frien Request");

                                DeclineRequestBtn.setVisibility(View.VISIBLE);
                                DeclineRequestBtn.setEnabled(true);

                                DeclineRequestBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        UnfriendAnExistingFriend();

                                    }
                                });

                            }

                        }

                        else
                        {
                            FriendsRef.child(senderuserid)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                        CURRENT_STATE = "friends";
                                      SendRequestBtn.setText("Unfriend");

                                        DeclineRequestBtn.setVisibility(View.INVISIBLE);
                                        DeclineRequestBtn.setEnabled(false);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }



    private void initialize_id()
    {
        userProfImage=(CircleImageView)findViewById(R.id.person_profile_profiPic);
        userName=(TextView) findViewById(R.id.person_profile_Username);
        userProfName1=(TextView) findViewById(R.id.person_profile_ProfileName);
        userStatus=(TextView) findViewById(R.id.person_profile_status);
        userCountry=(TextView) findViewById(R.id.person_profile_country);
        userGender=(TextView) findViewById(R.id.person_profile_gender);
        userRelation=(TextView) findViewById(R.id.peerson_profile_relation);
        userDOB=(TextView) findViewById(R.id.person_profile_Dob);

        SendRequestBtn=(Button)findViewById(R.id.send_request_button);
        DeclineRequestBtn=(Button)findViewById(R.id.decline_request_button);



        CURRENT_STATE = "not_friends";

    }

}
