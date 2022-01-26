if __name__ == '__main__':
    import sys
    import cv2
    import struct
    import numpy as np

    # Default process_image (turns image red)
    def process_image(img: np.ndarray, **kwargs) -> np.ndarray:
        '''Example ``process_image`` function for type hints

        Args:
            img: The image as recieved from the Java pipe.
            **kwargs: A dictionary composed of the command line arguments.

        Returns:
            The processed image. 
        '''

        # Turn image red
        img[:, :, 0] = 0
        img[:, :, 1] = 0

        return img
    
    exec(f'from {sys.argv[1]} import process_image')

    kwargs = dict()
    for arg in sys.argv[2:]:
        if '=' in arg:
            key, value = tuple(arg.split('='))
            kwargs[key] = value

    try:
        while True:
            bstr = sys.stdin.buffer.read(12)
            height, width, channels = struct.unpack('>III', bstr)
            
            buffer = sys.stdin.buffer.read(width * height * channels)
            img = np.array(np.frombuffer(buffer, dtype=np.uint8).reshape(height, width, channels))

            try:
                img = process_image(img, **kwargs)
            except Exception as e:
                sys.stderr.write(str(e))
                sys.stderr.flush()

            sys.stdout.buffer.write(img.tobytes())
            sys.stdout.buffer.flush()
    except KeyboardInterrupt:
        sys.stderr.write("\nShutting down Python Pipe\n")
        sys.stderr.flush()
        pass
else:
    print("You are not allowed to import this module.")