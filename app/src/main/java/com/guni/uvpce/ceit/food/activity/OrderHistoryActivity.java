package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.guni.uvpce.ceit.food.R;
import com.guni.uvpce.ceit.food.model.CartItemList;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class OrderHistoryActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    private DatabaseReference usersRef, itemsRef, orderRef;
    private FirebaseAuth mAuth;

    private String current_user_id;

    private FirebaseRecyclerAdapter<CartItemList, OrderHistoryViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        toolbar = findViewById(R.id.order_history_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Order History");

        recyclerView = findViewById(R.id.all_item_order_history_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");
        orderRef = FirebaseDatabase.getInstance().getReference().child("Orders");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = orderRef.orderByChild("uid")
                .startAt(current_user_id).endAt(current_user_id + "\uf8ff");
        //Log.i("Cart", String.valueOf(query));
        displayOrderHistoryItems(query);
    }

    private void displayOrderHistoryItems(Query query) {
        FirebaseRecyclerOptions<CartItemList> options =
                new FirebaseRecyclerOptions.Builder<CartItemList>()
                        .setQuery(query, CartItemList.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<CartItemList, OrderHistoryViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final OrderHistoryViewHolder holder, int position, @NonNull CartItemList model) {
                        final String postKey = getRef(position).getKey();
                        holder.total.setText("Total: " +model.getTotal() + "₹");
                        holder.time.setText(" " + model.getTime());
                        holder.date.setText(model.getDate());
                        holder.orderStatus.setText(model.getOrder_status());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(OrderHistoryActivity.this, SingleOrderHistoryActivity.class);
                                intent.putExtra("postKey", postKey);
                                startActivity(intent);
                            }
                        });

                        orderRef.child(postKey).child("Items").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    long l = dataSnapshot.getChildrenCount();
                                    String s = String.valueOf(l);
                                    holder.itemno.setText(s + " Items");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        Calendar calDate = Calendar.getInstance();
                        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
                        String saveDate = currentDate.format(calDate.getTime());

                        if (saveDate.equals(model.getDate()) && model.getOrder_status().equals("Pending")){
                            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteItem(postKey);
                                }
                            });
                        }
                        else {
                            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(OrderHistoryActivity.this, "You can't cancle this order", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }

                    @NonNull
                    @Override
                    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_order_history_layout, parent, false);
                        OrderHistoryViewHolder itemViewHolder = new OrderHistoryViewHolder(view);
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
                                orderRef.child(postKey).child("order_status").setValue("Cancled");
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

    public static class OrderHistoryViewHolder extends RecyclerView.ViewHolder{

        private TextView date, time, total, itemno, orderStatus;
        private ImageButton imageButton;
        private View mView;

        public OrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.order_history_date);
            time = itemView.findViewById(R.id.order_history_time);
            total = itemView.findViewById(R.id.order_history_total_amount);
            itemno = itemView.findViewById(R.id.order_history_total_items);
            imageButton = itemView.findViewById(R.id.order_history_delete_item);
            orderStatus = itemView.findViewById(R.id.order_history_order_status);
            mView = itemView;
        }
    }

}
