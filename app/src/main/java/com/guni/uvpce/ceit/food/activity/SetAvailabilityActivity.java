package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.guni.uvpce.ceit.food.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SetAvailabilityActivity extends BaseActivity {
    private Toolbar mToolbar;
    private Button btn;
    private Spinner spinner, spinner2, spinner3;

    private ProgressDialog loadingBar;

    private DatabaseReference usersRef, itemRef;
    private FirebaseAuth mAuth;

    private String current_user_id;
    private ArrayList<String> itemList = new ArrayList<String>();
    private String selectedCat = "Nothing Selected", selectedItem = "Nothing Selected", selectedStatus = "Nothing Selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_availability);

        btn = findViewById(R.id.set_availability_send);
        spinner = findViewById(R.id.set_availability_select_category);
        spinner2 = findViewById(R.id.set_availability_select_item);
        spinner3 = findViewById(R.id.set_availability_select_status);

        mToolbar = findViewById(R.id.set_availability_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Item");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        itemRef = FirebaseDatabase.getInstance().getReference().child("Items");

        loadingBar = new ProgressDialog(this);

        ArrayAdapter<CharSequence> comAdapter = ArrayAdapter.createFromResource(this, R.array.category, android.R.layout.simple_spinner_item);
        comAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(comAdapter);
        spinner.setSelection(0);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this, R.array.status, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(statusAdapter);
        spinner3.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedCat = "Nothing Selected";
                }
                else {
                    selectedCat = spinner.getSelectedItem().toString();
                    itemList.clear();

                    itemRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            itemList.add("Select item");
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                if (selectedCat.equals(snapshot.child("category").getValue().toString())){
                                    itemList.add(snapshot.child("item_name").getValue().toString());
                                }
                            }
                            ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(SetAvailabilityActivity.this, android.R.layout.simple_spinner_item, itemList);
                            itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner2.setAdapter(itemAdapter);
                            spinner2.setSelection(0);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedItem = "Nothing Selected";
                }
                else {
                    selectedItem = spinner2.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0){
                    selectedStatus = "Nothing Selected";
                }
                else {
                    selectedStatus = spinner3.getSelectedItem().toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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

        if (selectedItem.equals("Nothing Selected")){
            Toast.makeText(SetAvailabilityActivity.this,"Please Select item First!!", Toast.LENGTH_LONG).show();
        }
        else if (selectedStatus.equals("Nothing Selected")){
            Toast.makeText(SetAvailabilityActivity.this,"Please Select Status First!!", Toast.LENGTH_LONG).show();
        }
        else if (selectedCat.equals("Nothing Selected")){
            Toast.makeText(SetAvailabilityActivity.this,"Please Select category First!!", Toast.LENGTH_LONG).show();
        }
        else {
            loadingBar.setMessage("Setting Availability status");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            SavingPostInfo();
        }
    }

    private void SavingPostInfo() {

        itemRef.child(selectedItem).child("status").setValue(selectedStatus);
        Intent intent = new Intent(SetAvailabilityActivity.this, DashboardActivity.class);
        startActivity(intent);
        Toast.makeText(SetAvailabilityActivity.this,"Set availability Successfully!!!", Toast.LENGTH_LONG).show();
        loadingBar.dismiss();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SetAvailabilityActivity.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
