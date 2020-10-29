package com.guni.uvpce.ceit.food;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.guni.uvpce.ceit.food.activity.DashboardActivity;
import com.guni.uvpce.ceit.food.model.CartItemList;
import com.guni.uvpce.ceit.food.model.User;

public class SupplierList extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView recyclerView;

    private DatabaseReference usersRef;

    private FirebaseRecyclerAdapter<User, SupplierViewHolder> firebaseRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_list);

        mToolbar = findViewById(R.id.supplier_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Edit Supplier");

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        recyclerView = findViewById(R.id.all_supplier_list);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayAllSupplier();
    }

    private void displayAllSupplier() {
        Query query = usersRef.orderByChild("user_role")
                .startAt("Supplier").endAt("Supplier" + "\uf8ff");

        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();

        firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<User, SupplierViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull SupplierViewHolder holder, int position, @NonNull User model) {
                        final String postKey = getRef(position).getKey();
                        holder.email.setText(model.getEmail());
                        holder.name.setText(model.getName());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(SupplierList.this, EditSupplier.class);
                                intent.putExtra("postKey", postKey);
                                startActivity(intent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public SupplierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_supplier_list_layout, parent, false);
                        SupplierViewHolder itemViewHolder = new SupplierViewHolder(view);
                        return itemViewHolder;
                    }
                };
        recyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class SupplierViewHolder extends RecyclerView.ViewHolder{

        private TextView name, email;
        private View mView;
        public SupplierViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.supplier_name);
            email = itemView.findViewById(R.id.supplier_email);
            mView = itemView;
        }
    }
}
