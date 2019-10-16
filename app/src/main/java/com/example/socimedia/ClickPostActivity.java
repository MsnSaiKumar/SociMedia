package com.example.socimedia;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {
    private TextView PostDescription;
    private ImageView PostImage;
    private Button DeletePostButton,EditPostButton;
    private String PostKey,currentUserId,databaseUserId;
    private  String description,image;
    private DatabaseReference clickPostRef;
    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        // to get the key through intent from mainActivity 169 line//

        PostKey=getIntent().getExtras().get("PostKey").toString();

        mauth=FirebaseAuth.getInstance();
        currentUserId=mauth.getCurrentUser().getUid();//the id of the user who is online//

        clickPostRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        PostDescription=(TextView) findViewById(R.id.click_description);
        PostImage =(ImageView)findViewById(R.id.click_img);
        DeletePostButton=(Button)findViewById(R.id.click_delete_btn);
        EditPostButton=(Button)findViewById(R.id.click_edit_btn);

        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);

        clickPostRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {

                    // it retrives the desciption and postimage stored in firebaseDatabase into clickpostActivity//

                    description = dataSnapshot.child("description").getValue().toString();
                    image = dataSnapshot.child("Postimage").getValue().toString();
                    databaseUserId=dataSnapshot.child("uid").getValue().toString();

                    PostDescription.setText(description);
                    Picasso.with(ClickPostActivity.this).load(image).into(PostImage);

                    if(currentUserId.equals(databaseUserId))
                    {
                        // delete and edit button vil be displayed only whn the currentUsersId  =  User id present in database//
                        //mean oly for  the current user it displays delete & edit button//
                        DeletePostButton.setVisibility(View.VISIBLE);
                        EditPostButton.setVisibility(View.VISIBLE);
                    }
                    EditPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            EditCurrentPost(description);
                        }
                    });

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteCurrentpost();

            }
        });

    }

    private void EditCurrentPost(String description)
    {// this 1 should i check ?? tell ,me which function..

        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit post");

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
    builder.setView(inputField);


        builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
            clickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "post updated succesfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i)
            {
           dialog.cancel();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);

    }

    private void DeleteCurrentpost()
    {

        clickPostRef.removeValue();
        sendUserToMainActivity();
        Toast.makeText(this, "post has been sucessfully deleted", Toast.LENGTH_SHORT).show();
    }

    private void sendUserToMainActivity()
    {
        finish();
        Intent mainIntent = new Intent(this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
    }
}
