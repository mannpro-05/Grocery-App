package com.guni.uvpce.ceit.food;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.guni.uvpce.ceit.food.activity.DashboardActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class EditSupplier extends AppCompatActivity {

    private LinearLayout linearLayout;
    private Toolbar mToolbar;
    private Button button;

    private DatabaseReference usersRef, itemsRef;

    private String postKey;

    private int itemCount = 0;

    private ArrayList<String> itemList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_supplier);

        mToolbar = findViewById(R.id.register_edit_supplier_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Register Supplier");
        button = findViewById(R.id.register_edit_supplier_button);

        linearLayout = findViewById(R.id.register_edit_supplier_item_list);

        postKey = getIntent().getExtras().get("postKey").toString();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");

        usersRef.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String s = dataSnapshot.child("Items").getValue().toString();
                    String[] parts = s.split(",");
                    for (int i = 0; i < parts.length; i++){
                        itemList.add(parts[i]);
                    }

                    itemsRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                final String item = snapshot.child("item_name").getValue().toString();
                                CheckBox checkBox = new CheckBox(EditSupplier.this);
                                checkBox.setText(item);
                                if (itemList.contains(item)){
                                    checkBox.setChecked(true);
                                    itemCount += 1;
                                }
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

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUser();
            }
        });
    }

    private void updateUser() {
        if (itemCount == 0){
            return;
        }

        String s = "";
        for (int i = 0; i < itemList.size(); i++){
            s += itemList.get(i) + ",";
        }

        usersRef.child(postKey).child("Items").setValue(s).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    startActivity(new Intent(EditSupplier.this, DashboardActivity.class));
                    Toast.makeText(EditSupplier.this, "Supplier updated successfully", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
