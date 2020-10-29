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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddItemsActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ImageButton ib1;
    private EditText ed1, ed2;
    private Button btn;
    private Spinner spinner;

    final static int Gallery_Pick = 1;
    private Uri ImageUri;
    private String name, price;
    private ProgressDialog loadingBar;

    private StorageReference eventReference;
    private DatabaseReference usersRef, itemRef;
    private FirebaseAuth mAuth;

    private String saveDate, saveTime, postName, current_user_id, date;
    private String  downloadUrl, selectedCat = "Nothing Selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_items);

        ib1 = findViewById(R.id.add_item_pic);
        ed1 = findViewById(R.id.add_item_name);
        ed2 = findViewById(R.id.add_item_price);
        btn = findViewById(R.id.add_item_send);
        spinner = findViewById(R.id.add_item__select_category);

        mToolbar = findViewById(R.id.add_item_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Item");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        eventReference = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        itemRef = FirebaseDatabase.getInstance().getReference().child("Items");

        loadingBar = new ProgressDialog(this);

        ArrayAdapter<CharSequence> comAdapter = ArrayAdapter.createFromResource(this, R.array.category, android.R.layout.simple_spinner_item);
        comAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(comAdapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedCat = "Nothing Selected";
                }
                else {
                    selectedCat = spinner.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ib1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {
        name = ed1.getText().toString();
        price = ed2.getText().toString();
        /*if (ImageUri == null){
            Toast.makeText(AddItemsActivity.this,"Please Select Image First!!", Toast.LENGTH_LONG).show();
        }
        else*/ if (TextUtils.isEmpty(name)){
            Toast.makeText(AddItemsActivity.this,"Please Write name First!!", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(price)){
            Toast.makeText(AddItemsActivity.this,"Please Write price First!!", Toast.LENGTH_LONG).show();
        }
        else if (selectedCat.equals("Nothing Selected")){
            Toast.makeText(AddItemsActivity.this,"Please Select category First!!", Toast.LENGTH_LONG).show();
        }
        else {
            loadingBar.setMessage("Item is adding");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImage();
        }
    }

    private void StoringImage() {
        Calendar calDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveDate = currentDate.format(calDate.getTime());

        Calendar calTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveTime = currentTime.format(calTime.getTime());

        postName = current_user_id + saveDate + saveTime;
        if(ImageUri != null) {
            final StorageReference filePath = eventReference.child("Item Events").child(ImageUri.getLastPathSegment() + postName + ".jpg");
            filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                downloadUrl = task.getResult().toString();
                                SavingPostInfo();
                            }
                        });
                    } else {
                        String msg = task.getException().getMessage();
                        Toast.makeText(AddItemsActivity.this, "Error Occured: " + msg, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else{
            downloadUrl = "Nothing is Selected.";
            SavingPostInfo();
        }
    }

    private void SavingPostInfo() {

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    HashMap<String,Object> postMap = new HashMap<String, Object>();
                    postMap.put("uid", current_user_id);
                    postMap.put("date", saveDate);
                    postMap.put("time", saveTime);
                    postMap.put("item_name", name);
                    postMap.put("price", price);
                    postMap.put("item_image", downloadUrl);
                    postMap.put("category", selectedCat);
                    postMap.put("status", "Available");

                    itemRef.child(name).updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful()){

                                        Intent intent = new Intent(AddItemsActivity.this, DashboardActivity.class);
                                        startActivity(intent);
                                        Toast.makeText(AddItemsActivity.this,"Post added Successfully!!!", Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();
                                    }
                                    else {
                                        String msg = task.getException().getMessage();
                                        Toast.makeText(AddItemsActivity.this,"Error Occured: " + msg, Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            ib1.setImageURI(ImageUri);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddItemsActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
