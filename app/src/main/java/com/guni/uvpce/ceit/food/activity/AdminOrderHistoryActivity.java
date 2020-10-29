package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class AdminOrderHistoryActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    private DatabaseReference usersRef, itemsRef, orderRef;
    private FirebaseAuth mAuth;

    private String current_user_id;

    private FirebaseRecyclerAdapter<CartItemList, AdminOrderHistoryViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_history);

        toolbar = findViewById(R.id.admin_oh_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Order History");

        recyclerView = findViewById(R.id.all_item_admin_oh_list);
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
        Query query = orderRef.orderByChild("date");
        //Log.i("Cart", String.valueOf(query));
        displayOrderHistoryItems(query);
    }

    private void displayOrderHistoryItems(Query query) {
        FirebaseRecyclerOptions<CartItemList> options =
                new FirebaseRecyclerOptions.Builder<CartItemList>()
                        .setQuery(query, CartItemList.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<CartItemList, AdminOrderHistoryViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final AdminOrderHistoryViewHolder holder, int position, @NonNull CartItemList model) {
                        final String postKey = getRef(position).getKey();
                        holder.total.setText("Total: " + model.getTotal() + "₹");
                        holder.time.setText(" " + model.getTime());
                        holder.date.setText(model.getDate());
                        holder.orderStatus.setText(model.getOrder_status());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(AdminOrderHistoryActivity.this, SingleOrderHistoryActivity.class);
                                intent.putExtra("postKey", postKey);
                                startActivity(intent);
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

                        holder.button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                callPermission(userId);
                            }
                        });

                        if (model.getOrder_status().equals("Pending")) {
                            holder.button2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    confirmPermission(postKey);
                                }
                            });
                        }
                        else {
                            holder.button2.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(AdminOrderHistoryActivity.this, "This order is already confirmed or cancled", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

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

                        if (model.getOrder_status().equals("Pending")){
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
                                    Toast.makeText(AdminOrderHistoryActivity.this, "You can't cancle this order", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }

                    @NonNull
                    @Override
                    public AdminOrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_admin_order_history_layout, parent, false);
                        AdminOrderHistoryViewHolder itemViewHolder = new AdminOrderHistoryViewHolder(view);
                        return itemViewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void confirmPermission(final String postKey) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to confirm the order?")
                .setCancelable(false)
                .setPositiveButton("CONFIRM",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                orderRef.child(postKey).child("order_status").setValue("Confirmed");
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

    private void callPermission(final String userId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to call?")
                .setCancelable(false)
                .setPositiveButton("CALL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                usersRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String mobile = dataSnapshot.child(userId).child("mobile_no").getValue().toString();
                                        Intent intent = new Intent(Intent.ACTION_DIAL);
                                        intent.setData(Uri.parse("tel:" + mobile));
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

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

    private void deleteItem(final String postKey) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Warning");
        alertDialogBuilder
                .setMessage("Do you really want to cancle order?")
                .setCancelable(false)
                .setPositiveButton("CANCLE",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // what to do if YES is tapped
                                orderRef.child(postKey).child("order_status").setValue("Cancled");
                            }
                        })
                .setNegativeButton("NO",
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

    public static class AdminOrderHistoryViewHolder extends RecyclerView.ViewHolder{

        private TextView date, time, total, itemno, orderStatus, name;
        private ImageButton imageButton;
        private Button button, button2;
        private View mView;

        public AdminOrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.admin_oh_date);
            time = itemView.findViewById(R.id.admin_oh_time);
            total = itemView.findViewById(R.id.admin_oh_total_amount);
            itemno = itemView.findViewById(R.id.admin_oh_total_items);
            imageButton = itemView.findViewById(R.id.admin_oh_delete_item);
            orderStatus = itemView.findViewById(R.id.admin_oh_order_status);
            button = itemView.findViewById(R.id.admin_oh_call);
            button2 = itemView.findViewById(R.id.admin_oh_confirm);
            name = itemView.findViewById(R.id.admin_oh_name);
            mView = itemView;
        }
    }
}
