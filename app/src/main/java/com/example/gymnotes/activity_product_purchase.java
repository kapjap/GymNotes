package com.example.gymnotes;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class activity_product_purchase extends AppCompatActivity {

    private TextView titleView, descriptionView, categoryView, priceView;
    private EditText nameEditText, lastNameEditText, emailEditText, cardNumberEditText;
    private Button buyButton;
    private ImageView buttonBackBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_purchase);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        titleView = findViewById(R.id.productTitleBuy);
        descriptionView = findViewById(R.id.productDescriptionBuy);
        categoryView = findViewById(R.id.productCategoryBuy);
        priceView = findViewById(R.id.productPriceBuy);

        nameEditText = findViewById(R.id.editTextName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        emailEditText = findViewById(R.id.editTextEmail);
        cardNumberEditText = findViewById(R.id.editTextCardNumber);
        buyButton = findViewById(R.id.confirmPurchaseButtonBuy);
        buttonBackBuy = findViewById(R.id.buttonBackBuy);

        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String category = getIntent().getStringExtra("category");
        String price = getIntent().getStringExtra("price");
        int productId = getIntent().getIntExtra("id", -1);

        if (title != null) titleView.setText(title);
        if (description != null) descriptionView.setText(description);
        if (category != null) categoryView.setText(category);
        if (price != null) priceView.setText(price);

        buttonBackBuy.setOnClickListener(v -> finish());

        buyButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String cardNumber = cardNumberEditText.getText().toString().trim();

            if (name.isEmpty() || lastName.isEmpty() || email.isEmpty() || cardNumber.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference purchaseRef = database.getReference("purchases");
            String purchaseId = purchaseRef.push().getKey();

            Purchase purchase = new Purchase(name, lastName, email, cardNumber, productId);

            if (purchaseId != null) {
                purchaseRef.child(purchaseId).setValue(purchase)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Purchase saved", Toast.LENGTH_SHORT).show();

                            nameEditText.setText("");
                            lastNameEditText.setText("");
                            emailEditText.setText("");
                            cardNumberEditText.setText("");
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}