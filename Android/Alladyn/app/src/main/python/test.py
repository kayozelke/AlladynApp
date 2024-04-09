import statistics
import os 

def test_function():
    text = "Hey I'm here!!!!"
    return text

def function_os():
    # result = os.getcwd()
    os.mkdir('./1')
    result = os.listdir(os.getcwd())
    # arr = [1,1,1]
    # result = statistics.mean(arr)
    return result

def write_file(file_path, content):
    with open(file_path, 'w') as f:
        f.write(content)