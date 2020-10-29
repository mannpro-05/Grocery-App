package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.guni.uvpce.ceit.food.MainActivity;
import com.guni.uvpce.ceit.food.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends BaseActivity {
    private Button button;
    private EditText editText;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        button = findViewById(R.id.reset_pass_btn);
        editText = findViewById(R.id.reset_pass_email);
        toolbar = findViewById(R.id.reset_pass_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Reset Password");

        mAuth = FirebaseAuth.getInstance();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editText.getText().toString();
                if (TextUtils.isEmpty(email)){
                    Toast.makeText(ResetPasswordActivity.this,"Please Enter email!!!", Toast.LENGTH_LONG).show();
                }
                else
                {
                    mAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ResetPasswordActivity.this,"Please check your mail for reset password", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ResetPasswordActivity.this, MainActivity.class));
                                    }
                                    else {
                                        Toast.makeText(ResetPasswordActivity.this,"Something is wrong!!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}
