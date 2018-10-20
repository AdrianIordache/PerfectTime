package com.example.adi.perfecttime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private ImageView googleSignInButton, facebookButton, twitterButton;
    private EditText UserEmail;
    private EditText UserPassword;
    private Button registerButton;
    private ProgressDialog loadingBar;
    private TextView forgetPassword;

    private FirebaseAuth mAuth;
    private Boolean emailAddressChecker;


    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;
    private  static final  String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        registerButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        googleSignInButton = (ImageView) findViewById(R.id.google_button);
        facebookButton = (ImageView) findViewById(R.id.facebook_button);
        twitterButton = (ImageView) findViewById(R.id.twitter_button);
        loginButton = (Button) findViewById(R.id.login_button);
        forgetPassword = (TextView) findViewById(R.id.forget_password_link);

        loadingBar = new ProgressDialog(this);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToRegisterActivity();
            }

            private void SendUserToRegisterActivity()
            {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {
                allowUserToLogin();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Toast.makeText(LoginActivity.this, "Connection to Google Sign In Failed...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                signIn();
            }
        });

        facebookButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Toast.makeText(LoginActivity.this, "Not yet developed, coming in future updates", Toast.LENGTH_SHORT).show();
            }
        });

        twitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "Not yet developed, coming in future updates", Toast.LENGTH_SHORT).show();
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

    }



    private void signIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {
            loadingBar.setTitle("Google Sign In");
            loadingBar.setMessage("Please wait, while we are allowing you to login into using your Google Account...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if(result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                authWithGoogle(account);
                Toast.makeText(this, "Please wait, while we are getting your account...", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Can't get the Authentication result ", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }


    private void authWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "authWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            sendUserToMainActivity();
                            loadingBar.dismiss();
                        } else {

                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = task.getException().toString();
                            sendUserToLoginActivity();
                            Toast.makeText(LoginActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    private void verifyEmailAdress()
    {
        FirebaseUser user = mAuth.getCurrentUser();
        emailAddressChecker = user.isEmailVerified();

        if(emailAddressChecker)
        {
            sendUserToMainActivity();
            Toast.makeText(LoginActivity.this, "You've been logged in successfully!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Please verify your Account first", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }

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

    private void allowUserToLogin()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please write your email...", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty((password)))
        {
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else
        {

            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait, while we are allowing you to login into your Account...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                verifyEmailAdress();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }

                    });
        }

    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();

    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(LoginActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }
}
