package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.guni.uvpce.ceit.food.MainActivity;
import com.guni.uvpce.ceit.food.R;
import com.guni.uvpce.ceit.food.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends BaseActivity implements View.OnClickListener {
    private EditText email, name, pass, repass, add, mobile;
    private TextView login;
    private Button register;
    private Toolbar toolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog loadingBar;

    private String email_id, fullName, address, mobileNo;

    private int passsame = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        email = findViewById(R.id.register_email);
        name = findViewById(R.id.register_name);
        pass = findViewById(R.id.register_password);
        repass = findViewById(R.id.register_repassword);
        add = findViewById(R.id.register_address);
        mobile = findViewById(R.id.register_phone);
        register = findViewById(R.id.register_button);
        login = findViewById(R.id.register_login_text);

        toolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Registration");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loadingBar = new ProgressDialog(this);

        register.setOnClickListener(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        repass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strPass1 = pass.getText().toString().trim();
                String strPass2 = repass.getText().toString().trim();

                if (!strPass1.equals(strPass2)) {
                    repass.setError("Password not matched");
                    passsame = 0;
                } else {
                    repass.setError(null);
                    passsame = 1;
                }
            }
        });
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
        } else if(passInput.length() < 6 || passInput.length() > 15){
            pass.setError("Please enter a valid password");
            return false;
        } else {
            pass.setError(null);
            return true;
        }
    }

    private boolean validateName(){
        String nameInput = name.getText().toString().trim();

        if(nameInput.isEmpty()){
            name.setError("Field can't be empty");
            return false;
        } else if(nameInput.length() > 50){
            name.setError("Please enter a valid Name");
            return false;
        } else {
            name.setError(null);
            return true;
        }
    }

    private boolean validateAddress(){
        String addInput = add.getText().toString().trim();

        if(addInput.isEmpty()){
            add.setError("Field can't be empty");
            return false;
        }
        else {
            add.setError(null);
            return true;
        }
    }

    private boolean validateMobileNo(){
        String mobileInput = mobile.getText().toString().trim();

        if(mobileInput.isEmpty()){
            mobile.setError("Field can't be empty");
            return false;
        } else if(mobileInput.length() != 10){
            mobile.setError("Please enter a valid Mobile No");
            return false;
        } else {
            mobile.setError(null);
            return true;
        }
    }


    @Override
    public void onClick(View v) {
        registerUser();
    }

    private void registerUser() {

        if (!validateEmail()  || !validateName() || !validatePassword() || !validateAddress() || !validateMobileNo() || passsame == 0) {
            return;
        }

        email_id = email.getText().toString().trim();
        String password = pass.getText().toString().trim();
        fullName = name.getText().toString().trim();
        address = add.getText().toString().trim();
        mobileNo = mobile.getText().toString().trim();
        final String user_role = "Customer";

        loadingBar.setMessage("Account is creating");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        mAuth.createUserWithEmailAndPassword(email_id, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            User user = new User(
                                    fullName,
                                    email_id,
                                    mobileNo,
                                    address,
                                    user_role
                            );

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user);

                                }
                        else {
                            //display a failure message
                            Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                            startActivity(intent);
                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this, "Fail", Toast.LENGTH_LONG).show();
                        }


                    }
                });
    }
}
