import requests
import os


def getImageFromHttpResponse(http_address, output_file_location):
    # chatGPT comments
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



def test():
    CAM_ADDRESS = "192.168.1.10"
    IMAGES_DIR = "images"

    if not os.path.isdir(IMAGES_DIR):
        try:
            os.mkdir(IMAGES_DIR,)
        except OSError as err:
            print(f"OSError: {str(err)}")
            exit(1)

    for i in range(0,10):
        PATH = f"{IMAGES_DIR}/image_{str(i)}.jpg"
        getImageFromHttpResponse(f'http://{CAM_ADDRESS}/capture', PATH)


# test()