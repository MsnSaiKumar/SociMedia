package com.example.socimedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private EditText userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB;
    private CircleImageView userProfImage;
    private Button updateAccountSettingbutton;
    private int Gallery_pic=1;
    private ProgressDialog mDialog;
    private StorageReference userProfileRef;


    private FirebaseAuth mauth;
    private DatabaseReference SettingUserRef;
    private  String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mtoolbar=(Toolbar)findViewById(R.id.settings_toolbar);
        getSupportActionBar().setTitle("Account Settings");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);




        mauth=FirebaseAuth.getInstance();
        currentUserId=mauth.getCurrentUser().getUid();
        SettingUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        userProfileRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        userProfImage=(CircleImageView)findViewById(R.id.settings_profile_img);
        userName=(EditText)findViewById(R.id.settings_username);
        userProfName=(EditText)findViewById(R.id.settings_profilename);
        userStatus=(EditText)findViewById(R.id.settings_status);
        userCountry=(EditText)findViewById(R.id.settings_country);
        userGender=(EditText)findViewById(R.id.settings_gender);
        userRelation=(EditText)findViewById(R.id.settings_relationship_status);
        userDOB=(EditText)findViewById(R.id.settings_dob);
        updateAccountSettingbutton=(Button)findViewById(R.id.settings_update_button);

        mDialog = new ProgressDialog(this);

 SettingUserRef.addValueEventListener(new ValueEventListener() {
     @Override
     public void onDataChange(@NonNull DataSnapshot dataSnapshot)
     {

         if(dataSnapshot.exists())
         {
             // Retriving user data from Firebasedatabase // sai ....................????????????????????/

//             String myProfileImage =dataSnapshot.child("profileimage").getValue().toString();
             if(dataSnapshot.hasChild("profileimage"))
             {
                 String profileImage2 = dataSnapshot.child("profileimage").getValue().toString();

                 Picasso.with(getApplicationContext()).
                     load(profileImage2).placeholder(R.drawable.profile).into(userProfImage);

             }
             String myUserName =dataSnapshot.child("userName").getValue().toString();
             String myProfileName =dataSnapshot.child("fullName").getValue().toString();
             String myProfileStatus =dataSnapshot.child("status").getValue().toString();
             String myDob =dataSnapshot.child("dob").getValue().toString();
             String myCountry =dataSnapshot.child("countryName").getValue().toString();
             String myGender =dataSnapshot.child("gender").getValue().toString();
             String myRelationStatus =dataSnapshot.child("relationshipStatus").getValue().toString();


             // Retrived data is ready to set/display on settings Activity//
//             Picasso.with(SettingsActivity.this).load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
             userName.setText(myUserName);
             userProfName.setText(myProfileName);
             userCountry.setText(myCountry);
             userStatus.setText(myProfileStatus);
             userDOB.setText(myDob);
             userGender.setText(myGender);
             userRelation.setText(myRelationStatus);

         }
     }

     @Override
     public void onCancelled(@NonNull DatabaseError databaseError) {

     }
 });
 updateAccountSettingbutton.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View view) {
         ValidateAccountInfo();
     }
 });
 userProfImage.setOnClickListener(new View.OnClickListener() {
     @Override
     public void onClick(View view)
     {

         Intent galleryIntent = new Intent();
         galleryIntent.setType("image/*");
         galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
         startActivityForResult(galleryIntent,Gallery_pic);
     }
 });


}
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_pic && resultCode==RESULT_OK && data!=null)
        {
            Uri imageuri=data.getData();
            CropImage.activity()   // crop image
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)//x axisRatio=1,y axisRatio=1
                    .start(this);


        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result =CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                mDialog.setTitle("Profile image");
                mDialog.setMessage("Please wait while we are updating profile pic...");
                mDialog.setCanceledOnTouchOutside(true);
                mDialog.show();

                Uri resulturi= result.getUri();
                Picasso.with(getApplicationContext()).load(resulturi).into(userProfImage);

                final StorageReference filepath =userProfileRef.child(currentUserId +".jpg");//stores userimage of particular id

                filepath.putFile(resulturi).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()     //  saving to storage and confirming it
                {

                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {

                        if(task.isSuccessful())

                        {
                            Toast.makeText(SettingsActivity.this, "Profile pic stored successfully", Toast.LENGTH_SHORT).show();


//                                    //  getting the image url and saving it to the firebase database and confirming it


                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                            {
                                @Override
                                public void onSuccess(Uri uri)
                                {
                                    final String downloadUrl = uri.toString();

                                    SettingUserRef.child("profileimage").setValue(downloadUrl)// creates subfolder as profileImage
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override

                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        Intent selfIntent=new Intent(SettingsActivity.this,SettingsActivity.class);
                                                        startActivity(selfIntent);

                                                        Toast.makeText(SettingsActivity.this, "profile img stored to FirebaseDatabase succesfully", Toast.LENGTH_SHORT).show();
                                                        mDialog.dismiss();
                                                    }

                                                    else
                                                    {
                                                        String message =task.getException().getMessage();
                                                        Toast.makeText(SettingsActivity.this, "error"+message, Toast.LENGTH_SHORT).show();
                                                        mDialog.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });


            }
            else
            {
                Toast.makeText(this, "error image cant be cropped", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void ValidateAccountInfo()
    {
        // updated data is entered by user//
        String username = userName.getText().toString();
        String profilename = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String dob = userDOB.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();

        if(TextUtils.isEmpty(username))
        {

            Toast.makeText(this, "please write username", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(profilename))
        {
            Toast.makeText(this, "please write Profilename", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(status))
        {
            Toast.makeText(this, "please write status", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(dob))
        {
            Toast.makeText(this, "please write DOB", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "please write Your country Name", Toast.LENGTH_SHORT).show();
        }

       else if(TextUtils.isEmpty(gender))
        {
            Toast.makeText(this, "please write your gender", Toast.LENGTH_SHORT).show();
        }

       else if(TextUtils.isEmpty(relation))
        {
            Toast.makeText(this, "please write your RelationshipStatus", Toast.LENGTH_SHORT).show();
        }
       else
        {
            mDialog.setTitle("Profie image");
            mDialog.setMessage("Please wait while we are updating...");
            mDialog.setCanceledOnTouchOutside(true);
            mDialog.show();

            UpdateAccountInfo(username,profilename,status,dob,country,gender,relation);
        }

    }

    private void UpdateAccountInfo(String username, String profilename, String status, String dob, String country, String gender, String relation)
    {

        HashMap userMap = new HashMap();
        userMap.put("userName",username);
        userMap.put("fullName",profilename);
        userMap.put("status",status);
        userMap.put("dob",dob);
        userMap.put("countryName",country);
        userMap.put("gender",gender);
        userMap.put("relationshipStatus",relation);

        SettingUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(SettingsActivity.this, "Account settings updated Succesfully", Toast.LENGTH_SHORT).show();
                    sendToMainActivity();
                    mDialog.dismiss();
                }
                else
                {


                    Toast.makeText(SettingsActivity.this, "error occureedd while updating Account Setting information", Toast.LENGTH_SHORT).show();
                    mDialog.dismiss();
                }
            }
        });
    }
    private void sendToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this , MainActivity.class);
        startActivity(intent);
        finish();
    }
}
