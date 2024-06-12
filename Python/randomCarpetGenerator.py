import random


def pickRandomCarpet():
    

    carpets = [
        'https://i.postimg.cc/QxnMXP7g/D7Ylux4.jpg',
        'https://i.postimg.cc/g2X2rDyC/b6fwpF8.jpg',
        'https://i.postimg.cc/mr7DdNQF/wzorek.jpg'
    ]
    
    return random.choice(carpets)


print(pickRandomCarpet())