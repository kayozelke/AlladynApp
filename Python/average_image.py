import os, numpy
from PIL import Image


def getAvgFromImages(images_directory_location, output_location):
    
    # Access all files in directory
    allfiles=os.listdir(images_directory_location)
    imlist = []
    for filename in allfiles:
        if filename[-4:] in [".jpg", ".png"]:
            imlist.append(os.path.join(images_directory_location, filename))

    # Assuming all images are the same size, get dimensions of first image
    w,h=Image.open(imlist[0]).size
    N=len(imlist)

    # Create a numpy array of floats to store the average (assume RGB images)
    arr=numpy.zeros((h,w,3),numpy.float64)

    # Build up average pixel intensities, casting each image as an array of floats
    for im in imlist:
        imarr=numpy.array(Image.open(im),dtype=numpy.float64)
        arr=arr+imarr/N

    # Round values in array and cast as 8-bit integer
    arr=numpy.array(numpy.round(arr),dtype=numpy.uint8)

    # Generate, save and preview final image
    out=Image.fromarray(arr,mode="RGB")
    out.save(output_location)


IMAGES_DIR = os.path.join(os.getcwd(), "images")
AVG_IMG_LOCATION = os.path.join(os.getcwd(), "average.png")
# getAvgFromImages(IMAGES_DIR, AVG_IMG_LOCATION)
