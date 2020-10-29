package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.ExpandedMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.guni.uvpce.ceit.food.AddSupplier;
import com.guni.uvpce.ceit.food.DesignByActivity;
import com.guni.uvpce.ceit.food.MainActivity;
import com.guni.uvpce.ceit.food.R;
import com.guni.uvpce.ceit.food.SupplierList;
import com.guni.uvpce.ceit.food.adapter.DashboardMenuAdapter;
import com.guni.uvpce.ceit.food.adapter.ExpandableListAdapter;
import com.guni.uvpce.ceit.food.model.CartItemList;
import com.guni.uvpce.ceit.food.model.DashboardMenu;
import com.guni.uvpce.ceit.food.model.ExpandedMenuModel;
import com.guni.uvpce.ceit.food.model.ItemList;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardActivity extends BaseActivity {

    enum userRole{
        Admin,
        MasterAdmin,
        Customer
    }
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private EditText editText;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar mToolbar;
    private CircleImageView circleImageView;
    private TextView navUserName;
    private ImageButton cart;
    private ProgressDialog loadingBar;
    private Spinner spinner;
    private ArrayList<String> itemList = new ArrayList<String>();

    private Query query;

    private ExpandableListAdapter mMenuAdapter;
    private ExpandableListView expandableList;
    private List<ExpandedMenuModel> listDataHeader;

    private DatabaseReference usersRef, itemsRef, cartRef, orderHistoryRef, orderRef;
    private FirebaseAuth mAuth;
    private int i = 0;

    private String current_user_id, role, selectedItem = "Nothing Selected";

    private FirebaseRecyclerAdapter<ItemList, ItemViewHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerAdapter<CartItemList, SupplierOrderHistoryViewHolder> firebaseRecyclerAdapter1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mToolbar = findViewById(R.id.dashboard_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");
        drawerLayout = findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(DashboardActivity.this, drawerLayout,R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Please Wait");
        loadingBar.setMessage("While account is getting ready");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        expandableList = (ExpandableListView) findViewById(R.id.navigation_submenu);

        navigationView = findViewById(R.id.navigation_menu);
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        circleImageView = (CircleImageView) navView.findViewById(R.id.nav_prof_img);
        navUserName = (TextView) navView.findViewById(R.id.nav_username);
        spinner = findViewById(R.id.dashboard_order_history_filter);
        spinner.setVisibility(View.GONE);
        cart = findViewById(R.id.dashboard_cart);
        editText = findViewById(R.id.dashboard_item_search);

        editText.setVisibility(View.GONE);
        cart.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");
        orderHistoryRef = FirebaseDatabase.getInstance().getReference().child("OrderHistoryByItem");
        orderRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        cartRef = FirebaseDatabase.getInstance().getReference().child("Cart").child(current_user_id);

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    role = dataSnapshot.child("user_role").getValue().toString();
                    if (role.equals("Customer")) {
                        CreateMenu(userRole.Customer);
                        editText.setVisibility(View.VISIBLE);
                        cart.setVisibility(View.VISIBLE);
                    }
                    else if (role.equals("Admin")){
                        CreateMenu(userRole.Admin);
                        editText.setVisibility(View.VISIBLE);
                        cart.setVisibility(View.VISIBLE);
                    }
                    else if (role.equals("Master Admin")){
                        CreateMenu(userRole.MasterAdmin);
                        editText.setVisibility(View.VISIBLE);
                        cart.setVisibility(View.VISIBLE);
                    }
                    else {
                        CreateMenuForSupplier();
                        editText.setVisibility(View.GONE);
                        cart.setVisibility(View.GONE);
                        spinner.setVisibility(View.VISIBLE);
                        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                String s = dataSnapshot.child("Items").getValue().toString();
                                /*for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                    String s = "Item" + i;
                                    itemList.add(snapshot.child(s).getValue().toString());
                                    i++;
                                }*/
                                String[] parts = s.split(",");
                                for (i = 0; i < parts.length; i++){
                                    itemList.add(parts[i]);
                                }
                                ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(DashboardActivity.this, android.R.layout.simple_spinner_item, itemList);
                                itemAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(itemAdapter);
                                spinner.setSelection(0);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                    setExpandableList();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedItem = spinner.getSelectedItem().toString();
                Query query = orderHistoryRef.child(selectedItem);
                displayAllOrderHistory(query);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //navigationView.inflateMenu(R.menu.customer_menu);

        //set post view
        recyclerView = findViewById(R.id.all_item_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("profileImage")) {
                        Picasso.get().load(dataSnapshot.child("profileImage").getValue().toString()).into(circleImageView);
                    }
                    navUserName.setText(dataSnapshot.child("name").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //userMenuSelector(item);
                item.setChecked(true);
                drawerLayout.closeDrawers();
                return true;
            }
        });

        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, CartActivity.class));
                //Toast.makeText(DashboardActivity.this, "Cart", Toast.LENGTH_LONG).show();
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String search = editText.getText().toString().trim();
                if (TextUtils.isEmpty(search)){
                    query = itemsRef.orderByChild("item_name");
                    displayAllItems(query);
                }
                else {
                    query = itemsRef.orderByChild("item_name")
                            .startAt(search).endAt(search + "\uf8ff");
                    displayAllItems(query);
                }
            }
        });
    }

    private void CreateMenuForSupplier() {
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        ExpandedMenuModel item1 = new ExpandedMenuModel();
        item1.setIconName("Profile");
        // Adding data header
        listDataHeader.add(item1);

        item1.getChildList().add("View Profile");
        item1.getChildList().add("Update Profile");

        ExpandedMenuModel item5 = new ExpandedMenuModel();
        item5.setIconName("Design By");
        listDataHeader.add(item5);

        ExpandedMenuModel item6 = new ExpandedMenuModel();
        item6.setIconName("Logout");
        listDataHeader.add(item6);
    }

    private void CreateMenu(userRole role) {
        listDataHeader = new ArrayList<ExpandedMenuModel>();
        ExpandedMenuModel item1 = new ExpandedMenuModel();
        item1.setIconName("Profile");
        // Adding data header
        listDataHeader.add(item1);

        item1.getChildList().add("View Profile");
        item1.getChildList().add("Update Profile");
        switch (role)
        {
            case MasterAdmin:
                prepareListDataForMasterAdmin();
                break;
            case Admin:
                prepareListDataForAdmin();
                break;
            case Customer:
                prepareListDataForCustomer();
                break;
            default:
                break;
        }

        ExpandedMenuModel item5 = new ExpandedMenuModel();
        item5.setIconName("Order History");
        listDataHeader.add(item5);

        ExpandedMenuModel item6 = new ExpandedMenuModel();
        item6.setIconName("Design By");
        listDataHeader.add(item6);

        ExpandedMenuModel item7 = new ExpandedMenuModel();
        item7.setIconName("Logout");
        listDataHeader.add(item7);
    }

    private void prepareListDataForMasterAdmin() {
        ExpandedMenuModel item2 = new ExpandedMenuModel();
        item2.setIconName("Add admin");
        listDataHeader.add(item2);
    }
    private void prepareListDataForAdmin() {
        ExpandedMenuModel item2 = new ExpandedMenuModel();
        item2.setIconName("Items");
        listDataHeader.add(item2);

        item2.getChildList().add("Add Items");
        item2.getChildList().add("Edit Items");
        item2.getChildList().add("Set Availability");

        ExpandedMenuModel item3 = new ExpandedMenuModel();
        item3.setIconName("Add supplier");
        listDataHeader.add(item3);

        ExpandedMenuModel item4 = new ExpandedMenuModel();
        item4.setIconName("Edit supplier");
        listDataHeader.add(item4);
    }
    private void prepareListDataForCustomer() {

        ExpandedMenuModel item2 = new ExpandedMenuModel();
        item2.setIconName("Categories");
        listDataHeader.add(item2);

        item2.getChildList().add("All Items");
        item2.getChildList().add("Vegetables");
        item2.getChildList().add("Fruits");
        item2.getChildList().add("Grains");
        item2.getChildList().add("Dairy");
    }

    private void dashboardMenuAction(DashboardMenu dashboardMenu) {
        menuChildAction(dashboardMenu.group_pos,dashboardMenu.child_pos);
    }

    private boolean menuChildAction(int group_pos, int child_pos) {
        //Log.d("DEBUG", "submenu item clicked");

        switch (role){
            case "Customer" :
                switch (group_pos){
                    case 4:
                        logoutUser();
                        drawerLayout.closeDrawers();
                        break;
                    case 3:
                        startActivity(new Intent(DashboardActivity.this, DesignByActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case 2:
                        startActivity(new Intent(DashboardActivity.this, OrderHistoryActivity.class));
                        //Toast.makeText(DashboardActivity.this, "Order History", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case 1:
                        switch (child_pos) {
                            case 0:
                                query = itemsRef.orderByChild("item_name");
                                displayAllItems(query);
                                //Toast.makeText(DashboardActivity.this, "All Items", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                            case 1:
                                query = itemsRef.orderByChild("category")
                                        .startAt("Vegetables").endAt("Vegetables" + "\uf8ff");
                                displayAllItems(query);
                                //Toast.makeText(DashboardActivity.this, "Vegies", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                            case 2:
                                query = itemsRef.orderByChild("category")
                                        .startAt("Fruits").endAt("Fruits" + "\uf8ff");
                                displayAllItems(query);
                                //Toast.makeText(DashboardActivity.this, "Fruits", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                            case 3:
                                query = itemsRef.orderByChild("category")
                                        .startAt("Grains").endAt("Grains" + "\uf8ff");
                                displayAllItems(query);
                                //Toast.makeText(DashboardActivity.this, "Grains", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                            case 4:
                                query = itemsRef.orderByChild("category")
                                        .startAt("Dairy").endAt("Dairy" + "\uf8ff");
                                displayAllItems(query);
                                //Toast.makeText(DashboardActivity.this, "Dairy", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                        }
                        break;
                    case 0:
                        switch (child_pos){
                            case 0:
                                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                                //Toast.makeText(DashboardActivity.this, "View Profile", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                            case 1:
                                startActivity(new Intent(DashboardActivity.this, EditProfileActivity.class));
                                //Toast.makeText(DashboardActivity.this, "Update Profile", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                        }
                        break;
                }
                break;
            case "Admin" :
                switch (group_pos){
                    case 6:
                        logoutUser();
                        drawerLayout.closeDrawers();
                        break;
                    case 5:
                        startActivity(new Intent(DashboardActivity.this, DesignByActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case 4:
                        startActivity(new Intent(DashboardActivity.this, AdminOrderHistoryActivity.class));
                        //Toast.makeText(DashboardActivity.this, "Order History", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case 3:
                        startActivity(new Intent(DashboardActivity.this, SupplierList.class));
                        //Toast.makeText(DashboardActivity.this, "Add supplier", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case 2:
                        startActivity(new Intent(DashboardActivity.this, AddSupplier.class));
                        //Toast.makeText(DashboardActivity.this, "Add supplier", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case 1:
                        switch (child_pos) {
                            case 0:
                                startActivity(new Intent(DashboardActivity.this, AddItemsActivity.class));
                                //Toast.makeText(DashboardActivity.this, "Add Items", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                            case 1:
                                startActivity(new Intent(DashboardActivity.this, EditItems.class));
                                //Toast.makeText(DashboardActivity.this, "Add Items", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                            case 2:
                                startActivity(new Intent(DashboardActivity.this, SetAvailabilityActivity.class));
                                //Toast.makeText(DashboardActivity.this, "set availability", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                        }
                        break;
                    case 0:
                        switch (child_pos){
                            case 0:
                                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                                //Toast.makeText(DashboardActivity.this, "View Profile", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                            case 1:
                                startActivity(new Intent(DashboardActivity.this, EditProfileActivity.class));
                                //Toast.makeText(DashboardActivity.this, "Update Profile", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                        }
                        break;
                }
                break;
            case "Master Admin":
                switch (group_pos){
                    case 4:
                        logoutUser();
                        drawerLayout.closeDrawers();
                        break;
                    case 3:
                        startActivity(new Intent(DashboardActivity.this, DesignByActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case 2:
                        startActivity(new Intent(DashboardActivity.this, AdminOrderHistoryActivity.class));
                        //Toast.makeText(DashboardActivity.this, "Order History", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case 1:
                        startActivity(new Intent(DashboardActivity.this, AddAdminActivity.class));
                        //Toast.makeText(DashboardActivity.this, "Add admin", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case 0:
                        switch (child_pos){
                            case 0:
                                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                                //Toast.makeText(DashboardActivity.this, "View Profile", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                            case 1:
                                startActivity(new Intent(DashboardActivity.this, EditProfileActivity.class));
                                //Toast.makeText(DashboardActivity.this, "Update Profile", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                        }
                        break;
                }
                break;
            case "Supplier":
                switch (group_pos){
                    case 2:
                        logoutUser();
                        drawerLayout.closeDrawers();
                        break;
                    case 1:
                        startActivity(new Intent(DashboardActivity.this, DesignByActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case 0:
                        switch (child_pos){
                            case 0:
                                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                                //Toast.makeText(DashboardActivity.this, "View Profile", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                            case 1:
                                startActivity(new Intent(DashboardActivity.this, EditProfileActivity.class));
                                //Toast.makeText(DashboardActivity.this, "Update Profile", Toast.LENGTH_LONG).show();
                                drawerLayout.closeDrawers();
                                break;
                        }
                        break;
                }
                break;
        }

        /* switch (group_pos){
            case 4:
                logoutUser();
                drawerLayout.closeDrawers();
                break;
            case 3:
                startActivity(new Intent(DashboardActivity.this, DesignByActivity.class));
                drawerLayout.closeDrawers();
                break;
            case 2:
                if (role.equals("Admin") || role.equals("Master Admin")) {
                    startActivity(new Intent(DashboardActivity.this, AdminOrderHistoryActivity.class));
                    //Toast.makeText(DashboardActivity.this, "Order History", Toast.LENGTH_LONG).show();
                    drawerLayout.closeDrawers();
                }
                else {
                    startActivity(new Intent(DashboardActivity.this, OrderHistoryActivity.class));
                    //Toast.makeText(DashboardActivity.this, "Order History", Toast.LENGTH_LONG).show();
                    drawerLayout.closeDrawers();
                }
                break;
            case 1:
                if (role.equals("Customer")) {
                    switch (child_pos) {
                        case 0:
                            query = itemsRef.orderByChild("item_name");
                            displayAllItems(query);
                            //Toast.makeText(DashboardActivity.this, "All Items", Toast.LENGTH_LONG).show();
                            drawerLayout.closeDrawers();
                            break;
                        case 1:
                            query = itemsRef.orderByChild("category")
                                    .startAt("Vegetables").endAt("Vegetables" + "\uf8ff");
                            displayAllItems(query);
                            //Toast.makeText(DashboardActivity.this, "Vegies", Toast.LENGTH_LONG).show();
                            drawerLayout.closeDrawers();
                            break;
                        case 2:
                            query = itemsRef.orderByChild("category")
                                    .startAt("Fruits").endAt("Fruits" + "\uf8ff");
                            displayAllItems(query);
                            //Toast.makeText(DashboardActivity.this, "Fruits", Toast.LENGTH_LONG).show();
                            drawerLayout.closeDrawers();
                            break;
                        case 3:
                            query = itemsRef.orderByChild("category")
                                    .startAt("Grains").endAt("Grains" + "\uf8ff");
                            displayAllItems(query);
                            //Toast.makeText(DashboardActivity.this, "Grains", Toast.LENGTH_LONG).show();
                            drawerLayout.closeDrawers();
                            break;
                        case 4:
                            query = itemsRef.orderByChild("category")
                                    .startAt("Dairy").endAt("Dairy" + "\uf8ff");
                            displayAllItems(query);
                            //Toast.makeText(DashboardActivity.this, "Dairy", Toast.LENGTH_LONG).show();
                            drawerLayout.closeDrawers();
                            break;
                    }
                }
                else if (role.equals("Admin")){
                    switch (child_pos) {
                        case 0:
                            startActivity(new Intent(DashboardActivity.this, AddItemsActivity.class));
                            //Toast.makeText(DashboardActivity.this, "Add Items", Toast.LENGTH_LONG).show();
                            drawerLayout.closeDrawers();
                            break;
                        case 1:
                            startActivity(new Intent(DashboardActivity.this, EditItems.class));
                            //Toast.makeText(DashboardActivity.this, "Add Items", Toast.LENGTH_LONG).show();
                            drawerLayout.closeDrawers();
                            break;
                        case 2:
                            startActivity(new Intent(DashboardActivity.this, SetAvailabilityActivity.class));
                            //Toast.makeText(DashboardActivity.this, "set availability", Toast.LENGTH_LONG).show();
                            drawerLayout.closeDrawers();
                            break;
                    }
                }
                else {
                    switch (child_pos) {
                        case 0:
                            startActivity(new Intent(DashboardActivity.this, AddAdminActivity.class));
                            //Toast.makeText(DashboardActivity.this, "Add admin", Toast.LENGTH_LONG).show();
                            drawerLayout.closeDrawers();
                            break;
                        case 1:
                            startActivity(new Intent(DashboardActivity.this, AddSupplier.class));
                            //Toast.makeText(DashboardActivity.this, "Add supplier", Toast.LENGTH_LONG).show();
                            drawerLayout.closeDrawers();
                            break;
                    }
                }
                break;

            case 0:
                switch (child_pos){
                    case 0:
                        startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                        //Toast.makeText(DashboardActivity.this, "View Profile", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                    case 1:
                        startActivity(new Intent(DashboardActivity.this, EditProfileActivity.class));
                        //Toast.makeText(DashboardActivity.this, "Update Profile", Toast.LENGTH_LONG).show();
                        drawerLayout.closeDrawers();
                        break;
                }
                break;
        }*/
        return false;
    }

    private void setExpandableList() {
        mMenuAdapter = new ExpandableListAdapter(this, listDataHeader, expandableList);

        // setting list adapter
        expandableList.setAdapter(mMenuAdapter);

        expandableList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
                int group_pos = (int) mMenuAdapter.getGroupId(i);
                int child_pos = (int) mMenuAdapter.getChildId(i, i1);
                return menuChildAction(group_pos,child_pos);
            }
        });
        expandableList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                //Log.d("DEBUG", "heading clicked");
                int group_pos = (int) mMenuAdapter.getGroupId(i);
                menuChildAction(group_pos,-1);
                return false;
            }
        });
    }

    private void logoutUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to logout?")
                .setCancelable(false)
                .setPositiveButton("LOGOUT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                FirebaseAuth.getInstance().signOut();
                                finish();
                                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
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



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to exit?")
                .setCancelable(false)
                .setPositiveButton("EXIT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                finishAffinity();
                                System.exit(0);
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

    @Override
    protected void onStart() {
        super.onStart();
        setDashboardByRole();
    }

    private void setDashboardByRole() {

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    role = dataSnapshot.child("user_role").getValue().toString();
                    if (role.equals("Supplier")) {
                        Query query = orderHistoryRef.child(selectedItem);
                        displayAllOrderHistory(query);
                        Toast.makeText(DashboardActivity.this, role, Toast.LENGTH_LONG).show();
                    }
                    else {
                        query = itemsRef.orderByChild("item_name");
                        //Log.i("Dashboard", String.valueOf(query));
                        displayAllItems(query);
                    }
                    loadingBar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void displayAllOrderHistory(Query query) {
        FirebaseRecyclerOptions<CartItemList> options =
                new FirebaseRecyclerOptions.Builder<CartItemList>()
                        .setQuery(query, CartItemList.class)
                        .build();

        firebaseRecyclerAdapter1 =
                new FirebaseRecyclerAdapter<CartItemList, SupplierOrderHistoryViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final SupplierOrderHistoryViewHolder holder, int position, @NonNull CartItemList model) {
                        final String postKey = getRef(position).getKey();
                        holder.total.setText("Total: " + model.getNumber() + "kg");
                        holder.time.setText(" " + model.getTime());
                        holder.date.setText(model.getDate());

                        orderRef.child(postKey).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    holder.orderStatus.setText(dataSnapshot.child("order_status").getValue().toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        final String userId = model.getUid();

                        usersRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Object ss = dataSnapshot.child(userId).child("name").getValue();
                                if(ss != null) {
                                    String s = ss.toString();
                                    holder.name.setText(s);
                                }
                                else{
                                    holder.name.setText("None");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public SupplierOrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.all_suplier_order_history_layout, parent, false);
                        SupplierOrderHistoryViewHolder itemViewHolder = new SupplierOrderHistoryViewHolder(view);
                        return itemViewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter1);
        firebaseRecyclerAdapter1.startListening();
    }

    private void displayAllItems(Query query) {
        FirebaseRecyclerOptions<ItemList> options =
                new FirebaseRecyclerOptions.Builder<ItemList>()
                .setQuery(query, ItemList.class)
                .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<ItemList, ItemViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ItemViewHolder holder, int position, @NonNull final ItemList model) {
                        final String postKey = getRef(position).getKey();
                        final int price = Integer.parseInt(model.getPrice());

                        holder.category.setText(model.getCategory());
                        holder.itemName.setText(model.getItem_name());
                        holder.price.setText("Price: " +model.getPrice() + "₹" + " (Per kg.)");
                        Picasso.get().load(model.getItem_image()).into(holder.imageView);

                        holder.plus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int n = Integer.parseInt(holder.number.getText().toString());
                                if (n > 0){
                                    n += 1;
                                    holder.number.setText(String.valueOf(n));
                                }
                            }
                        });

                        holder.minus.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                int n = Integer.parseInt(holder.number.getText().toString());
                                if (n > 1){
                                    n -= 1;
                                    holder.number.setText(String.valueOf(n));
                                }
                            }
                        });

                        if (model.getStatus().equals("Available")) {
                            holder.status.setText("In stock");

                            holder.button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    int n = Integer.parseInt(holder.number.getText().toString());
                                    int p = price;
                                    int total = n * p;
                                    HashMap hashMap = new HashMap();
                                    hashMap.put("category", model.getCategory());
                                    hashMap.put("item_name", model.getItem_name());
                                    hashMap.put("item_image", model.getItem_image());
                                    hashMap.put("price", model.getPrice());
                                    hashMap.put("number", String.valueOf(n));
                                    hashMap.put("total", String.valueOf(total));

                                    cartRef.child(model.getItem_name()).updateChildren(hashMap)
                                            .addOnCompleteListener(new OnCompleteListener() {
                                                @Override
                                                public void onComplete(@NonNull Task task) {
                                                    if (task.isSuccessful()) {
                                                        holder.number.setText("1");
                                                        Toast.makeText(DashboardActivity.this, "Item Added SuccessFully!!!", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        String msg = task.getException().getMessage();
                                                        Toast.makeText(DashboardActivity.this, "Error Occured: " + msg, Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                }
                            });
                        }
                        else {
                            holder.status.setText(model.getStatus());

                            holder.button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    holder.number.setText("1");
                                    Toast.makeText(DashboardActivity.this, "Soryy, Item is out of stock!!!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }

                    @NonNull
                    @Override
                    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.all_items_layout, parent, false);
                        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
                        return itemViewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;
        private TextView category, itemName, price, minus, plus, number, status;
        private Button button;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.item_image);
            category = itemView.findViewById(R.id.item_category);
            itemName = itemView.findViewById(R.id.item_name);
            price = itemView.findViewById(R.id.item_price);
            minus = itemView.findViewById(R.id.item_quantity_minus);
            plus = itemView.findViewById(R.id.item_quantity_plus);
            number = itemView.findViewById(R.id.item_quantity_number);
            button = itemView.findViewById(R.id.item_add_to_cart);
            status = itemView.findViewById(R.id.item_status);

        }
    }

    public static class SupplierOrderHistoryViewHolder extends RecyclerView.ViewHolder{

        private TextView date, time, total, orderStatus, name;
        private View mView;

        public SupplierOrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.supplier_oh_date);
            time = itemView.findViewById(R.id.supplier_oh_time);
            total = itemView.findViewById(R.id.supplier_oh_total_amount);
            orderStatus = itemView.findViewById(R.id.supplier_oh_order_status);
            name = itemView.findViewById(R.id.supplier_oh_name);
            mView = itemView;
        }
    }
}
