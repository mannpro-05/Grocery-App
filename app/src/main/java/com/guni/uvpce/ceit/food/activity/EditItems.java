package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.guni.uvpce.ceit.food.R;

import java.util.ArrayList;

public class EditItems extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button btn;
    private Spinner spinner, spinner2;
    private EditText editText;

    private ProgressDialog loadingBar;

    private DatabaseReference usersRef, itemRef;
    private FirebaseAuth mAuth;

    private String current_user_id;
    private ArrayList<String> itemList = new ArrayList<String>();
    private String selectedCat = "Nothing Selected", selectedItem = "Nothing Selected";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_items);

        btn = findViewById(R.id.edit_item_send);
        spinner = findViewById(R.id.edit_item_select_category);
        spinner2 = findViewById(R.id.edit_item_select_item);
        editText = findViewById(R.id.edit_item_price);

        mToolbar = findViewById(R.id.edit_item_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Edit Item");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
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
                            ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(EditItems.this, android.R.layout.simple_spinner_item, itemList);
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

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo() {

        if (selectedItem.equals("Nothing Selected")){
            Toast.makeText(EditItems.this,"Please Select item First!!", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(editText.getText().toString().trim())){
            Toast.makeText(EditItems.this,"Please write price First!!", Toast.LENGTH_LONG).show();
        }
        else if (selectedCat.equals("Nothing Selected")){
            Toast.makeText(EditItems.this,"Please Select category First!!", Toast.LENGTH_LONG).show();
        }
        else {
            confirmation();
        }
    }

    private void confirmation() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to update item?")
                .setCancelable(false)
                .setPositiveButton("UPDATE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                loadingBar.setMessage("Item updating");
                                loadingBar.show();
                                loadingBar.setCanceledOnTouchOutside(true);
                                SavingPostInfo();
                            }
                        })
                .setNegativeButton("CANCLE",
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

    private void SavingPostInfo() {

        String price = editText.getText().toString().trim();
        itemRef.child(selectedItem).child("price").setValue(price);
        Intent intent = new Intent(EditItems.this, DashboardActivity.class);
        startActivity(intent);
        Toast.makeText(EditItems.this,"Item updated Successfully!!!", Toast.LENGTH_LONG).show();
        loadingBar.dismiss();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(EditItems.this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
