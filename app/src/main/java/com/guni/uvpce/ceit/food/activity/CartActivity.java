package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.guni.uvpce.ceit.food.R;
import com.guni.uvpce.ceit.food.model.CartItemList;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class CartActivity extends BaseActivity {

    private TextView textView;
    private Button button;
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    private DatabaseReference usersRef, itemsRef, cartRef, orderRef, totalItemRef, orderHistoryRef;
    private FirebaseAuth mAuth;

    private String current_user_id, saveDate, saveTime, postName;

    private int total_amount = 0;
    private int ino = 0;

    private ArrayList<String> itemName = new ArrayList<String>();
    private ArrayList<String> itemCount = new ArrayList<String>();

    private FirebaseRecyclerAdapter<CartItemList, CartItemListViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        textView = findViewById(R.id.cart_total_amount);
        button = findViewById(R.id.cart_checkout);
        toolbar = findViewById(R.id.cart_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Cart");

        recyclerView = findViewById(R.id.all_item_cart_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");
        cartRef = FirebaseDatabase.getInstance().getReference().child("Cart");
        orderRef = FirebaseDatabase.getInstance().getReference().child("Orders");
        totalItemRef = FirebaseDatabase.getInstance().getReference().child("Orders Total Items");
        orderHistoryRef = FirebaseDatabase.getInstance().getReference().child("OrderHistoryByItem");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (total_amount == 0) {
                    Toast.makeText(CartActivity.this, "Nothing in cart!!!", Toast.LENGTH_LONG).show();
                }
                else {
                    checkoutConfirm();
                }
            }
        });
    }

    private void checkoutConfirm() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to place the order?")
                .setCancelable(false)
                .setPositiveButton("PLACE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                checkout();
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

    private void checkout() {
        Calendar calDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveDate = currentDate.format(calDate.getTime());

        Calendar calTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveTime = currentTime.format(calTime.getTime());

        postName = current_user_id + saveDate + saveTime;

        for (int i = 0; i < itemName.size(); i++){
            HashMap hashMap = new HashMap();
            ino = Integer.parseInt(itemCount.get(i));

            hashMap.put("item_name", itemName.get(i));
            hashMap.put("number", itemCount.get(i));

            orderRef.child(postName).child("Items").child(itemName.get(i)).updateChildren(hashMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            Toast.makeText(CartActivity.this, "Order Placed SuccessFully", Toast.LENGTH_LONG).show();
                        }
                    });
            hashMap.put("uid", current_user_id);
            hashMap.put("date", saveDate);
            hashMap.put("time", saveTime);
            orderHistoryRef.child(itemName.get(i)).child(postName)
                    .updateChildren(hashMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            Toast.makeText(CartActivity.this, "Order Placed SuccessFully", Toast.LENGTH_LONG).show();
                        }
                    });
        }
        orderRef.child(postName).child("total").setValue(String.valueOf(total_amount));
        orderRef.child(postName).child("uid").setValue(current_user_id);
        orderRef.child(postName).child("date").setValue(saveDate);
        orderRef.child(postName).child("order_status").setValue("Pending");
        orderRef.child(postName).child("time").setValue(saveTime);
        cartRef.child(current_user_id).removeValue();
        textView.setText("Not any item is added to cart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = cartRef.child(current_user_id).orderByChild("total");
        Log.i("Cart", String.valueOf(query));
        displayCartItems(query);
    }

    private void displayCartItems(Query query) {

        FirebaseRecyclerOptions<CartItemList> options =
                new FirebaseRecyclerOptions.Builder<CartItemList>()
                        .setQuery(query, CartItemList.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<CartItemList, CartItemListViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final CartItemListViewHolder holder, int position, @NonNull CartItemList model) {
                        final String postKey = getRef(position).getKey();
                        total_amount += Integer.parseInt(model.getTotal());
                        textView.setText("Total amount: " + String.valueOf(total_amount) + "₹");
                        holder.category.setText(model.getCategory());
                        holder.itemName.setText(model.getItem_name());
                        holder.price.setText("Price: " +model.getPrice() + "₹" + " (Per kg.)");
                        holder.total.setText("Total: " +model.getTotal() + "₹");
                        holder.number.setText("Quantity: " + model.getNumber() + " kg.");
                        Picasso.get().load(model.getItem_image()).into(holder.imageView);
                        itemName.add(holder.itemName.getText().toString());
                        itemCount.add(model.getNumber());

                        holder.imageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                cartRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.child(holder.itemName.getText().toString()).exists()){
                                            total_amount -= Integer.parseInt(dataSnapshot.child(holder.itemName.getText().toString()).child("total").getValue().toString());
                                            if (total_amount > 0) {
                                                textView.setText("Total amount: " + String.valueOf(total_amount) + "₹");
                                            }
                                            else {
                                                textView.setText("Not any item is added to cart");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                                deleteItem(postKey);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public CartItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_items_cart_layout, parent, false);
                        CartItemListViewHolder itemViewHolder = new CartItemListViewHolder(view);
                        return itemViewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void deleteItem(final String postKey) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to remove item?")
                .setCancelable(false)
                .setPositiveButton("REMOVE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                cartRef.child(current_user_id).child(postKey).removeValue();
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
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class CartItemListViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView category, itemName, price, number, total;
        private ImageButton imageButton;

        public CartItemListViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.cart_item_image);
            category = itemView.findViewById(R.id.cart_item_category);
            itemName = itemView.findViewById(R.id.cart_item_name);
            price = itemView.findViewById(R.id.cart_item_price);
            number = itemView.findViewById(R.id.cart_item_quantity_number);
            total = itemView.findViewById(R.id.cart_item_total);
            imageButton = itemView.findViewById(R.id.cart_delete_item);
        }
    }
}
