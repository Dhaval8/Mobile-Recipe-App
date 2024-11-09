package com.c019shranth.madproject.fragment;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.c019shranth.madproject.R;
import com.c019shranth.madproject.adapters.RecipeAdapter;
import com.c019shranth.madproject.databinding.FragmentProfileBinding;
import com.c019shranth.madproject.models.Recipe;
import com.c019shranth.madproject.models.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfileFragment extends Fragment implements IPickResult {

    private FragmentProfileBinding binding;
    private final List<Recipe> recipes = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            new AlertDialog.Builder(getContext())
                    .setTitle("Login Required")
                    .setMessage("you need to log in to view your profile")
                    .show();


        }else{
        loadProfile();
        loadUserRecipes();
        init();
        }
    }

    private void init() {
        binding.imgEditProfile.setOnClickListener(v ->
                PickImageDialog.build(new PickSetup()).show(requireActivity())
                        .setOnPickResult(r -> {
                            Log.e("ProfileFragment", "Profile image URI: " + r.getUri());
                            binding.imgProfile.setImageBitmap(r.getBitmap());
                            uploadImage(r.getBitmap(), "image.jpg");
                        })
                        .setOnPickCancel(() -> Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show())
        );

        binding.imgEditCover.setOnClickListener(v ->
                PickImageDialog.build(new PickSetup()).show(requireActivity())
                        .setOnPickResult(r -> {
                            Log.e("ProfileFragment", "Cover image URI: " + r.getUri());
                            binding.imgCover.setImageBitmap(r.getBitmap());
                            uploadImage(r.getBitmap(), "cover.jpg");
                        })
                        .setOnPickCancel(() -> Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show())
        );
    }

    private void uploadImage(Bitmap bitmap, String fileName) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference()
                .child("images")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .child(fileName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
        uploadTask.addOnFailureListener(exception -> {
            Log.e("ProfileFragment", "Image upload failed: " + exception.getMessage());
            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show();
        }).addOnSuccessListener(taskSnapshot ->
                Toast.makeText(requireContext(), "Image uploaded", Toast.LENGTH_SHORT).show()
        );
    }

    private void loadUserRecipes() {
        binding.rvProfile.setLayoutManager(new GridLayoutManager(getContext(), 2));
        RecipeAdapter recipeAdapter = new RecipeAdapter();
        binding.rvProfile.setAdapter(recipeAdapter);

        DatabaseReference reference = FirebaseDatabase.getInstance("https://recipe-cf3dd-default-rtdb.asia-southeast1.firebasedatabase.app").getReference();
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getUid());

        reference.child("Recipes").orderByChild("authorId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipes.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Recipe recipe = dataSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipes.add(recipe);
                        Log.d("ProfileFragment", "Recipe loaded: " + recipe.getName());
                    } else {
                        Log.e("ProfileFragment", "Recipe data is null");
                    }
                }
                recipeAdapter.setRecipeList(recipes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Database error: " + error.getMessage());
            }
        });
    }

    private void loadProfile() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://recipe-cf3dd-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users");
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getUid());

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    binding.tvUserName.setText(user.getName());
                    binding.tvEmail.setText(user.getEmail());

                    loadProfileImage("image.jpg", binding.imgProfile);

                    loadProfileImage("cover.jpg", binding.imgCover);
                } else {
                    Log.e("ProfileFragment", "User data is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "Profile loading cancelled: " + error.getMessage());
            }
        });
    }

    private void loadProfileImage(String fileName, ImageView imageView) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("images")
                .child(FirebaseAuth.getInstance().getUid())
                .child(fileName);

        storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                Glide.with(requireContext())
                        .load(uri)
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .into(imageView)
        ).addOnFailureListener(e ->
                Log.e("ProfileFragment", "Failed to load image: " + e.getMessage())
        );
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {
            Log.e("ProfileFragment", "Image picked successfully: " + r.getUri());
            binding.imgProfile.setImageBitmap(r.getBitmap());
        } else {
            Log.e("ProfileFragment", "Error picking image: " + r.getError().getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
