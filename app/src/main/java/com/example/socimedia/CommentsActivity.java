package com.example.socimedia;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {
    private ImageButton PostCommentButton;
    private EditText  CommentinputText;
    private RecyclerView commentsList;


    private FirebaseAuth mauth;
    private DatabaseReference userRef,PostsRef;


    private String CurrentUserId;
    private  String Post_key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_key=getIntent().getExtras().get("PostKey").toString();


        mauth=FirebaseAuth.getInstance();
        CurrentUserId=mauth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef=FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_key).child("Comments");

        commentsList=(RecyclerView)findViewById(R.id.comment_list);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);



        CommentinputText =(EditText)findViewById(R.id.comment_input);
        PostCommentButton =(ImageButton)findViewById(R.id.comment_ImageBtn);




        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                userRef.child(CurrentUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                    if(dataSnapshot.exists())
                    {
                   String username=dataSnapshot.child("userName").getValue().toString();

                   ValidateComment(username);

                   CommentinputText.setText("");
                    }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });


        displayAllComments();
    }

    private void displayAllComments() {

        FirebaseRecyclerAdapter<Comments , CommentsHolder> myObj=
                new FirebaseRecyclerAdapter<Comments, CommentsHolder>
                        (
                                Comments.class,
                                R.layout.all_comments_layout,
                                CommentsHolder.class,
                                PostsRef

                        )
                {
                    @Override
                    protected void populateViewHolder(CommentsHolder holder, Comments model, int pos) {

                        holder.setComment(model.getComment()); // ( model.getcomment() ) is object.functionName //
                        holder.setUserName(model.getUserName());
                        holder.setDate(model.getDate());
                        holder.setTime(model.getTime());


                    }
                };

        commentsList.setAdapter(myObj);
    }



    public static class CommentsHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView UserName,userComments,userDate,userTime;

        public CommentsHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            UserName = (TextView)mView.findViewById(R.id.comment_username);
            userComments = (TextView)mView.findViewById(R.id.comment_text);
            userDate=(TextView)mView.findViewById(R.id.comment_date);
            userTime=(TextView)mView.findViewById(R.id.comment_time);

        }

        public void setComment(String comment)

        {
             userComments.setText(comment);
        }

        public void setUserName(String userName)
        {
            UserName.setText(userName);

        }


        public void setDate(String date)
        {
            userDate.setText(date);

        }

        public void setTime(String time)
        {
            userTime.setText(time);
        }



    }



    private void ValidateComment(String username)
    {

        // where user writes the commentText field //
        String commentText = CommentinputText.getText().toString();
        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this, "Please write text to a comment", Toast.LENGTH_SHORT).show();
        }
        else
        {


            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMM-yyy");
        final String saveCurrentDate=currentDate.format(calForDate.getTime());

            // used to display the Time //

            Calendar calforTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime=currentTime.format(calforTime.getTime());


            final  String RandomKey = CurrentUserId + saveCurrentDate + saveCurrentTime + System.currentTimeMillis();

            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",CurrentUserId);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("userName",username);

            PostsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {

                            if(task.isSuccessful())
                            {
                                Toast.makeText(CommentsActivity.this, "You Have Commented Sucessfully", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(CommentsActivity.this, "Error Try again.....", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }

    }
}
