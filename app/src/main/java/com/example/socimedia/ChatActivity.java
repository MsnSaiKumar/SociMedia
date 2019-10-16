package com.example.socimedia;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chattoolbar;
    private ImageButton sendMessageButton, sendImageFileButton;
    private EditText userMessageInput;

    private RecyclerView userMessageList;
    private  List<Messages> messagesList= new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messagesAdapter;

    private  String messageRecieverId,messageReceiverName,messageSenderId,saveCurrentDate,saveCurrentTime;

    private TextView receiverName;
    private CircleImageView receiverProfileImage;

    private DatabaseReference RootRef;
    private FirebaseAuth mauth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mauth=FirebaseAuth.getInstance();
        messageSenderId=mauth.getCurrentUser().getUid();

        RootRef= FirebaseDatabase.getInstance().getReference();



        messageRecieverId=getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName=getIntent().getExtras().get("userName").toString();


            // open my code broh

        Initializefields();


        DisplayReciverinfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SendMessage();
            }
        });

//        FetchMessages();

    }

    private void FetchMessages()
    {
        RootRef.child("Message").child(messageSenderId).child(messageRecieverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {

                        if(dataSnapshot.exists())
                        {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            System.out.println("wassup man");
                            System.out.println("12345");
                            messagesList.add(messages);
                            messagesAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }






    @Override
    protected void onStart()
    {
        super.onStart();
        FetchMessages();
    }

    private void SendMessage()
    {

        String messageText= userMessageInput.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(ChatActivity.this, "please write message", Toast.LENGTH_SHORT).show();
        }

        else
        {
            String message_sender_ref ="Message/" + messageSenderId + "/" + messageRecieverId;
            String message_reciever_ref ="Message/" + messageRecieverId + "/" + messageSenderId;

            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderId).child(messageRecieverId).push();

            String message_push_id = user_message_key.getKey();

            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyy");
            saveCurrentDate=currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat CurrentTime = new SimpleDateFormat("HH:mm aa");
            saveCurrentTime = CurrentTime.format(calForDate.getTime());

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id,messageTextBody);
            messageBodyDetails.put(message_reciever_ref + "/" + message_push_id,messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "Message sent sucessfully", Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }
                    else
                    {
                        String message =task.getException().getMessage();
                        Toast.makeText(ChatActivity.this, "error " + message, Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }


                }
            });


        }
    }



    private void DisplayReciverinfo()
    {
        receiverName.setText(messageReceiverName);

        RootRef.child("Users").child(messageRecieverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    final String friendprofileimage = dataSnapshot.child("profileimage").getValue().toString();
                    Picasso.with(ChatActivity.this).load(friendprofileimage).placeholder(R.drawable.profile).into(receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void Initializefields()
    {
        chattoolbar = (Toolbar)findViewById(R.id.chat_bar_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar,null);
        getSupportActionBar().setCustomView(action_bar_view);

        receiverName =(TextView)findViewById(R.id.custom_profile_name);
        receiverProfileImage=(CircleImageView)findViewById(R.id.custom_image);

        sendMessageButton=(ImageButton)findViewById(R.id.id_chat_activity_send_message_button);
        sendImageFileButton=(ImageButton)findViewById(R.id.id_selct_file_btn);
        userMessageInput=(EditText)findViewById(R.id.id_chat_activity_message_text);


        messagesAdapter = new MessageAdapter(messagesList);
        userMessageList=(RecyclerView)findViewById(R.id.id_chat_activity_recycle_view);
        linearLayoutManager= new LinearLayoutManager(this);

        userMessageList.setHasFixedSize(true);
        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messagesAdapter);

    }

}
