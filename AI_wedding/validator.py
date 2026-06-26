from ultralytics import YOLO
from PIL import Image
import numpy as np


class ImageValidator:

    def __init__(self):
        self.model = YOLO("yolov8n.pt")

    def validate(self, img: Image.Image):

        results = self.model.predict(
            np.array(img),
            verbose=False
        )

        persons = []

        for box in results[0].boxes:
            cls = int(box.cls[0])

            # class 0 = person
            if cls == 0:
                persons.append(box)

        if len(persons) == 0:
            return False, "Không phát hiện người trong ảnh."

        if len(persons) > 1:
            return False, "Ảnh chỉ được chứa một người."

        box = persons[0]

        x1, y1, x2, y2 = box.xyxy[0]

        person_area = (x2 - x1) * (y2 - y1)
        image_area = img.width * img.height

        ratio = float(person_area / image_area)

        if ratio < 0.25:
            return False, "Người đứng quá xa camera."

        return True, None