import cv2
import numpy as np

def process_image(img: np.ndarray, **kwargs) -> np.ndarray:
    
    # Turn image green
    img[:, :, 0] = 0
    img[:, :, 2] = 0

    return img