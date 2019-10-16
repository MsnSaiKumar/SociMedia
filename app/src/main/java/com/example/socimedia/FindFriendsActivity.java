package com.example.socimedia;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mtoolbar;
    private ImageButton SearchButton;
    private EditText SearchInputText;

    private DatabaseReference AllusersDatabaseRef;


    private RecyclerView SearchResultList;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
     mtoolbar=(Toolbar)findViewById(R.id.toolbar);
        getSupportActionBar().setTitle("Find Friends");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SearchResultList =(RecyclerView)findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton=(ImageButton)findViewById(R.id.find_search_btn);
        SearchInputText=(EditText)findViewById(R.id.find_friends_Search);


        AllusersDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Users");

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                String searhBoxInput = SearchInputText.getText().toString();

                SearchPeopleAndFriends(searhBoxInput);
            }
        });
    }

    private void SearchPeopleAndFriends(String searhBoxInput)
    {
        Toast.makeText(this, "Searching...", Toast.LENGTH_LONG).show();

        Query searchPeopleandFriendsQuery = AllusersDatabaseRef.orderByChild("fullName")
                .startAt(searhBoxInput).endAt(searhBoxInput+ "\uf8ff");

        FirebaseRecyclerAdapter<Findfriends , FindFriendsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Findfriends, FindFriendsViewHolder>
                        (
                                Findfriends.class,  //module class//
                                R.layout.all_users_display_layout,// all find friends  display//
                                FindFriendsViewHolder.class,
                                searchPeopleandFriendsQuery


                        ) {
                    @Override
                    protected void populateViewHolder(FindFriendsViewHolder ViewHolder, Findfriends model, final int position) {

                        ViewHolder.setFullName(model.getFullName());
                        ViewHolder.setStatus(model.getStatus());
                        ViewHolder.setProfileimage(getApplicationContext(),model.getProfileimage());


                        ViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visit_user_id = getRef(position).getKey();
                                Intent profileIntent = new Intent(FindFriendsActivity.this,PersonProfileActivity.class);
                                profileIntent.putExtra("visit_user_id" , visit_user_id);
                                startActivity(profileIntent);

                            }
                        });
                    }
                } ;
        SearchResultList.setAdapter(firebaseRecyclerAdapter);


    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        // constructor//
        public FindFriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView = itemView;

        }
        public void setProfileimage(Context ctx , String profileimage)
        {
            CircleImageView myImage = (CircleImageView)mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);

        }

        public void setFullName(String fullName)
        {
            TextView myName = (TextView)mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullName);
        }


        public void setStatus(String status)
        {
            TextView myStatus = (TextView)mView.findViewById(R.id.all_users_status);
            myStatus.setText(status);
        }



    }
}
