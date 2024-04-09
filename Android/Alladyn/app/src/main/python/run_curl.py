import requests
import os


def getImageFromHttpResponse(http_address, output_file_location):
    try:
        res = requests.get(http_address, stream=True)  # Dodanie 'stream=True' jest istotne

        print(res) 

        if res.status_code == 200:
            with open(output_file_location, 'wb') as f:
                for chunk in res.iter_content(chunk_size=1024):  # Iteruj po kawałkach zawartości, aby uniknąć przepełnienia pamięci
                    f.write(chunk)
            return True
        return False
    except Exception as e:
        print("Error:")
        print(str(e))
        return False



def generateSamplePhotos(file_path):
    CAM_ADDRESS = "192.168.1.10"

    for i in range(0,10):
        PATH = os.path.join(file_path, f"image_{str(i)}.jpg")
        getImageFromHttpResponse(f'http://{CAM_ADDRESS}/capture', PATH)

    # PATH = os.path.join(file_path, "image.jpg")
    # getImageFromHttpResponse(f'http://{CAM_ADDRESS}/capture', PATH)


