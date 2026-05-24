package com.example.gymnotes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class store_sports extends AppCompatActivity {

    TextView title1, price1, category1, description1;
    TextView title2, price2, category2, description2;
    TextView title3, price3, category3, description3;
    TextView title4, price4, category4, description4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_store_sports);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        title1 = findViewById(R.id.title1);
        price1 = findViewById(R.id.price1);
        category1 = findViewById(R.id.category1);
        description1 = findViewById(R.id.description1);

        title2 = findViewById(R.id.title2);
        price2 = findViewById(R.id.price2);
        category2 = findViewById(R.id.category2);
        description2 = findViewById(R.id.description2);

        title3 = findViewById(R.id.title3);
        price3 = findViewById(R.id.price3);
        category3 = findViewById(R.id.category3);
        description3 = findViewById(R.id.description3);

        title4 = findViewById(R.id.title4);
        price4 = findViewById(R.id.price4);
        category4 = findViewById(R.id.category4);
        description4 = findViewById(R.id.description4);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference productsRef = database.getReference("products");

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    setCard(snapshot.child("product1"), title1, price1, category1, description1);
                    setCard(snapshot.child("product2"), title2, price2, category2, description2);
                    setCard(snapshot.child("product3"), title3, price3, category3, description3);
                    setCard(snapshot.child("product4"), title4, price4, category4, description4);
                } else {
                    Toast.makeText(store_sports.this, "Нет данных о товарах", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(store_sports.this, "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);

        button1.setOnClickListener(v -> openProductScreen(1, title1, price1, category1, description1));
        button2.setOnClickListener(v -> openProductScreen(2, title2, price2, category2, description2));
        button3.setOnClickListener(v -> openProductScreen(3, title3, price3, category3, description3));
        button4.setOnClickListener(v -> openProductScreen(4, title4, price4, category4, description4));
    }

    private void setCard(DataSnapshot productSnap, TextView title, TextView price, TextView category, TextView description) {
        String name = productSnap.child("title").getValue(String.class);
        Double priceVal = productSnap.child("price").getValue(Double.class);
        String cat = productSnap.child("category").getValue(String.class);
        String desc = productSnap.child("description").getValue(String.class);

        if (name != null) title.setText(name);
        if (priceVal != null) price.setText("$" + String.format("%.2f", priceVal));
        if (cat != null) category.setText("Category: " + cat);
        if (desc != null) description.setText(desc);
    }

    private void openProductScreen(int productId, TextView titleView, TextView priceView, TextView categoryView, TextView descriptionView) {
        String title = titleView.getText().toString().trim();
        String description = descriptionView.getText().toString().trim();
        String category = categoryView.getText().toString().trim();
        String price = priceView.getText().toString().trim();

        Intent intent = new Intent(this, activity_product_purchase.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("category", category);
        intent.putExtra("price", price);
        intent.putExtra("id", productId);
        startActivity(intent);
    }
}