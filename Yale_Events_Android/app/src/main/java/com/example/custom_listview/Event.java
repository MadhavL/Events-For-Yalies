package com.example.custom_listview;

import android.graphics.Bitmap;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Event {

    public String name, date, time, link;
    public List<String> category;
    int imageId;
    Bitmap image;

    public Event(String name, String date, List<String> category, String time, String link, Bitmap image) {
        this.name = name;
        this.date = date;
        this.category = category;
        this.time = time;
        this.link = link;
        this.image = image;
    }

    public Date getTime() {
        Date timeAsDate = new Date(0);

        if (time != null) {
            String rawTime = time.split(" – ")[0];
            String hour = rawTime.split(" ")[0];
            if (!hour.contains(":")) {
                hour += ":00";
            }
            String suffix = rawTime.split(" ")[1];
            rawTime = hour + " " + suffix;
            if (rawTime != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.ENGLISH);
                try {
                    timeAsDate = formatter.parse(rawTime);
                    return timeAsDate;
                } catch (ParseException e) {

                }
            }
        }
        return timeAsDate;
    }
}
