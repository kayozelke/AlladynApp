import time
import math

def computeMeasurements(photoFilePath):
    length = generateRandomNumber()
    width = generateRandomNumber()

    rounded_length = roundMeasurements(length)
    rounded_width = roundMeasurements(width)

    square_area = rounded_length * rounded_width
    rounded_square_area = roundMeasurements(square_area)

    print(f"rounded_length: {rounded_length}, rounded_width: {rounded_width}, rounded_square_area: {rounded_square_area}")
    return {"length": rounded_length, "width": rounded_width, "area": rounded_square_area}



def roundMeasurements(measurement):
    measurement_rounded = math.ceil(measurement * 10) / 10
    return measurement_rounded


def generateRandomNumber():
    current_time = int(time.time() * 100) 
    last_two_digits = current_time % 100 / 100 
    random_number = round(last_two_digits * 10, 2) 
    return random_number

# print(f"{computeMeasurements("/test.txt")}")