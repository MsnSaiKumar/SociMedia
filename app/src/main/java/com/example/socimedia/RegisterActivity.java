package com.example.socimedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText email , pass , verify_pass ;
    private Button reg_btn;
    private TextView logIn;
    private FirebaseAuth auth;
    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialize_ids();

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext() , LoginActivity.class));
                finish();
            }
        });


        reg_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mEmail = email.getText().toString().trim();
                String mPass = pass.getText().toString().trim();
                String mVerifyPass = verify_pass.getText().toString().trim();



                if(TextUtils.isEmpty(mEmail))
                {
                    email.setError("Enter a Email");
                    return;
                }
                else if(TextUtils.isEmpty(mPass)){
                    pass.setError("Enter a password");
                    return;
                }
                else if(TextUtils.isEmpty(mVerifyPass))
                {
                    pass.setError("Enter pass again");
                    return;
                }
                else if( ! mPass.equals(mVerifyPass))
                {
                    Toast.makeText(getApplicationContext(), "Password Doesnt match",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Creating new Account");
                    loadingBar.setMessage("Please wait while creating new Account");
                    loadingBar.show();
                    loadingBar.setCanceledOnTouchOutside(true);

                    auth.createUserWithEmailAndPassword(mEmail ,mPass).addOnCompleteListener
                            (new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful())
                                    {
                                        Toast.makeText(getApplicationContext(),
                                                "SuccesFully authenticated",
                                                Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();

                                        startActivity(new Intent(getApplicationContext() ,
                                                SetupActivity.class));
                                        finish();
                                    }
                                    else
                                    {
                                        String message=task.getException().getMessage();
                                        Toast.makeText(getApplicationContext(), "error" +message,
                                                Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }

                                }
                            });

                }



            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();
        if(user != null)
        {
            sendToMainActivity();
        }
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(getApplicationContext() , MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void initialize_ids() {
        email =(EditText) findViewById(R.id.id_email_signUp);
        pass = (EditText)findViewById(R.id.id_pass_signUp);
        verify_pass = (EditText)findViewById(R.id.id_confirm_pass_signUp);
        reg_btn = (Button)findViewById(R.id.id_register_signUp);
        logIn = (TextView)findViewById(R.id.id_login_signUp);

        auth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

    }

    }

