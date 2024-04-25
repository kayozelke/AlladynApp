"""
Filename: init.py
Usage: This script will measure different objects in the frame using a reference object of known dimension. 
The object with known dimension must be the leftmost object.
Author: Shashank Sharma
"""
import math
from scipy.spatial.distance import euclidean
from imutils import perspective
from imutils import contours
import numpy as np
import imutils
import cv2
# from PIL import Image

# Function to show array of images (intermediate results)
def show_images(images):
    for i, img in enumerate(images):
        cv2.imshow("image_" + str(i), img)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
    
def getWidthAndHeightFromContours(cnt, pixel_per_m):
    
    box = cv2.minAreaRect(cnt)
    box = cv2.boxPoints(box)
    box = np.array(box, dtype="int")
    box = perspective.order_points(box)
    (tl, tr, br, bl) = box
    # cv2.drawContours(image, [box.astype("int")], -1, (0, 0, 255), 2) 
    
    wid = euclidean(tl, tr)/pixel_per_m
    ht = euclidean(tr, br)/pixel_per_m
    return wid,ht

def getContoursSortedFromImage(img_path):
    # Read image and preprocess
    image = cv2.imread(img_path)

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    blur = gray
    blur = cv2.GaussianBlur(gray, (9,9), 0)
    # blur = cv2.GaussianBlur(gray, (3, 3), 0)


    # compute the median of the single channel pixel intensities
    v = np.median(image)
    # apply automatic Canny edge detection using the computed median
    sigma = 0.33
    lower = int(max(0, (1.0 - sigma) * v))
    upper = int(min(255, (1.0 + sigma) * v))

    edged = cv2.Canny(blur, lower, upper, apertureSize=3, L2gradient=False)
    # edged = cv2.Canny(blur, 40, 200, apertureSize=3, L2gradient=False)
    edged = cv2.dilate(edged, None, iterations=1)
    edged = cv2.erode(edged, None, iterations=1)

    #show_images([blur, edged])

    # Find contours
    cnts = cv2.findContours(edged.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    cnts = imutils.grab_contours(cnts)

    # Sort contours from left to right as leftmost contour is reference object
    if len(cnts):
        (cnts, _) = contours.sort_contours(cnts)

    # Remove contours which are not large enough
    cnts = [x for x in cnts if cv2.contourArea(x) > 100]
    return cnts

def addSingleCntAndSizesToImage(numpy_ndarray_image, cnt, pixel_per_m):
    box = cv2.minAreaRect(cnt)
    box = cv2.boxPoints(box)
    box = np.array(box, dtype="int")
    box = perspective.order_points(box)
    (tl, tr, br, bl) = box
    cv2.drawContours(numpy_ndarray_image, [box.astype("int")], -1, (0, 0, 255), 3)
    mid_pt_horizontal = (tl[0] + int(abs(tr[0] - tl[0])/2), tl[1] + int(abs(tr[1] - tl[1])/2))
    mid_pt_verticle = (tr[0] + int(abs(tr[0] - br[0])/2), tr[1] + int(abs(tr[1] - br[1])/2))
    wid = roundMeasurements(euclidean(tl, tr)/pixel_per_m)
    ht = roundMeasurements(euclidean(tr, br)/pixel_per_m)
    cv2.putText(numpy_ndarray_image, "{:.1f}m".format(wid), (int(mid_pt_horizontal[0] - 15), int(mid_pt_horizontal[1] - 10)), 
        cv2.FONT_HERSHEY_SIMPLEX, 1.3, (255, 255, 0), 4)
    cv2.putText(numpy_ndarray_image, "{:.1f}m".format(ht), (int(mid_pt_verticle[0] + 10), int(mid_pt_verticle[1])), 
        cv2.FONT_HERSHEY_SIMPLEX, 1.3, (255, 255, 0), 4)
    return numpy_ndarray_image
    


def getPixelPerCmByReferenceObject(cnts, reference_object_width_in_cm):
    if len(cnts) < 1:
        return None

    ref_object = cnts[0]
    ref_box = cv2.minAreaRect(ref_object)
    ref_box = cv2.boxPoints(ref_box)
    ref_box = np.array(ref_box, dtype="int")
    ref_box = perspective.order_points(ref_box)
    (r_tl, r_tr, r_br, r_bl) = ref_box
    dist_in_pixel = euclidean(r_tl, r_tr)
    pixel_per_m = dist_in_pixel/reference_object_width_in_cm

    return pixel_per_m

def getPixelPerCmByImageWidth(image_file : str, real_visible_range_in_cm):
    im = cv2.imread(image_file)
    _, width, _ = im.shape
    return width / real_visible_range_in_cm

def getPixelPerCmByImageHeight(image_file : str, real_visible_range_in_cm):
    im = cv2.imread(image_file)
    height, _, _ = im.shape
    return height / real_visible_range_in_cm


def measureEveryObject(cnts, pixel_per_m):
    returnArray = []
    
    if len(cnts) < 1:
        print("Warning from 'measureEveryObject()': No contours provided!")
        return returnArray

    for cnt in cnts:
        cnt_width, cnt_height = getWidthAndHeightFromContours(cnt,pixel_per_m=pixel_per_m)
        returnArray.append([cnt_width, cnt_height])

    return returnArray



def addAllCntsAndSizesToImage(cnts, pixel_per_m, input_img_path : str, output_img_path : str):
    image = cv2.imread(input_img_path)
    for cnt in cnts:
        image = addSingleCntAndSizesToImage(image, cnt, pixel_per_m)

    # show_images([image])
    cv2.imwrite(output_img_path, image) 


def roundMeasurements(measurement):
    measurement_rounded = math.ceil(measurement * 10) / 10
    return measurement_rounded


def alladyn_main(source_img_path, output_img_path):
    
    REAL_VISIBLE_WIDTH_AT_IMG_IN_M = 1.5
    
    pixel_per_m = getPixelPerCmByImageWidth(source_img_path, REAL_VISIBLE_WIDTH_AT_IMG_IN_M)
    # get contours
    # if not found any, object type is None
    cnts = getContoursSortedFromImage(source_img_path)
    
    if cnts:
        # get biggest obj
        largest_obj_cnt = max(cnts, key=cv2.contourArea)
        largest_obj_real_width, largest_obj_real_height = getWidthAndHeightFromContours(cnt=largest_obj_cnt,pixel_per_m=pixel_per_m)
        
        #### ADD lines and sizes to image file ####
        if output_img_path is not None and output_img_path:
            image = cv2.imread(source_img_path)
            image = addSingleCntAndSizesToImage(image, largest_obj_cnt, pixel_per_m)
            cv2.imwrite(output_img_path, image) 
        ###########################################
        
        resultData = {
            'length' : roundMeasurements(largest_obj_real_height),
            'width' : roundMeasurements(largest_obj_real_width)
        }
            
        resultData['area'] = roundMeasurements(resultData["length"] * resultData['width'])
    else:
        resultData = {'length': 0.0 , 'width': 0.0, 'area' : 0.0}
    
    return resultData




