package com.example.socimedia;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {
    private Toolbar mtolbar;
    private Button ResetPasswordSendEmailButton;
    private EditText ResetEmailInput;

    private FirebaseAuth mauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mtolbar = (Toolbar) findViewById(R.id.forget_password_toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        ResetPasswordSendEmailButton = (Button) findViewById(R.id.reset_sendemailbutton);
        ResetEmailInput = (EditText) findViewById(R.id.reset_edittext);


        mauth = FirebaseAuth.getInstance();

        ResetPasswordSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail = ResetEmailInput.getText().toString();

                if (TextUtils.isEmpty(userEmail)) {
                    Toast.makeText(ResetPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mauth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                                Toast.makeText(ResetPasswordActivity.this, "please check your email", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(getApplicationContext(),"error" + message ,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
    }
}
