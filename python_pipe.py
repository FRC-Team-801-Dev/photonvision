print("Hello world I am the python_pipe")
import sys
import struct
import cv2
import numpy as np

while True:
    width, height, channels = struct.unpack('iii', sys.stdin.buffer.read(12))
    buffer = sys.stdin.buffer.read(width * height * channels)

    img = np.frombuffer(buffer, dtype=np.uint8).reshape(width, height, channels)
    img[:, :, :] = 0
    sys.stdout.write(img.tobytes())