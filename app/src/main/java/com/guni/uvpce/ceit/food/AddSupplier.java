package com.guni.uvpce.ceit.food;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.guni.uvpce.ceit.food.activity.AddAdminActivity;
import com.guni.uvpce.ceit.food.activity.DashboardActivity;
import com.guni.uvpce.ceit.food.model.ItemList;
import com.guni.uvpce.ceit.food.model.User;

import java.util.ArrayList;
import java.util.HashMap;

public class AddSupplier extends AppCompatActivity implements View.OnClickListener {
    private EditText email, name, pass, repass, add, mobile;
    private Button register;
    private Toolbar toolbar;
    private LinearLayout linearLayout;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private ProgressDialog loadingBar;

    private String email_id, fullName, address, mobileNo;
    private ArrayList<String> itemList = new ArrayList<String>();
    private int passsame = 0, itemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_supplier);

        email = findViewById(R.id.register_supplier_email);
        name = findViewById(R.id.register_supplier_name);
        pass = findViewById(R.id.register_supplier_password);
        repass = findViewById(R.id.register_supplier_repassword);
        add = findViewById(R.id.register_supplier_address);
        mobile = findViewById(R.id.register_supplier_phone);
        register = findViewById(R.id.register_supplier_button);
        linearLayout = (LinearLayout) findViewById(R.id.register_supplier_item_list);

        toolbar = findViewById(R.id.register_supplier_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Register Supplier");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("Items").exists()){
                    TextView textView = new TextView(AddSupplier.this);
                    textView.setText("Select Supplier Item");
                    textView.setTextSize(20);
                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(textView);
                    for (DataSnapshot snapshot : dataSnapshot.child("Items").getChildren()){
                        final String item = snapshot.child("item_name").getValue().toString();
                        CheckBox checkBox = new CheckBox(AddSupplier.this);
                        checkBox.setText(item);
                        checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked){
                                    itemCount += 1;
                                    itemList.add(item);
                                }
                                else {
                                    itemCount -= 1;
                                    itemList.remove(item);
                                }
                            }
                        });
                        linearLayout.addView(checkBox);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadingBar = new ProgressDialog(this);

        register.setOnClickListener(this);

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

        if (!validateEmail()  || !validateName() || !validatePassword() || !validateAddress() || !validateMobileNo() || passsame == 0 || itemCount == 0) {
            return;
        }

        email_id = email.getText().toString().trim();
        String password = pass.getText().toString().trim();
        fullName = name.getText().toString().trim();
        address = add.getText().toString().trim();
        mobileNo = mobile.getText().toString().trim();
        final String user_role = "Supplier";

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
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    /*if (task.isSuccessful()) {
                                       Intent intent = new Intent(AddSupplier.this, DashboardActivity.class);
                                        startActivity(intent);
                                        loadingBar.dismiss();
                                        Toast.makeText(AddSupplier.this, "Success", Toast.LENGTH_LONG).show();
                                    } else {
                                        //display a failure message
                                        Intent intent = new Intent(AddSupplier.this, DashboardActivity.class);
                                        startActivity(intent);
                                        loadingBar.dismiss();
                                        Toast.makeText(AddSupplier.this, "Fail", Toast.LENGTH_LONG).show();
                                    }*/
                                }
                            });

                            String s = "";
                            HashMap hashMap = new HashMap();
                            for (int i = 0; i < itemList.size(); i++){
                                s += itemList.get(i) + ",";
                                //hashMap.put(s, itemList.get(i));
                            }

                            FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("Items").setValue(s)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(AddSupplier.this, DashboardActivity.class);
                                        startActivity(intent);
                                        loadingBar.dismiss();
                                        Toast.makeText(AddSupplier.this, "Success", Toast.LENGTH_LONG).show();
                                    } else {
                                        //display a failure message
                                        Intent intent = new Intent(AddSupplier.this, DashboardActivity.class);
                                        startActivity(intent);
                                        loadingBar.dismiss();
                                        Toast.makeText(AddSupplier.this, "Fail", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                });
    }
}
