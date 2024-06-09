#import necessary libraries
from time import sleep
import pygame
import pygame.camera
import requests
import base64
import json
import RPi.GPIO as GPIO
import pigpio
import os
import json
from firebase import firebase
from datetime import date

pwm = pigpio.pi()

#analyse image function
def analyse_image(IMAGE_PATH):
    returned = [[], []]

    #analyse image locally
    res = os.popen("alpr -c ro -j -n 3 " + IMAGE_PATH).read()
    results = json.loads(res)
    
    #if there is a result, build plate number to be analysed for online service
    if len(results['results']):
        for r in results['results'][0]['candidates']:
            returned[0].append(format_plate_number(r['plate']))
    
        #check image online, on OpenALPR API
        with open(IMAGE_PATH, 'rb') as image_file:
            r = requests.post('https://api.platerecognizer.com/v1/plate-reader/', data=dict(regions=["ro"]), files=dict(upload=image_file), headers={'Authorization': 'Token beea50c9352e241e68054caa814076159fe05be3'})
            if len(r.json()['results']):
                i = 0
                for r in r.json()['results'][0]['candidates']:
                    i = i + 1
                    if i > 3:
                        break
                    returned[1].append(format_plate_number(r['plate']))
    
    #return result
    return returned

#function that formats the plates by capitalizing the letters and adding necessary spaces
def format_plate_number(plate_number):
    formatted_plate = ""
    i = 0
    
    while i < len(plate_number):
        #check if current index is a letter
        if plate_number[i].isalpha():
            formatted_plate = formatted_plate + plate_number[i]
            #check if next index is a number
            if i + 1 < len(plate_number) and plate_number[i + 1].isdigit():
                formatted_plate = formatted_plate + " "
        #check if current index is a number
        elif plate_number[i].isdigit():
            formatted_plate = formatted_plate + plate_number[i]
            #check if next index is a letter
            if i + 1 < len(plate_number) and plate_number[i + 1].isalpha():
                formatted_plate = formatted_plate + " "
        i = i + 1
    
    formatted_plate = formatted_plate.upper()
    
    return formatted_plate

#save image function
def take_image(IMAGE_PATH):
    #initialize camera
    pygame.camera.init()
    cam = pygame.camera.Camera("/dev/video0", (800, 600))
    sleep(0.1)
    cam.start()
    sleep(0.1)
    
    #take a picture
    img = cam.get_image()
    cam.stop()
    
    #save image in .bmp format
    pygame.image.save(img, 'image.bmp')
    
    #convert image in .jpg format
    os.system("convert image.bmp " + IMAGE_PATH)

#initialize pins function
def setup_pins():
    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    GPIO.setup(12, GPIO.IN)
    GPIO.setup(15, GPIO.OUT)
    GPIO.output(15, GPIO.LOW)
    pwm.set_mode(14, pigpio.OUTPUT)
    pwm.set_PWM_frequency(14, 50)
    turn_servo(0)


#function that turns the actuator
def turn_servo(state):
    if state:
        pwm.set_servo_pulsewidth(14, 2000)
        print("")
        print("Barier is now open")
        print("")
    else:
        pwm.set_servo_pulsewidth(14, 1000)
        print("")
        print("Barier is now closed")
        print("")
    sleep(2)
    pwm.set_PWM_dutycycle(14, 0)
    pwm.set_PWM_frequency(14, 0)

#function that reads the proximity sensor
def check_distance():
    return 1 - GPIO.input(12)

#function that turns the led on/off
def turn_led(state):
    if state:
        GPIO.output(15, GPIO.HIGH)
    else:
        GPIO.output(15, GPIO.LOW)

#function that prints in console the gathered results from local program/API
def print_results(result):
    print("")
    print("Offline results:")
    if len(result[0]) == 0:
        print("none")
    for a in result[0]:
        print(a)
    
    print("Online results:")
    if len(result[1]) == 0:
        print("none")
    for a in result[1]:
        print(a)
    print("")

#function that updates how many cars are currently in the parking lot
def update_current_capacity(cars):
    #connect to the database
    authentication = firebase.FirebaseAuthentication('AIzaSyCKkmz83BU8kFzRwUc6I9Bh0-st8lVkcac', 'alexandru.mitrofan10@gmail.com', extra={'id': 'chs-system-de-parcare'})
    f = firebase.FirebaseApplication('https://chs-system-de-parcare-default-rtdb.europe-west1.firebasedatabase.app/', authentication=authentication)

    #update parameter from database
    f.put('/currentCapacity', 'currentCapacity' , str(cars))

#function that checks if the plate number is authorized
def check_server(result, cars):
    #connect to the database
    authentication = firebase.FirebaseAuthentication('AIzaSyCKkmz83BU8kFzRwUc6I9Bh0-st8lVkcac', 'alexandru.mitrofan10@gmail.com', extra={'id': 'chs-system-de-parcare'})
    f = firebase.FirebaseApplication('https://chs-system-de-parcare-default-rtdb.europe-west1.firebasedatabase.app/', authentication=authentication)
    
    #gather parking lot max capacity
    locuriDict = f.get('/maxCapacity', None)
    
    locuri = []
    for key, value in locuriDict.items():
        locuri.append(value)
    
    locuri = int(locuri[-1])
    
    #check if parking lot is not full already
    if cars >= locuri:
        return False
    
    #get current date
    today = date.today()
    formatedDate = today.strftime("%d/%m/%Y")

    #gather authorized plate numbers
    numereDict = f.get('/plateNumbers', None)    

    #check all plate numbers from the database
    for key, value in numereDict.items():
        numar = []
        numar.append(value['plateNumber'])
        numar = str(numar[-1])
        #check if plate number exists in offline results
        for nrOff in result[0]:
            if nrOff == numar:
                permanent = []
                permanent.append(value['isPermanent'])
                permanent = bool(permanent[-1])
                #check if number is permanent or not
                if permanent == False:
                    dateRez = f.get('/plateNumbers/' + key + '/reservedDates', None)
                    #check if today the temporar number has access
                    if formatedDate in dateRez:
                        return True
                elif permanent == True:
                    return True
        #check if plate number exists in online results
        for nrOn in result[1]:
            if nrOn == numar:
                permanent = []
                permanent.append(value['isPermanent'])
                permanent = bool(permanent[-1])
                #check if number is permanent or not
                if permanent == False:
                    dateRez = f.get('/plateNumbers/' + key + '/reservedDates', None)
                    #check if today the temporar number has access
                    if formatedDate in dateRez:
                        return True
                elif permanent == True:
                    return True

    return False

#function leaves barrier open or closed indefinitely
def open_or_close_barrier_indefinitely():
    #connect to the database
    authentication = firebase.FirebaseAuthentication('AIzaSyCKkmz83BU8kFzRwUc6I9Bh0-st8lVkcac', 'alexandru.mitrofan10@gmail.com', extra={'id': 'chs-system-de-parcare'})
    f = firebase.FirebaseApplication('https://chs-system-de-parcare-default-rtdb.europe-west1.firebasedatabase.app/', authentication=authentication)

    stareBarieraDeschisaDict = f.get('/openBarrierIndefinitely', None)
    stareBarieraInchisaDict = f.get('/closeBarrierIndefinitely', None)

    stareBarieraDeschisa = []
    for key, value in stareBarieraDeschisaDict.items():
        stareBarieraDeschisa.append(value)
        
    deschideBariera = bool(stareBarieraDeschisa[-1])
    
    stareBarieraInchisa = []
    for key, value in stareBarieraInchisaDict.items():
        stareBarieraInchisa.append(value)
    
    inchideBariera = bool(stareBarieraInchisa[-1])

    if inchideBariera == True:
        
        print ("Preparing for 'barrier closed indefinitely' state...")
        
        #wait until car leaves
        while check_distance() == 1:
            sleep(0.1)
        
        sleep(4)

        #close barrier
        turn_servo(0)

        #turn on led indefinitely
        turn_led(True)

        print ("Barrier closed indefinitely by admin!")

        #recheck database for a change of state every 30 seconds
        while inchideBariera == True:
            
            stareBarieraInchisaDict = f.get('/closeBarrierIndefinitely', None)

            stareBarieraInchisa = []
            for key, value in stareBarieraInchisaDict.items():
                stareBarieraInchisa.append(value)
    
            inchideBariera = bool(stareBarieraInchisa[-1])
            
            sleep(5)

        #turn off led
        turn_led(False)
        
        print ("System recovery successful from 'barrier closed indefinitely' state!")
        
    if deschideBariera == True:

        #open barrier
        turn_servo(1)

        print ("Barrier opened indefinitely by admin!")

        #recheck database for a change of state every 30 seconds
        while deschideBariera == True:
            
            stareBarieraDeschisaDict = f.get('/openBarrierIndefinitely', None)

            stareBarieraDeschisa = []
            for key, value in stareBarieraDeschisaDict.items():
                stareBarieraDeschisa.append(value)
        
            deschideBariera = bool(stareBarieraDeschisa[-1])

            sleep(5)
        
        print ("System is recovering back to normal from 'barrier opened indefinitely' state...")
        
        #wait until car leaves
        while check_distance() == 1:
            sleep(0.1)
        
        sleep(4)
    
        #close barrier
        turn_servo(0)
        print ("System recovery successful from 'barrier opened indefinitely' state!")
