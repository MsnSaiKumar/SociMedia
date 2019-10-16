package com.example.socimedia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private DatabaseReference PostsRef,likesref,userRef;
    private FirebaseAuth mauth;
    private  String currentUserId;
    private RecyclerView myPostsList;
    Boolean LikeChecker=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        likesref=FirebaseDatabase.getInstance().getReference().child("Likes");

        mauth=FirebaseAuth.getInstance();
        currentUserId=mauth.getCurrentUser().getUid();
        PostsRef =FirebaseDatabase.getInstance().getReference().child("Posts");

        mtoolbar = (Toolbar)findViewById(R.id.mypost_toolbar);
        getSupportActionBar().setTitle("My Posts");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        myPostsList =(RecyclerView)findViewById(R.id.posts_activity_recycle_view);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        DisplayAllMyPosts();

    }

    private void DisplayAllMyPosts()
    {
        Query myPostQuery =PostsRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId);


        FirebaseRecyclerAdapter<Posts,MyPostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_post_layout,
                                MyPostsViewHolder.class,
                                myPostQuery

                        ) {
                    @Override
                    protected void populateViewHolder(MyPostsViewHolder holder, Posts model, int position)
                    {
                        final String PostKey = getRef(position).getKey();
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
                                Intent clickPostIntent = new Intent(MyPostsActivity.this,ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey",PostKey);
                                startActivity(clickPostIntent);
                            }
                        });

                        holder.CommentPostButton.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent commentsIntent = new Intent(MyPostsActivity.this,CommentsActivity.class);
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
                                            if(dataSnapshot.child(PostKey).hasChild(currentUserId))
                                            {
                                                likesref.child(PostKey).child(currentUserId).removeValue();
                                                LikeChecker = false;
                                            }
                                            else
                                            {
                                                likesref.child(PostKey).child(currentUserId).setValue(true);
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




                myPostsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static  class MyPostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        int countLikes;
        String currentuserid;
        DatabaseReference LikesRef;


        ImageButton LikePostButton,CommentPostButton;
        TextView DisplayNoOfLikes;


        CircleImageView userDP;
        TextView userName , postDesc  , postDate  ,postTime;
        ImageView Postimage;
        public MyPostsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView=itemView;

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
        public void setProfileImage(Context ct, String profileImage)
        {
            Picasso.with(ct).load(profileImage).into(userDP);
        }

        public void setPostimage(Context ct , String postimage)
        {
            Picasso.with(ct).load(postimage).into(Postimage);
        }
    }
}
