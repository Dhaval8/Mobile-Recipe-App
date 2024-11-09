package com.c019shranth.madproject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.c019shranth.madproject.databinding.ActivityRecipeDetailsBinding;
import com.c019shranth.madproject.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RecipeDetailsActivity extends AppCompatActivity {
    ActivityRecipeDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        if (recipe != null) {
            binding.tvName.setText(recipe.getName() != null ? recipe.getName() : "Unknown");
            binding.tcCategory.setText(recipe.getCategory() != null ? recipe.getCategory() : "Uncategorized");
            binding.tvDescription.setText(recipe.getDescription() != null ? recipe.getDescription() : "No Description");
            binding.tvCalories.setText(String.format("%s Calories", recipe.getCalories() != null ? recipe.getCalories() : "0"));

            Glide.with(RecipeDetailsActivity.this)
                    .load(recipe.getImage())
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher)
                    .into(binding.imgRecipe);

            String currentUserId = FirebaseAuth.getInstance().getUid();
            if (recipe.getAuthorId() != null && recipe.getAuthorId().equalsIgnoreCase(currentUserId)) {
                binding.imgEdit.setVisibility(View.VISIBLE);
                binding.btnDelete.setVisibility(View.VISIBLE);
            } else {
                binding.imgEdit.setVisibility(View.GONE);
                binding.btnDelete.setVisibility(View.GONE);
            }

            int calories = parseCalories(recipe.getCalories());
            if (calories > 500) {
                Toast.makeText(this, "This recipe is high in calories!", Toast.LENGTH_SHORT).show();
            }
        } else {
            binding.tvName.setText("Recipe not found");
            binding.imgRecipe.setVisibility(View.GONE);
        }

        binding.btnDelete.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        ProgressDialog dialog = new ProgressDialog(this);
                        dialog.setMessage("Deleting...");
                        dialog.setCancelable(false);
                        dialog.show();
                        DatabaseReference reference = FirebaseDatabase.getInstance("https://recipe-cf3dd-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Recipes");
                        reference.child(recipe.getId()).removeValue().addOnCompleteListener(task -> {
                            dialog.dismiss();
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Recipe Deleted Successfully", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(this, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        });

        updateDataWithFireBase(recipe != null ? recipe.getId() : null);
    }

    private int parseCalories(String caloriesStr) {
        int calories = 0;
        if (caloriesStr != null) {
            try {
                calories = Integer.parseInt(caloriesStr.replaceAll("[^\\d]", ""));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                calories = -1;
            }
        }
        return calories;
    }

    private void updateDataWithFireBase(String id) {
        if (id == null) return;

        DatabaseReference reference = FirebaseDatabase.getInstance("https://recipe-cf3dd-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Recipes");
        reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    binding.tvName.setText(recipe.getName() != null ? recipe.getName() : "Unknown");
                    binding.tcCategory.setText(recipe.getCategory() != null ? recipe.getCategory() : "Uncategorized");
                    binding.tvDescription.setText(recipe.getDescription() != null ? recipe.getDescription() : "No Description");
                    binding.tvCalories.setText(String.format("%s Calories", recipe.getCalories() != null ? recipe.getCalories() : "0"));
                    Glide.with(RecipeDetailsActivity.this)
                            .load(recipe.getImage())
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .into(binding.imgRecipe);
                } else {
                    Log.e("TAG", "Recipe data is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TAG", "onCancelled: ", error.toException());
            }
        });
    }
}
