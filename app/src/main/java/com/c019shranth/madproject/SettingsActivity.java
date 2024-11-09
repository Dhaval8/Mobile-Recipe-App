package com.c019shranth.madproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.c019shranth.madproject.databinding.ActivityAddRecipeBinding;
import com.c019shranth.madproject.databinding.ActivitySettingsBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    @NonNull ActivitySettingsBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivitySettingsBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        binding.linearLayoutShare.setOnClickListener(view -> shareApp());
        binding.linearLayoutRate.setOnClickListener(view -> rateApp());
        binding.linearLayoutFeedback.setOnClickListener(view -> sendFeedback());
        //binding.linearLayoutShare.setOnClickListener(view -> shareApp());
        binding.linearLayoutApps.setOnClickListener(view -> moreApps());
        binding.linearLayoutPrivacy.setOnClickListener(view -> privacyPolicy());
        binding.btnSignout.setOnClickListener(view -> signOut());


    }

    private void signOut() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialogInterface, i) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finishAffinity();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();
    }

    private void privacyPolicy() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse("https://policies.google.com/privacy?hl=en-US"));
        startActivity(intent);
    }

    private void moreApps() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse("https://www.apple.com/in/app-store/"));
        startActivity(intent);
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for " + getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "Hi " + getString(R.string.developer_name) + ",");
        startActivity(Intent.createChooser(intent, "Send Feedback"));
    }

    private void rateApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(android.net.Uri.parse("https://www.apple.com/in/app-store/" + getPackageName()));
        startActivity(intent);
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.developer_email)});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback for " + getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "Hi " + getString(R.string.developer_name) + ",");
        startActivity(Intent.createChooser(intent, "Send Feedback"));
        
    }
}