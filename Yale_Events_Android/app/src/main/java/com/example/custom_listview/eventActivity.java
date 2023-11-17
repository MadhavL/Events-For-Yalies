package com.example.custom_listview;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.example.custom_listview.databinding.ActivityEventBinding;

import java.io.File;

public class eventActivity extends AppCompatActivity {

    ActivityEventBinding binding;
    String link;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate");
        super.onCreate(savedInstanceState);
        binding = ActivityEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = this.getIntent();

        if (intent != null){
            System.out.println("intent not null");
            String eventName = intent.getStringExtra("eventName");
            String date = intent.getStringExtra("date");
            String time = intent.getStringExtra("time");
            link = intent.getStringExtra("link");
            if (link == null) {
                binding.registerButton.setVisibility(View.GONE);
            }
            //gets the file path
            String filePath = getIntent().getStringExtra("imagePath");
            //loads the file
            if (filePath != null) {
                File file = new File(filePath);
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                binding.eventImage.setImageBitmap(bitmap);
            }



            binding.eventNameTitle.setText(eventName);
            binding.eventDate.setText(date);
            binding.eventTime.setText(time);

        }

    }

    public void registerClicked(View view) {
        Uri uri = Uri.parse(link);
        Intent openintent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(openintent);
    }
}