package com.guni.uvpce.ceit.food;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.guni.uvpce.ceit.food.R;
import com.guni.uvpce.ceit.food.activity.BaseActivity;
import com.guni.uvpce.ceit.food.activity.DashboardActivity;
import com.guni.uvpce.ceit.food.activity.RegisterActivity;
import com.guni.uvpce.ceit.food.activity.ResetPasswordActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private EditText email, pass;
    private TextView forgetPass, register;
    private Button loginButton;

    private FirebaseAuth mAuth;
    private CardView loginCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        email = findViewById(R.id.login_email);
        pass = findViewById(R.id.login_password);
        forgetPass = findViewById(R.id.login_forget_pass_text);
        register = findViewById(R.id.login_register_text);
        loginButton = findViewById(R.id.login_button);
        loginCardView = findViewById(R.id.loginCard);
        loginCardView.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(MainActivity.this);

        forgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user =mAuth.getCurrentUser();
        if (user != null){
            //handle already login user
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            loginCardView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("YES",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                finishAffinity();
                                System.exit(0);
                            }
                        })
                .setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // code to do on NO tapped
                                dialog.cancel();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private boolean validateEmail() {
        String emailInput = email.getText().toString().trim();

        if(emailInput.isEmpty()){
            email.setError("Field can't be empty");
            return false;
        } else if(!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()){
            email.setError("Please enter a valid email address");
            return false;
        } else {
            email.setError(null);
            return true;
        }
    }

    private boolean validatePassword(){
        String passInput = pass.getText().toString().trim();

        if(passInput.isEmpty()){
            pass.setError("Field can't be empty");
            return false;
        } else {
            pass.setError(null);
            return true;
        }
    }

    @Override
    public void onClick(View v) {
        loginUser();
    }

    private void loginUser() {
        if (!validateEmail()|| !validatePassword()) {
            Toast.makeText(this, "Fail", Toast.LENGTH_SHORT).show();
            return;
        }
        String emailInput = email.getText().toString().trim();
        String passInput = pass.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(emailInput, passInput)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
