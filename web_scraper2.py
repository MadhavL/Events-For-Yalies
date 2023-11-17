from bs4 import BeautifulSoup

from selenium import webdriver
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support.wait import WebDriverWait
from webdriver_manager.chrome import ChromeDriverManager
import re
import firebase_admin
from firebase_admin import db

cred_obj = firebase_admin.credentials.Certificate('credential.json')
default_app = firebase_admin.initialize_app(cred_obj, {
	'databaseURL':'https://eventtest-a96be-default-rtdb.firebaseio.com/'
	})

ref = db.reference("/Events")

options = webdriver.ChromeOptions()

options.add_argument("--headless")
options.add_argument("--no-sandbox")
options.add_argument("--disable-gpu")
options.add_argument("--window-size=1920x1080")
options.add_argument("--disable-extensions")

chrome_driver = webdriver.Chrome(
    service=Service(ChromeDriverManager().install()),
    options=options
)

base_URL = "https://yaleconnect.yale.edu"

def event_scraper():
    URL = "https://yaleconnect.yale.edu/events"
    with chrome_driver as driver:
        driver.implicitly_wait(15)
        driver.get(URL)
        wait = WebDriverWait(driver, 10)
        
        # time.sleep(10) # increase the load time to fetch all element, not advised solution
       
        # wait until this element is visible 
        wait.until(EC.visibility_of_any_elements_located((By.CLASS_NAME, "rsvp__event-tags")))
        soup = BeautifulSoup(driver.page_source, "html.parser")
        results = soup.find(id="divAllItems")  
        events = results.find_all("li", id=re.compile('^event_'))
        data = {}
        for event in events:
            date = event.find("div", class_= "media-heading", attrs={'style':"display:inline-block;width: calc(100% - 25px);"}).find_all("p")
            print(date[0].text)
            imageBlock = event.find("div", class_="listing-element__preimg-block col-md-2")
            image = base_URL + imageBlock.find("img")['src']
            # print(image)
            text = event.find("h3", class_= 'media-heading header-cg--h4')
            name = text.find("a").text.strip() #Name of the event
            link = base_URL + text.find("a")['href']
            categories = event.find_all("span", class_='label label-default label-tag')
            categoriesdict = {}
            print("Categories: ", end='')
            for category in categories:
                print(category.text.strip(), end=', ') #The categories for the event
                categoriesdict[category.text.strip().replace('/', '\\')] = 0
            #Collect registration link
            categoryentry = {"Categories": categoriesdict, "Date": date[0].text, "Link": link, "Image": image, "Time": date[1].text}
            data[name.replace('/', '\\')] = categoryentry
            print('\n')
        ref.set(data)

event_scraper()