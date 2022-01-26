import sys
import cv2
import struct
import numpy as np

while True:
    bstr = sys.stdin.buffer.read(12)
    height, width, channels = struct.unpack('>III', bstr)
    
    buffer = sys.stdin.buffer.read(width * height * channels)
    img = np.array(np.frombuffer(buffer, dtype=np.uint8).reshape(height, width, channels))

    # Turn image red
    img[:, :, 0] = 0
    img[:, :, 1] = 0

    cv2.imshow("Processed Image", img)
    
    if cv2.waitKey(10) == ord('q'):
        break

    sys.stdout.buffer.write(img.tobytes())
    sys.stdout.buffer.flush()