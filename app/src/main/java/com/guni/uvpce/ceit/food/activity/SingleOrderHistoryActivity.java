package com.guni.uvpce.ceit.food.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

public class SingleOrderHistoryActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private Toolbar toolbar;

    private DatabaseReference usersRef, itemsRef, orderRef;
    private FirebaseAuth mAuth;

    private String current_user_id, postKey;

    private FirebaseRecyclerAdapter<CartItemList, SingleOrderItemListViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_order_history);

        toolbar = findViewById(R.id.single_order_history_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Order History");

        recyclerView = findViewById(R.id.all_item_single_order_history_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        postKey = getIntent().getExtras().get("postKey").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        itemsRef = FirebaseDatabase.getInstance().getReference().child("Items");
        orderRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(postKey).child("Items");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Query query = orderRef.orderByChild("item_name");
        //Log.i("Cart", String.valueOf(query));
        displayCartItems(query);
    }

    private void displayCartItems(Query query) {

        FirebaseRecyclerOptions<CartItemList> options =
                new FirebaseRecyclerOptions.Builder<CartItemList>()
                        .setQuery(orderRef, CartItemList.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<CartItemList, SingleOrderItemListViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final SingleOrderItemListViewHolder holder, int position, @NonNull CartItemList model) {
                        final String postKey = getRef(position).getKey();

                        holder.itemName.setText(model.getItem_name());
                        holder.number.setText("Quantity: " + model.getNumber() + " kg.");
                        final int n = Integer.parseInt(model.getNumber());
                        itemsRef.child(holder.itemName.getText().toString()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    String cat = dataSnapshot.child("category").getValue().toString();
                                    int p = Integer.parseInt(dataSnapshot.child("price").getValue().toString());
                                    if (dataSnapshot.child("item_image").exists()){
                                        Picasso.get().load(dataSnapshot.child("item_image").getValue().toString()).into(holder.imageView);
                                    }
                                    int t = n*p;
                                    holder.category.setText(cat);
                                    holder.price.setText("Price: " +String.valueOf(p) + "₹" + " (Per kg.)");
                                    holder.total.setText("Total: " +String.valueOf(t) + "₹");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public SingleOrderItemListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_items_single_order_history, parent, false);
                        SingleOrderItemListViewHolder itemViewHolder = new SingleOrderItemListViewHolder(view);
                        return itemViewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    public static class SingleOrderItemListViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView category, itemName, price, number, total;

        public SingleOrderItemListViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.oh_item_image);
            category = itemView.findViewById(R.id.oh_item_category);
            itemName = itemView.findViewById(R.id.oh_item_name);
            price = itemView.findViewById(R.id.oh_item_price);
            number = itemView.findViewById(R.id.oh_item_quantity_number);
            total = itemView.findViewById(R.id.oh_item_total);
        }
    }
}
