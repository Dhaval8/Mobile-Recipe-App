package com.c019shranth.madproject;

import static java.lang.System.currentTimeMillis;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.c019shranth.madproject.databinding.ActivityAddRecipeBinding;
import com.c019shranth.madproject.models.Category;
import com.c019shranth.madproject.models.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddRecipeActivity extends AppCompatActivity {

    ActivityAddRecipeBinding binding;
    private boolean isImageSelected = false;
    private ProgressDialog dialog;
    boolean isEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadCategories();
        binding.btnAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
            }
        });

        binding.imgRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
        isEdit = getIntent().getBooleanExtra("isEdit",false);
        if(isEdit){
            editRecipe();


        }

        if (binding.getRoot() != null) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void editRecipe() {
        Recipe recipe = (Recipe) getIntent().getSerializableExtra("recipe");
        isImageSelected=true;
        binding.etRecipeName.setText(recipe.getName());
        binding.etDescription.setText(recipe.getDescription());
        binding.etCookingTime.setText(recipe.getTime());
        binding.etCategory.setText(recipe.getCategory());
        binding.etCalories.setText(recipe.getCalories());
        binding.btnAddRecipe.setText("Update Recipe");
        Glide
                .with(binding.getRoot().getContext())
                .load(recipe.getImage())
                .centerCrop()
                .placeholder(R.drawable.bg_default_recipe)
                .into(binding.imgRecipe);

    }

    private void loadCategories() {
        List<String> categories = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,categories);
        binding.etCategory.setAdapter(adapter);
        DatabaseReference reference = FirebaseDatabase.getInstance("https://recipe-cf3dd-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Categories");


        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && snapshot.hasChildren()){
                    for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                        categories.add(dataSnapshot.getValue(Category.class).getName());
                    }
                    adapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void pickImage() {
        PickImageDialog.build(new PickSetup())
                .show(AddRecipeActivity.this)
                .setOnPickResult(r -> {
                    Log.e("ProfileFragment", "Image URI: " + r.getUri());
                    binding.imgRecipe.setImageBitmap(r.getBitmap());
                    binding.imgRecipe.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    isImageSelected = true;
                })
                .setOnPickCancel(new IPickCancel() {
                    @Override
                    public void onCancelClick() {
                        Toast.makeText(AddRecipeActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getData() {
        String recipeName = Objects.requireNonNull(binding.etRecipeName.getText()).toString();
        String recipeDescription = Objects.requireNonNull(binding.etDescription.getText()).toString();
        String cookingTime = Objects.requireNonNull(binding.etCookingTime.getText()).toString();
        String calories = Objects.requireNonNull(binding.etCalories.getText()).toString();
        String recipeCategory = binding.etCategory.getText().toString();

        if (recipeName.isEmpty()) {
            binding.etRecipeName.setError("Enter Recipe Name");
        } else if (recipeDescription.isEmpty()) {
            binding.etDescription.setError("Enter Recipe Description");
        } else if (cookingTime.isEmpty()) {
            binding.etCookingTime.setError("Enter Cooking Time");
        } else if (calories.isEmpty()) {
            binding.etCalories.setError("Enter Calories");
        } else if (recipeCategory.isEmpty()) {
            binding.etCategory.setError("Enter Category");
        } else if (!isImageSelected) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        } else {
            dialog = new ProgressDialog(this);
            dialog.setMessage("uploading");
            dialog.setCancelable(false);
            Recipe recipe = new Recipe(recipeName, recipeDescription, cookingTime, calories, recipeCategory, "",FirebaseAuth.getInstance().getUid());
            uploadImage(recipe);


        }
    }

    private String uploadImage(Recipe recipe) {

        binding.imgRecipe.setDrawingCacheEnabled(true);
        Bitmap bitmap= ((BitmapDrawable) binding.imgRecipe.getDrawable()).getBitmap();
        binding.imgRecipe.setDrawingCacheEnabled(false);
        final String[] url ={""};
        String id= isEdit ? recipe.getId() : currentTimeMillis()+"";




        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + id + "_recipe.jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageRef.putBytes(data);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw Objects.requireNonNull(task.getException());
            }
            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                url[0]=downloadUri.toString();
                Toast.makeText(AddRecipeActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                saveDataInDataBase(recipe,url[0]);
            } else {
                Toast.makeText(AddRecipeActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Log.e("AddRecipeActivity", "Upload Failed: " + Objects.requireNonNull(task.getException()).getMessage());
            }
        });

        return url[0];
    }
    private void saveDataInDataBase(Recipe recipe, String url) {
        recipe.setImage(url);
        DatabaseReference reference = FirebaseDatabase.getInstance("https://recipe-cf3dd-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Recipes");
        //DatabaseReference reference = FirebaseDatabase.getInstance("https://recipe-cf3dd-default-rtdb.asia-southeast1.firebasedatabase.app").getReference().child("Recipes");

        String id = reference.push().getKey();
        recipe.setId(id);

        if (id != null) {
            reference.child(id).setValue(recipe).addOnCompleteListener(task -> {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(AddRecipeActivity.this, "Recipe Added Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddRecipeActivity.this, "Failed to Add Recipe", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}