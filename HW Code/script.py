#import functions from file functions.py
from functions import *
from time import sleep

#initialize pins
setup_pins()
cars = 0
update_current_capacity(cars)
last_image = 0

#run infinite loop
while True:
    last_image += 1

    #take a picture every second
    if last_image > 1000:
        last_image = 0

    #save image
    take_image('image.jpg')

    #analyse image
    result = analyse_image('image.jpg')
    print_results(result)

    #found a result in the image
    if len(result[0]) > 0:

        #check if plate number is autorized
        ok = check_server(result, cars)

        #open barrier if is autorized
        if ok:
            cars += 1

            #open barrier
            turn_servo(1)

            #wait for the car to pass
            while check_distance() == 0:
                sleep(0.1)

            while check_distance() == 1:
                sleep(0.1)

            sleep(2)

            #close barrier
            turn_servo(0)

            #update number of cars in the parking lot
            update_current_capacity(cars)

        else:  # turn on led if not autorized
            turn_led(True)
            sleep(2)
            turn_led(False)

    #check if there is any car wanting to leave the parking lot
    if check_distance() == 1 and cars > 0:

        #open barrier
        cars -= 1
        turn_servo(1)

        #wait until car leaves
        while check_distance() == 1:
            sleep(0.1)

        sleep(4)

        #close barrier
        turn_servo(0)

        #update number of cars in the parking lot
        update_current_capacity(cars)

    sleep(0.001)
