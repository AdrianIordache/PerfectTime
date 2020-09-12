package com.example.adi.perfecttime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText userEmail;
    private EditText userPassword;
    private EditText userConfirmPassword;
    private Button createAccountButton;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        userEmail = (EditText) findViewById(R.id.register_email);
        userPassword = (EditText) findViewById(R.id.register_password);
        userConfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        createAccountButton = (Button) findViewById(R.id.register_button);

        loadingBar = new ProgressDialog(this);

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null)
        {
            sendUserToMainActivity();
        }
    }

    private void sendEmailVerificationMessage()
    {
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null)
        {
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(RegisterActivity.this, "Registration Successful, please verify your Account", Toast.LENGTH_SHORT).show();
                        sendUserToLoginActivity();
                        mAuth.signOut();
                    }
                    else 
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(RegisterActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                    }
                }
            });
        }
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        String confirmPassword = userConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email) == true) {
            Toast.makeText(this, "Please write your email...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password) == true) {
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(confirmPassword) == true) {
            Toast.makeText(this, "Please confirm your password...", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords does not match!", Toast.LENGTH_SHORT).show();
        } else {


            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, your new account is being created...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.createUserWithEmailAndPassword(email, password)
               .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                   @Override
                   public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            sendEmailVerificationMessage();
                            loadingBar.dismiss();
                        }
                        else
                        {
                            String message = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                   }

               });

        }

    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent (RegisterActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

}
