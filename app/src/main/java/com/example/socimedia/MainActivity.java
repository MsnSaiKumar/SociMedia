package com.example.socimedia;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserame;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, postsRef,likesref;

    private CircleImageView navProfileImage;
    private TextView navProfileName;
    private String UserID;
    private ImageButton AddnewPostButton;

    private Uri imageUri;


Boolean LikeChecker=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        UserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesref=FirebaseDatabase.getInstance().getReference().child("Likes");
        postsRef=FirebaseDatabase.getInstance().getReference().child("Posts");



        AddnewPostButton = (ImageButton) findViewById(R.id.id_add_new_post_button);

        drawerLayout = (DrawerLayout) findViewById(R.id.id_drawer_layout);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        postList=(RecyclerView)findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);

// displays new post at top and old one at bottom//
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        linearLayoutManager.setReverseLayout(true);

        linearLayoutManager.setStackFromEnd(true);

        postList.setLayoutManager(linearLayoutManager);


        navigationView = (NavigationView) findViewById(R.id.id_navigation_view);


        View navView = navigationView.inflateHeaderView(R.layout.navigationheader);//used for profile image icon in NavigationBar//
        // Here  we used *navView*  with findViewById because if it doesnt write tat it vil crash the app//
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.id_nav_profile_image);
        NavProfileUserame = (TextView) navView.findViewById(R.id.id_nav_user_profile_name);




        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        AddnewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToPostActivity();

            }
        });
      DisplayAllUsersPosts();
      DisplayImageAndNameOnTheNavigation();


    }



    private void DisplayAllUsersPosts()
    {
        // displays new posts at the bottom ,uploaded by any user//
        Query sortPostsInDecendingOrder = postsRef.orderByChild("counter");


// adapter acts as mediator tat retrieves data from firebase and set in mainActivity//
        FirebaseRecyclerAdapter<Posts,PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(
                        Posts.class,
                        R.layout.all_post_layout,
                        PostsViewHolder.class,
                        sortPostsInDecendingOrder ) {
                    @Override
                    protected void populateViewHolder(PostsViewHolder holder, Posts model, int position)
                    {

                        // whn user click on the post it get the position and key of the post  //
                        final String PostKey = getRef(position).getKey();

                        // all this is to display post details on MainActivity//
                        holder.setPostimage(getApplicationContext() , model.getPostimage()) ;
                        holder.setProfileImage(getApplicationContext(),model.getProfileImage());
                        holder.setDate(model.getDate());
                        holder.setTime(model.getTime());
                        holder.setDescription(model.getDescription());
                        holder.setFullName(model.getFullName());

                        holder.setLikeButtonStatus(PostKey);

                        //when user clicks on post it sends to clickPostActivity//

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent = new Intent(MainActivity.this,ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey",PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        holder.CommentPostButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent commentsIntent = new Intent(MainActivity.this,CommentsActivity.class);
                                commentsIntent.putExtra("PostKey",PostKey);
                                startActivity(commentsIntent);

                            }
                        });

                        holder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                LikeChecker = true;

                                likesref.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                      if(LikeChecker.equals(true))
                                      {
                                          if(dataSnapshot.child(PostKey).hasChild(UserID))
                                          {
                                              likesref.child(PostKey).child(UserID).removeValue();
                                              LikeChecker = false;
                                          }
                                          else
                                          {
                                              likesref.child(PostKey).child(UserID).setValue(true);
                                              LikeChecker=false;
                                          }
                                      }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {




                                    }
                                });
                            }
                        });

                    }
                };
postList.setAdapter(firebaseRecyclerAdapter);


    }

    private void DisplayImageAndNameOnTheNavigation()
    {

        userRef.child(UserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("fullName")) {
                        final String fullname = dataSnapshot.child("fullName").getValue().toString();
                        NavProfileUserame.setText(fullname);
                    }

                }
              if (dataSnapshot.exists() && dataSnapshot.hasChild("profileimage") )
                    {

                         String image = dataSnapshot.child("profileimage").getValue().toString();
                        System.out.println("profile image uri = "+ image );

                        Picasso.with(getApplicationContext()).
                                load(image).placeholder(R.drawable.profile).into(NavProfileImage);

                    }




            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
//
//    private void sendUserToImageViewerActivity(String fullname, String image)
//    {
//
//    }

    public  static  class PostsViewHolder extends RecyclerView.ViewHolder
    {
        int countLikes;
        String currentuserid;
        DatabaseReference LikesRef;


        ImageButton LikePostButton,CommentPostButton;
        TextView DisplayNoOfLikes;

        View mView;
        CircleImageView userDP;
        TextView userName , postDesc  , postDate  ,postTime;
        ImageView Postimage;



        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;

            LikePostButton=(ImageButton)mView.findViewById(R.id.like_button);
            CommentPostButton=(ImageButton)mView.findViewById(R.id.Comment_button);
            DisplayNoOfLikes=(TextView)mView.findViewById(R.id.display_no_of_likes);


            LikesRef=FirebaseDatabase.getInstance().getReference().child("Likes");
            currentuserid=FirebaseAuth.getInstance().getUid();


            userDP = (CircleImageView)mView.findViewById(R.id.post_profile_image);
            userName = (TextView) mView.findViewById(R.id.post_user_name);
            postDesc = (TextView) mView.findViewById(R.id.post_descrption);
            postDate = (TextView) mView.findViewById(R.id.post_date);
            postTime =(TextView) mView.findViewById(R.id.post_time);
            Postimage=(ImageView) mView.findViewById(R.id.post_image);

        }

        public void setLikeButtonStatus(final  String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
               if(dataSnapshot.child(PostKey).hasChild(currentuserid))
               {
                   // it counts the no of likes and stores in countLikes//
                   countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                   LikePostButton.setImageResource(R.drawable.like);
                   DisplayNoOfLikes.setText(Integer.toString(countLikes) + ("Likes"));
               }
               else
               {
                   countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                   LikePostButton.setImageResource(R.drawable.dislike);
                   DisplayNoOfLikes.setText(Integer.toString(countLikes) + ("Likes"));
               }


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        public void setFullName(String fullName)
        {
        userName.setText(fullName);
        }

        public void setDescription(String description)
        {
            postDesc.setText(description);
        }

        public void setTime(String time)
        {
            postTime.setText(time);
        }

        public void setDate(String date)
        {
            postDate.setText(date);
        }
        public void setProfileImage( Context ct,String profileImage)
        {
            Picasso.with(ct).load(profileImage).into(userDP);
        }

            public void setPostimage(Context ct , String postimage)
        {
            Picasso.with(ct).load(postimage).into(Postimage);
        }



    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {

            sendUserToLoginActivity();
        } else {
            checkUserExistence();

        }

    }


    private void UserMenuSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.id_nav_add_new_post:
                sendUserToPostActivity();
                break;

            case R.id.id_nav_profile:
                Toast.makeText(this, "profile", Toast.LENGTH_SHORT).show();
                SendUserToProfileActivity();
                break;



            case R.id.id_nav_home:
                Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.id_nav_friends:
                SendUserToFriendsActivity();
                Toast.makeText(this, "friends", Toast.LENGTH_SHORT).show();
                break;

            case R.id.id_nav_find_friends:
                Toast.makeText(this, "find friends", Toast.LENGTH_SHORT).show();
                SendUserToFindFriendActivity();
                break;

            case R.id.id_nav_messages:
                SendUserToFriendsActivity();
                Toast.makeText(this, "messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.id_nav_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                sendUserToSettingActivity();
                break;

            case R.id.id_nav_logout:
                mAuth.signOut();
                sendUserToLoginActivity();
                break;

            case R.id.id_rate:
                sendUserToRatingActivity();
                break;

        }
    }



//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//
//    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Exit")
                .setMessage("want to exit..")
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();

                    }
                })
                .setNegativeButton("No", null)
                .setCancelable(true);

        builder.create().show();
    }


    private void sendUserToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    private void checkUserExistence() {

        final String current_user_id = mAuth.getCurrentUser().getUid();

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if( ! dataSnapshot.hasChild(current_user_id)) {

                    sendUserToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(getApplicationContext() , SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendUserToRatingActivity() {
        Intent fintent = new Intent(getApplicationContext() ,RatingActivity.class);
        startActivity(fintent);
    }

    private void sendUserToSettingActivity() {
        Intent fintent = new Intent(getApplicationContext() ,SettingsActivity.class);
        startActivity(fintent);
    }

    private void sendUserToPostActivity() {
        Intent fintent = new Intent(getApplicationContext() ,PostActivityy.class);
        startActivity(fintent);

    }
    private void SendUserToProfileActivity()
    {
        Intent fintent = new Intent(getApplicationContext() ,ProfileActivity.class);
        startActivity(fintent);

    }
    private void SendUserToFindFriendActivity()
    {
        Intent fintent = new Intent(getApplicationContext() ,FindFriendsActivity.class);
        startActivity(fintent);
    }

    private void SendUserToFriendsActivity()
    {
        Intent fintent = new Intent(getApplicationContext() ,FriendsActivity.class);
        startActivity(fintent);

    }


}


