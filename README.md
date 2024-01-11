# Events-For-Yalies
The best way for Yale students to see what's going on nearby - cultural events, parties, extracurriculars, varsity games - all from your phone.

## Using the app
Create an account with your @yale.edu email address, search for events, or browse events by filtering by category, date, location, etc. Sign up for events by clicking on the link.

## The tech stack
- Native Android App (pure Java)
- Firebase Authentication for user profile management
- Firebase Database to store event information
- Python script to scrape Yale's event database on the web using Selenium and push to realtime database

## Spin up your own version
### Web scraper
1. Set up your Firebase Real-time Database
2. Save your API credential as 'credential.json' in the same folder you've extracted [web_scraper.py](web_scraper.py) to
3. Replace `databaseURL` with your Firebase database in line 15 of [web_scraper.py](web_scraper.py)

### Android App
1. `gradle build` the project (tested on Android SDK 31)
2. Download `google-services.json` from your Firebase console and replace it in the /app directory for your project
