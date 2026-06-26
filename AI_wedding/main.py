from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing import image

from PIL import Image, ImageOps
import numpy as np
import io

from validator import ImageValidator

app = FastAPI()

# ==========================
# CORS
# ==========================
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ==========================
# Load Model
# ==========================
model = load_model("model/body_shape_model.h5")

validator = ImageValidator()

class_labels = [
    "apple",
    "hourglass",
    "inverted_triangle",
    "pear",
    "rectangle"
]

IMG_SIZE = (224, 224)

MAX_IMAGE_BYTES = 10 * 1024 * 1024

SUPPORTED_IMAGE_TYPES = {
    "image/jpeg",
    "image/png",
    "image/webp"
}


# ==========================
# Read Image
# ==========================
async def read_supported_image(file: UploadFile):

    if file.content_type not in SUPPORTED_IMAGE_TYPES:
        raise HTTPException(
            status_code=400,
            detail="Only JPG, PNG and WEBP are supported."
        )

    contents = await file.read()

    if len(contents) > MAX_IMAGE_BYTES:
        raise HTTPException(
            status_code=413,
            detail="Image must be smaller than 10MB."
        )

    try:

        img = ImageOps.exif_transpose(
            Image.open(io.BytesIO(contents))
        ).convert("RGB")

        return img

    except Exception:
        raise HTTPException(
            status_code=400,
            detail="Invalid image."
        )


# ==========================
# Home
# ==========================
@app.get("/")
def home():
    return {
        "message": "Body Shape Detection API Running"
    }


# ==========================
# Predict
# ==========================
@app.post("/predict")
async def predict(
    file: UploadFile = File(...)
):

    try:

        # Đọc ảnh
        img = await read_supported_image(file)

        # Validate ảnh bằng YOLO
        valid, error = validator.validate(img)

        if not valid:
            raise HTTPException(
        status_code=400,
        detail=error
    )

        # Resize
        img = img.resize(IMG_SIZE)

        # Tensor
        img_array = image.img_to_array(img)

        img_array = img_array / 255.0

        img_array = np.expand_dims(
            img_array,
            axis=0
        )

        # Predict
        prediction = model.predict(
            img_array,
            verbose=0
        )

        index = np.argmax(prediction)

        confidence = float(
            np.max(prediction)
        )

        return {
            "success": True,
            "body_shape": class_labels[index],
            "confidence": round(confidence, 4)
        }

    except HTTPException:
        raise

    except Exception as e:
        return {
            "success": False,
            "error": str(e)
        }