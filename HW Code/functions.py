#import necessary libraries
from time import sleep
import pygame
import pygame.camera
import requests
import base64
import json
import RPi.GPIO as GPIO
import os
import json
from firebase import firebase

pwm = None

#analyse image function
def analyse_image(IMAGE_PATH):
    returned = [[], []]

    #analyse image locally
    res = os.popen("alpr -c ro -j -n 3 " + IMAGE_PATH).read()
    results = json.loads(res)
    
    #if there is a result, build plate number to be analysed for online service
    if len(results['results']):
        for r in results['results'][0]['candidates']:
            returned[0].append(r['plate'])
    
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
    cam = pygame.camera.Camera("/dev/video0", (640, 480))
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
    global pwm
    GPIO.setmode(GPIO.BCM)
    GPIO.setwarnings(False)
    GPIO.setup(12, GPIO.IN)
    GPIO.setup(15, GPIO.OUT)
    GPIO.output(15, GPIO.LOW)
    GPIO.setup(14, GPIO.OUT)
    pwm = GPIO.PWM(14, 50)
    pwm.start(0)
    turn_servo(0)


#function that turns the actuator
def turn_servo(state):
    global pwm
    GPIO.output(14, True)
    if state:
        pwm.ChangeDutyCycle(9.6)
        print("Barier open")
    else:
        pwm.ChangeDutyCycle(4.5)
        print("Barier closed")
    sleep(2)
    pwm.ChangeDutyCycle(0)
    GPIO.output(14, False)
    GPIO.output(14, GPIO.LOW)

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
    print("offline results")
    if len(result[0]) == 0:
        print("none")
    for a in result[0]:
        print(a)
    
    print("online results")
    if len(result[1]) == 0:
        print("none")
    for a in result[1]:
        print(a)
    print("")

#function that checks if the plate number is authorized
def check_server(result, cars):
    #datele de conectare la baza de date
    authentication = firebase.FirebaseAuthentication('AIzaSyCKkmz83BU8kFzRwUc6I9Bh0-st8lVkcac', 'alexandru.mitrofan10@gmail.com', extra={'id': 'chs-system-de-parcare'})
    f = firebase.FirebaseApplication('https://chs-system-de-parcare-default-rtdb.europe-west1.firebasedatabase.app/', authentication=authentication)
    
    #gather authorized plate numbers
    numereDict = f.get('/nrInmatriculare', None)
    
    #gather parking lot max capacity
    locuriDict = f.get('/nrLocuri', None)
    
    #convert from dictionary into list
    numere = []
    for key, value in numereDict.items():
        numere.append(value['nrInmatriculare'])
    
    locuri = []
    for key, value in locuriDict.items():
        locuri.append(value)
    
    locuri = int(locuri[-1])
    
    #check if parking lot is not full already
    if cars >= locuri:
        return False
    
    #check if plate number exists in the database
    for n in numere:
        for n1 in result[0]:
            if n1 == n:
                return True
    
        for n1 in result[1]:
            if n1 == n:
                return True
    
    return False
