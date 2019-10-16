package com.example.socimedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivityy extends AppCompatActivity {
    private Toolbar mtoolbar;
    private ProgressDialog loadingbar;
    private ImageButton SelectPostImage;
    private EditText PostDesccription;
    private Button UpdatePostButton;

    private static final int Gallery_pic = 1;
    private Uri Imageuri;
    private String Description;


    private StorageReference PostImagesReference;
    private DatabaseReference UserRef,PostsRef;
   private FirebaseAuth mauth;


   private  String saveCurrentDate ,saveCurrentTime,postRandomName,downloadUrl,current_user;
private long countPosts = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_activityy);

        initialize();

        SelectPostImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                OpenGallery();

            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
             validatePostInfo();
            }
        });

    }

    private void initialize()
    {

        mtoolbar = (Toolbar) findViewById(R.id.updatepostpagetoolbar);
        //  setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("update post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        SelectPostImage = (ImageButton) findViewById(R.id.id_posting_image);
        PostDesccription = (EditText) findViewById(R.id.id_posting_image_descrip);
        UpdatePostButton = (Button) findViewById(R.id.id_post_the_image_button);

        PostImagesReference= FirebaseStorage.getInstance().getReference();
        UserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts");

        loadingbar= new ProgressDialog(this);



    }

    private void validatePostInfo()
    {
         Description = PostDesccription.getText().toString();

        if(Imageuri == null)
        {
            Toast.makeText(this, "Please select post image.......", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this, "Please give the description.......", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingbar.setTitle("Add new post");
            loadingbar.setMessage("please wait we are updating new post");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();

            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage()
    {
        // used to display the date when user uploads the pic//

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyy");
        saveCurrentDate=currentDate.format(calForDate.getTime());

        // used to display the Time when user uploads the pic//

        Calendar calforTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime=currentTime.format(calForDate.getTime());


        postRandomName = saveCurrentDate + saveCurrentTime;

        final StorageReference filepath = PostImagesReference.child("Post Images").child(Imageuri.getLastPathSegment() + postRandomName +".jpg");

        //to check whether image is uploaded or not//

        filepath.putFile(Imageuri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
            {


            if (task.isSuccessful())
            {
                Toast.makeText(PostActivityy.this, "Image is uploaded Succesfully", Toast.LENGTH_SHORT).show();

                loadingbar.dismiss();

                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        downloadUrl = uri.toString();
                        SavingInfoToDatabase();

                    }
                });
            }
            else
            {
                String message = task.getException().getMessage();

                Toast.makeText(PostActivityy.this, "Error"+message, Toast.LENGTH_SHORT).show();
                loadingbar.dismiss();
            }
            }
        });
    }

    private void SavingInfoToDatabase()
    {
        mauth=FirebaseAuth.getInstance();
       final String current_user=mauth.getCurrentUser().getUid();

       PostsRef.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if(dataSnapshot.exists())
               {
                   //counts the number of posts//
                   countPosts =dataSnapshot.getChildrenCount();
               }
               else
               {
                   countPosts = 0;
               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });

        UserRef.child(current_user).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
            if (dataSnapshot.exists())

            {

                String Userfullname=dataSnapshot.child("fullName").getValue().toString();
                String UserProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                HashMap postmap = new HashMap();
                postmap.put("uid",current_user);
                postmap.put("date",saveCurrentDate);
                postmap.put("time",saveCurrentTime);
                postmap.put("description",Description);
                postmap.put("Postimage",downloadUrl);
                postmap.put("ProfileImage",UserProfileImage);
                postmap.put("fullName",Userfullname);
                postmap.put("counter",countPosts);

                PostsRef.child(current_user + postRandomName).updateChildren(postmap)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task)
                            {

                            if (task.isSuccessful())
                            {



                                Toast.makeText(PostActivityy.this, " New Post is updated successully", Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                                loadingbar.dismiss();
                            }
                            else
                            {


                                Toast.makeText(PostActivityy.this, "Errorr occurrd while updating your post", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                            }
                        });



            }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void SendUserToMainActivity()
    {

        Intent mainIntent = new Intent(PostActivityy.this,MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_pic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_pic && resultCode == RESULT_OK && data != null) {
            Imageuri = data.getData();
            SelectPostImage.setImageURI(Imageuri);
        }


    }

}