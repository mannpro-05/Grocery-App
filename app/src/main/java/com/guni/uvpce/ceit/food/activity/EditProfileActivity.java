package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.guni.uvpce.ceit.food.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends BaseActivity {

    private EditText ed1, ed2, ed3;
    private Button button;
    private CircleImageView circleImageView;
    private Toolbar mToolbar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference userProfileRef;
    private String userid, downloadUrl;

    private ProgressDialog loadingBar;
    final static int Gallery_Pick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ed1 = findViewById(R.id.edit_profile_name);
        ed2 = findViewById(R.id.edit_mobile);
        ed3 = findViewById(R.id.edit_address);
        button = findViewById(R.id.edit_profile_button);
        circleImageView = findViewById(R.id.edit_profile_pic);

        mToolbar = findViewById(R.id.edit_profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Profile");

        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userProfileRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Wait");
        loadingBar.setMessage("Profile is fetching");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userid.equals(mAuth.getUid())) {
                    Intent galleryIntent = new Intent();
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntent.setType("image/*");
                    startActivityForResult(galleryIntent, Gallery_Pick);
                }
            }
        });

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
        
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

    }

    private void updateProfile() {
        String name = ed1.getText().toString().trim();
        String mobile = ed2.getText().toString().trim();
        String address = ed3.getText().toString().trim();
        
        if (TextUtils.isEmpty(name)){
            ed1.setError("Please Write Something!!!");
        }
        else if (TextUtils.isEmpty(mobile)){
            ed2.setError("Please Write Something!!!");
        }
        else if (TextUtils.isEmpty(address)){
            ed3.setError("Please Write Something!!!");
        }
        else {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Profile image is updating");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            mDatabase.child("Users").child(userid).child("name").setValue(name);
            mDatabase.child("Users").child(userid).child("mobile_no").setValue(mobile);
            mDatabase.child("Users").child(userid).child("address").setValue(address);
            backToHomeActivity();
        }
    }

    private void backToHomeActivity() {
        Intent intent = new Intent(EditProfileActivity.this, DashboardActivity.class);
        startActivity(intent);
        Toast.makeText(EditProfileActivity.this,"Profile updated Successfully!!!", Toast.LENGTH_LONG).show();
        loadingBar.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            Uri ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK){
                Uri resultUri = result.getUri();
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Profile image is updating");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                final StorageReference filepath = userProfileRef.child(mAuth.getUid() + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(EditProfileActivity.this, "Success", Toast.LENGTH_LONG).show();
                            filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    downloadUrl = task.getResult().toString();
                                    mDatabase.child("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            dataSnapshot.getRef().child("profileImage").setValue(downloadUrl);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
            else {
                Toast.makeText(EditProfileActivity.this,"Fail", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDatabase.child("Users").child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ed1.setText((String) dataSnapshot.child("name").getValue());
                ed2.setText((String) dataSnapshot.child("mobile_no").getValue());
                ed3.setText((String) dataSnapshot.child("address").getValue());
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
