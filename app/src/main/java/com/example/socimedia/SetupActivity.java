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

public class SetupActivity extends AppCompatActivity {
    private CircleImageView profile_pic;
    private EditText user_Name ,full_name ,country_name ;
    private Button save;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    String current_user_id;
    private ProgressDialog mDialog;
    private static final int Gallery_Pic = 1;
    private StorageReference userProfileRef ;

    private Uri imageUri;
    private  String downloadUrl="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        initialize_id();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAccountSetupInformation();
            }
        });

        // it allows the user to open gallery//

        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent gallery_intent = new Intent();
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                gallery_intent.setType("image/*");
                startActivityForResult(gallery_intent ,Gallery_Pic);
            }
        });
        userRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {

                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profileimage"))

                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(profile_pic);
                    }
                    else
                    {

                        Picasso.with(SetupActivity.this).load(imageUri).placeholder(R.drawable.profile).into(profile_pic);
                        Toast.makeText(SetupActivity.this, "please sellect profile image first", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initialize_id() {
        profile_pic = (CircleImageView)findViewById(R.id.id_profile_image_setup);
        user_Name = (EditText)findViewById(R.id.id_multiline_username);
        full_name = (EditText)findViewById(R.id.id_multiline_fullName);
        country_name =(EditText)findViewById(R.id.id_multiline_country);
        save = (Button)findViewById(R.id.id_saveInfo_Setup);

        downloadUrl="";

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        mDialog = new ProgressDialog(this);
        userProfileRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
    }



    private void saveAccountSetupInformation() {

        String userName = user_Name.getText().toString().trim();
        String fullName = full_name.getText().toString().trim();
        String countryName = country_name.getText().toString().trim();


        if(TextUtils.isEmpty(userName))
        {
            user_Name.setError("Please enter the name");
            return;
        }
        else if(TextUtils.isEmpty(fullName))
        {
            full_name.setError("Please enter the full name");
            return;
        }
        else if(TextUtils.isEmpty(countryName))
        {
            country_name.setError("Please enter the country name");
            return;
        }
        else if (downloadUrl .equals(""))
        {
            Toast.makeText(this, "you must select a pic", Toast.LENGTH_SHORT).show();
            return;
        }

        else
        {

                mDialog.setTitle("Saving information..");
                mDialog.setMessage("Please wait while we are creating new Account...");
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                HashMap userMap = new HashMap();
                userMap.put("userName" , userName);
                userMap.put("fullName" , fullName);
                userMap.put("countryName" , countryName);
                userMap.put("status" , "Keep Rocking always" );
                userMap.put("gender" , "male");
                userMap.put("dob" , "01/01/0000");
                userMap.put("profileimage",downloadUrl);
                userMap.put("relationshipStatus" , ".......");
                userRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(SetupActivity.this,
                                    "account creation done", Toast.LENGTH_SHORT).show();
                            mDialog.dismiss();
                            sendToMainActivity();

                        }
                        else
                        {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "" +
                                    "couldn't save account info", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        }
            }




//    To pick the image//

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_Pic && resultCode==RESULT_OK && data!=null)
        {
             imageUri=data.getData();
            CropImage.activity()   // crop image//
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
                mDialog.setCanceledOnTouchOutside(false);
                mDialog.show();

                Uri resulturi= result.getUri();
                Picasso.with(getApplicationContext()).load(resulturi).placeholder(R.drawable.profile).into(profile_pic);

                final StorageReference filepath =userProfileRef.child(current_user_id +".jpg");//stores userimage of particular id//

                filepath.putFile(resulturi).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()     //  saving to storage and confirming it
                {

                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(SetupActivity.this, "Profile pic stored successfully", Toast.LENGTH_SHORT).show();


                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
                            {
                                @Override
                                public void onSuccess(Uri uri)
                                {
                                     downloadUrl = uri.toString();

                                     mDialog.dismiss();

//                                    userRef.child("profileimage").setValue(downloadUrl)// creates subfolder as profileImage//
//                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//
//                                                public void onComplete(@NonNull Task<Void> task)
//                                                {
//                                                    if (task.isSuccessful())
//                                                    {
//                                                        Intent selfIntent=new Intent(SetupActivity.this,SetupActivity.class);
//                                                        startActivity(selfIntent);
//
//                                                        Toast.makeText(SetupActivity.this, "profile img stored to FirebaseDatabase succesfully", Toast.LENGTH_SHORT).show();
//                                                        mDialog.dismiss();
//                                                    }
//
//                                                    else
//                                                    {
//                                                        String message =task.getException().getMessage();
//                                                        Toast.makeText(SetupActivity.this, "error"+message, Toast.LENGTH_SHORT).show();
//                                                        mDialog.dismiss();
//                                                    }
//                                                }
//                                            });
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



    private void sendToMainActivity() {
        Intent intent = new Intent(getApplicationContext() , MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}

