package com.example.custom_listview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.example.custom_listview.databinding.ActivityMainBinding;
import com.github.jhonnyx2012.horizontalpicker.DatePickerListener;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements DatePickerListener {

    // Registration link for event, categories
    // QR code for inviting to events
    // Add remaining filters

    ActivityMainBinding binding;
    ListAdapter listAdapter;
    ListView eventList;
    TextView date;
    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("EE, MMM dd", Locale.getDefault());
    ImageButton rightButton;
    ImageButton leftButton;
    ImageButton newEventButton;
    String current_date;
    String currentFilter = "";
    String currentCategory = "";

    FirebaseAuth mAuth;
    TextView userNameText;
    ImageButton signInButton;
    ImageButton signOutButton;

    int imageQueue = 0;

    SpinKitView loadingSpinner;

    TextView noResultText;

    TextView socialCategoryText;
    LinearLayout socialCategoryBg;

    TextView talkCategoryText;
    LinearLayout talkCategoryBg;

    TextView groupCategoryText;
    LinearLayout groupCategoryBg;

    TextView communityCategoryText;
    LinearLayout communityCategoryBg;

    TextView trainingCategoryText;
    LinearLayout trainingCategoryBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Events");

        date = (TextView) findViewById(R.id.dateText);
        Date right_now = c.getTime();
        current_date  = df.format(right_now);
        date.setText(current_date);

        loadingSpinner = findViewById(R.id.spin_kit);

        eventList = findViewById(R.id.eventList);
        eventList.setVisibility(View.GONE);

        rightButton = findViewById(R.id.rightArrow);
        leftButton = findViewById(R.id.leftArrow);
        newEventButton = findViewById(R.id.newEventButton);

        noResultText = findViewById(R.id.noResultText);

        socialCategoryText = findViewById(R.id.SocialCategoryText);
        socialCategoryBg = findViewById(R.id.SocialCategoryBackground);

        talkCategoryText = findViewById(R.id.TalkCategoryText);
        talkCategoryBg = findViewById(R.id.TalkCategoryBg);

        groupCategoryText = findViewById(R.id.GroupCategoryText);
        groupCategoryBg = findViewById(R.id.GroupCategoryBg);

        communityCategoryText = findViewById(R.id.CommunityCategoryText);
        communityCategoryBg = findViewById(R.id.CommunityCategoryBg);

        trainingCategoryText = findViewById(R.id.TrainingCategoryText);
        trainingCategoryBg = findViewById(R.id.TrainingCategoryBg);

        signOutButton = findViewById(R.id.signOutButton);
        signInButton = findViewById(R.id.signInButton);
        userNameText = findViewById(R.id.userNameText);
        mAuth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(view ->{
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        });


        signOutButton.setOnClickListener(view ->{
            mAuth.signOut();
            userNameText.setText("Guest");
            signInButton.setEnabled(true);
            newEventButton.setEnabled(false);
        });

        newEventButton.setOnClickListener(view ->{
            startActivity(new Intent(MainActivity.this, NewEventActivity.class));
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                c.add(Calendar.DATE, 1);
                Date new_date = c.getTime();
                current_date = df.format(new_date);
                date.setText(current_date);
                currentFilter = current_date + "-" + currentCategory;
                listAdapter.getFilter().filter(currentFilter);
            }
        });

        leftButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                c.add(Calendar.DATE, -1);
                Date new_date = c.getTime();
                current_date = df.format(new_date);
                date.setText(current_date);
                currentFilter = current_date + "-" + currentCategory;
                System.out.println("Date: " + current_date);
                listAdapter.getFilter().filter(currentFilter);
            }
        });


        ArrayList<Event> eventArrayList = new ArrayList<>();

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                String value = dataSnapshot.getValue(String.class);
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                for (Map.Entry<String, Object> event : map.entrySet()) {
                    System.out.println("Event = " + event.getKey()); //Event name
                    Map<String, Object> info = (Map<String, Object>) event.getValue();
                    String date = (String) info.get("Date");
                    String time = (String) info.get("Time");
                    String link = (String) info.get("Link");
                    String imageUrl = (String) info.get("Image");
                    Map<String, Object> categories = (Map<String, Object>) info.get("Categories");
//                    System.out.print("Categories: ");
                    List<String> categoriesList = new ArrayList<String>();
                    if (categories != null) {
                        for (Map.Entry<String, Object> category : categories.entrySet()) {
//                            System.out.print(category.getKey() + ", "); //Categories
                            categoriesList.add(category.getKey().toLowerCase().replace('\\', '/'));
                        }
                    }
                    else {
                        categoriesList.add("Social");
                    }

//                    System.out.println("\nDate = " + date); //Date
                    Bitmap placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
                    Event user = new Event(event.getKey(),date,categoriesList,time,link, placeholder);
                    new DownloadImageTask(user).execute(imageUrl);
                    eventArrayList.add(user);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        Comparator<Event> eventDateCompare
//                                = Comparator.comparing(
//                                Event::getTime, (s1, s2) -> {
//                                    return s2.compareTo(s1);
//                                });
                        Collections.sort(eventArrayList, Comparator.comparing(Event::getTime));
                    }
                }
//                Log.d("FB", "Value is: " + map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("FB", "Failed to read value.", error.toException());
            }
        });

//        System.out.println("Fired:" + eventArrayList.toString());
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            Collections.sort(eventArrayList, Comparator.comparing(Event::getTime));
//        }
//        System.out.println("Fired2:" + eventArrayList.toString());

        listAdapter = new ListAdapter(MainActivity.this, eventArrayList);

        listAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override public void onChanged() {
                super.onChanged();
                if (listAdapter.numFiltered == 0) {
                    noResultText.setVisibility(View.VISIBLE);
                }
                else {
                    noResultText.setVisibility(View.GONE);
                }

                if (!listAdapter.ready && listAdapter.numFiltered != 999) {
                    listAdapter.ready = true;
                    eventList.setVisibility(View.VISIBLE);
                }
            }
        });

        binding.eventList.setAdapter(listAdapter);
        binding.eventList.setClickable(true);
        binding.eventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event selectedEvent = (Event) eventList.getItemAtPosition(position);
                Intent i = new Intent(MainActivity.this, eventActivity.class);
                i.putExtra("eventName",selectedEvent.name);
                i.putExtra("date",selectedEvent.date);
                i.putExtra("time",selectedEvent.time);
                i.putExtra("link", selectedEvent.link);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                if (selectedEvent.image != null) {
//                    System.out.println("selectedImage not null");
//                    selectedEvent.image.compress(Bitmap.CompressFormat.PNG, 50, stream);
//                    System.out.println("Fired1");
//                    byte[] byteArray = stream.toByteArray();
//                    System.out.println("Fired2");
//                    i.putExtra("image",byteArray);
//                    System.out.println("Fired3");
                    String filePath= tempFileImage(MainActivity.this,selectedEvent.image,"name");
                    i.putExtra("imagePath", filePath);
                }
                startActivity(i);
                System.out.println("Fired4");
            }
        });

        checkUserSignedIn();

    }

    private void checkUserSignedIn()
    {
        FirebaseUser user = mAuth.getInstance().getCurrentUser();
        if (user != null) {
            if (user.isEmailVerified())
            {
                // user is verified, so you can finish this activity or send user to activity which you want.
                userNameText.setText(user.getDisplayName());
                signInButton.setEnabled(false);
            }
            else
            {
                // email is not verified, so just prompt the message to the user and restart this activity.
                // NOTE: don't forget to log out the user.
                Log.d("Firebase", "Not verified");
                startActivity(new Intent(MainActivity.this, NotVerifiedActivity.class));
                FirebaseAuth.getInstance().signOut();
                newEventButton.setEnabled(false);

            }

        }
        else {
            newEventButton.setEnabled(false);
        }

    }

    @Override
    public void onDateSelected(@NonNull final DateTime dateSelected) {
        // log it for demo
        Log.i("HorizontalPicker", "Selected date is " + dateSelected.toString());
    }

    public void deselectCategory(String catName, LinearLayout bg, TextView text) {
        currentCategory = currentCategory.replaceAll(catName + ";", "");
        currentFilter = current_date + "-" + currentCategory;
        bg.setBackground(getResources().getDrawable(R.drawable.empty_bg));
        text.setTextColor(getResources().getColor(R.color.text));
        listAdapter.getFilter().filter(currentFilter);
    }

    public void selectCategory(String catName, LinearLayout bg, TextView text) {
        currentCategory += catName + ";";
        currentFilter = current_date + "-" + currentCategory;
        bg.setBackground(getResources().getDrawable(R.drawable.gradient_bg));
        text.setTextColor(getResources().getColor(R.color.white));
        listAdapter.getFilter().filter(currentFilter);
    }

    public void socialClicked(View view) {
        if (currentCategory.toLowerCase().contains("social")) {
            deselectCategory("Social", socialCategoryBg, socialCategoryText);
        }
        else {
            selectCategory("Social", socialCategoryBg, socialCategoryText);
        }
    }

    public void talkClicked(View view) {
        if (currentCategory.toLowerCase().contains("lecture, talk, or panel")) {
            deselectCategory("Lecture, Talk, or Panel", talkCategoryBg, talkCategoryText);
        }
        else {
            selectCategory("Lecture, Talk, or Panel", talkCategoryBg, talkCategoryText);
        }
    }

    public void groupClicked(View view) {
        if (currentCategory.toLowerCase().contains("group meeting")) {
            deselectCategory("Group Meeting", groupCategoryBg, groupCategoryText);
        }
        else {
            selectCategory("Group Meeting", groupCategoryBg, groupCategoryText);
        }
    }

    public void communityClicked(View view) {
        if (currentCategory.toLowerCase().contains("commmunity service & volunteer")) {
            deselectCategory("Commmunity Service & Volunteer", communityCategoryBg, communityCategoryText);
        }
        else {
            selectCategory("Commmunity Service & Volunteer", communityCategoryBg, communityCategoryText);
        }
    }

    public void trainingClicked(View view) {
        if (currentCategory.toLowerCase().contains("training/workshop")) {
            System.out.println("Fired!");
            deselectCategory("Training/Workshop", trainingCategoryBg, trainingCategoryText);
        }
        else {
            selectCategory("Training/Workshop", trainingCategoryBg, trainingCategoryText);
        }
    }

    public static String tempFileImage(Context context, Bitmap bitmap, String name) {

        File outputDir = context.getCacheDir();
        File imageFile = new File(outputDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(context.getClass().getSimpleName(), "Error writing file", e);
        }

        return imageFile.getAbsolutePath();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        Event event;

        public DownloadImageTask(Event event) {
            this.event = event;
            imageQueue += 1;
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap mIcon11 = null;
            if (urls != null) {
                String urldisplay = urls[0];
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                event.image = result;
            }
            else {
//                Bitmap placeholder = BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_image);
            }
//            listAdapter.add(event);
            imageQueue--;
            listAdapter.notifyDataSetChanged();
            if (imageQueue == 0) {
                System.out.println("ALL LOADED!");
                loadingSpinner.setVisibility(View.GONE);
                currentFilter = current_date + "-" + currentCategory;
                listAdapter.getFilter().filter(currentFilter);
            }
        }
    }
}