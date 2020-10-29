package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.TextView;

import com.guni.uvpce.ceit.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends BaseActivity {
    private TextView txt1, txt2, txt3, txt4;
    private CircleImageView circleImageView;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String userid;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        txt1 = findViewById(R.id.my_profile_name);
        txt2 = findViewById(R.id.my_email);
        txt3 = findViewById(R.id.my_mobile);
        txt4 = findViewById(R.id.my_address);
        circleImageView = findViewById(R.id.my_profile_pic);

        mToolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile");

        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Wait");
        loadingBar.setMessage("Profile is fetching");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        mDatabase.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("profileImage")){
                    String image = dataSnapshot.child("profileImage").getValue().toString();
                    Picasso.get().load(image).into(circleImageView);
                }
                loadingBar.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child("Users").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                txt1.setText((String) dataSnapshot.child("name").getValue());
                txt2.setText("Email: " + (String) dataSnapshot.child("email").getValue());
                txt3.setText("Mobile: "+ (String) dataSnapshot.child("mobile_no").getValue());
                txt4.setText("Address: " + (String) dataSnapshot.child("address").getValue());
                if (dataSnapshot.hasChild("profileImage")) {
                    Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(circleImageView);
                }
                loadingBar.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
