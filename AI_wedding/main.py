from fastapi import FastAPI, File, Form, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing import image
from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont, ImageOps
import numpy as np
import io
import os
import cv2

from google.protobuf import message_factory

if not hasattr(message_factory.MessageFactory, "GetPrototype"):
    def _get_prototype(self, descriptor):
        return message_factory.GetMessageClass(descriptor)

    message_factory.MessageFactory.GetPrototype = _get_prototype

try:
    import mediapipe as mp
except Exception:
    mp = None

app = FastAPI()

# ==========================
# Enable CORS
# ==========================
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ==========================
# Load trained model
# ==========================
model = load_model(
    "model/body_shape_model.h5"
)

# IMPORTANT:
# sửa theo đúng thứ tự class của m
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
    "image/webp",
}
TRY_ON_PREVIEW = "preview"
TRY_ON_REAL = "real"
POSE_VISIBILITY_THRESHOLD = 0.45


async def read_supported_image(upload_file: UploadFile) -> Image.Image:
    if upload_file.content_type not in SUPPORTED_IMAGE_TYPES:
        raise HTTPException(
            status_code=400,
            detail="Only JPG, PNG and WEBP images are supported"
        )

    contents = await upload_file.read()
    if len(contents) > MAX_IMAGE_BYTES:
        raise HTTPException(
            status_code=413,
            detail="Image is larger than 10MB"
        )

    try:
        return ImageOps.exif_transpose(
            Image.open(io.BytesIO(contents))
        ).convert("RGB")
    except Exception as exc:
        raise HTTPException(
            status_code=400,
            detail=f"Invalid image file: {exc}"
        )


def cover_resize(img: Image.Image, size: tuple[int, int]) -> Image.Image:
    img_ratio = img.width / img.height
    target_ratio = size[0] / size[1]

    if img_ratio > target_ratio:
        new_height = size[1]
        new_width = int(new_height * img_ratio)
    else:
        new_width = size[0]
        new_height = int(new_width / img_ratio)

    resized = img.resize((new_width, new_height), Image.Resampling.LANCZOS)
    left = (new_width - size[0]) // 2
    top = (new_height - size[1]) // 2
    return resized.crop((left, top, left + size[0], top + size[1]))


def fit_resize(img: Image.Image, max_size: tuple[int, int]) -> Image.Image:
    output = img.copy()
    output.thumbnail(max_size, Image.Resampling.LANCZOS)
    return output


def rounded_mask(size: tuple[int, int], radius: int) -> Image.Image:
    mask = Image.new("L", size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle(
        (0, 0, size[0] - 1, size[1] - 1),
        radius=radius,
        fill=255
    )
    return mask


def extract_foreground_cutout(img: Image.Image) -> Image.Image:
    rgb = np.array(img.convert("RGB"))
    h, w = rgb.shape[:2]

    if h < 20 or w < 20:
        return img.convert("RGBA")

    mask = np.zeros((h, w), np.uint8)
    bgd_model = np.zeros((1, 65), np.float64)
    fgd_model = np.zeros((1, 65), np.float64)

    rect = (
        max(1, int(w * 0.08)),
        max(1, int(h * 0.04)),
        max(2, int(w * 0.84)),
        max(2, int(h * 0.90)),
    )

    try:
        cv2.grabCut(
            rgb,
            mask,
            rect,
            bgd_model,
            fgd_model,
            5,
            cv2.GC_INIT_WITH_RECT
        )
    except Exception:
        return img.convert("RGBA")

    alpha = np.where(
        (mask == cv2.GC_FGD) | (mask == cv2.GC_PR_FGD),
        255,
        0
    ).astype("uint8")

    foreground_ratio = float(np.count_nonzero(alpha)) / float(alpha.size)
    if foreground_ratio < 0.02 or foreground_ratio > 0.98:
        return img.convert("RGBA")

    rgba = img.convert("RGBA")
    rgba.putalpha(Image.fromarray(alpha, mode="L"))
    return rgba


def clamp(value: float, minimum: int, maximum: int) -> int:
    return max(minimum, min(int(round(value)), maximum))


def map_point_to_cover(
        point: tuple[float, float],
        source_size: tuple[int, int],
        target_size: tuple[int, int]
) -> tuple[int, int]:
    src_w, src_h = source_size
    dst_w, dst_h = target_size
    scale = max(dst_w / src_w, dst_h / src_h)
    scaled_w = src_w * scale
    scaled_h = src_h * scale
    crop_x = (scaled_w - dst_w) / 2
    crop_y = (scaled_h - dst_h) / 2
    return (
        int(round(point[0] * scale - crop_x)),
        int(round(point[1] * scale - crop_y))
    )


def landmark_point(
        landmark,
        width: int,
        height: int
) -> tuple[int, int] | None:
    if landmark.visibility < POSE_VISIBILITY_THRESHOLD:
        return None

    x = clamp(landmark.x * width, 0, width - 1)
    y = clamp(landmark.y * height, 0, height - 1)
    return x, y


def detect_body_features(person: Image.Image):
    if mp is None:
        return None

    pose = None
    selfie = None
    try:
        rgb = np.array(person.convert("RGB"))
        pose = mp.solutions.pose.Pose(
            static_image_mode=True,
            model_complexity=1,
            enable_segmentation=True,
            min_detection_confidence=0.45
        )
        selfie = mp.solutions.selfie_segmentation.SelfieSegmentation(
            model_selection=1
        )

        pose_result = pose.process(rgb)
        seg_result = selfie.process(rgb)
    except Exception:
        return None
    finally:
        try:
            if pose is not None:
                pose.close()
        except Exception:
            pass
        try:
            if selfie is not None:
                selfie.close()
        except Exception:
            pass

    if not pose_result.pose_landmarks:
        return None

    h, w = rgb.shape[:2]
    lm = pose_result.pose_landmarks.landmark

    landmarks = {
        "left_shoulder": landmark_point(lm[11], w, h),
        "right_shoulder": landmark_point(lm[12], w, h),
        "left_hip": landmark_point(lm[23], w, h),
        "right_hip": landmark_point(lm[24], w, h),
        "left_elbow": landmark_point(lm[13], w, h),
        "right_elbow": landmark_point(lm[14], w, h),
        "left_knee": landmark_point(lm[25], w, h),
        "right_knee": landmark_point(lm[26], w, h),
        "nose": landmark_point(lm[0], w, h),
    }

    if not landmarks["left_shoulder"] or not landmarks["right_shoulder"]:
        return None

    return {
        "landmarks": landmarks,
        "mask": seg_result.segmentation_mask if seg_result.segmentation_mask is not None else None,
        "source_size": (w, h),
    }


def detect_pose_only(img: Image.Image):
    if mp is None:
        return None

    pose = None
    try:
        rgb = np.array(img.convert("RGB"))
        pose = mp.solutions.pose.Pose(
            static_image_mode=True,
            model_complexity=1,
            enable_segmentation=False,
            min_detection_confidence=0.45
        )
        pose_result = pose.process(rgb)
    except Exception:
        return None
    finally:
        try:
            if pose is not None:
                pose.close()
        except Exception:
            pass

    if not pose_result.pose_landmarks:
        return None

    h, w = rgb.shape[:2]
    lm = pose_result.pose_landmarks.landmark
    landmarks = {
        "left_shoulder": landmark_point(lm[11], w, h),
        "right_shoulder": landmark_point(lm[12], w, h),
        "left_hip": landmark_point(lm[23], w, h),
        "right_hip": landmark_point(lm[24], w, h),
        "left_elbow": landmark_point(lm[13], w, h),
        "right_elbow": landmark_point(lm[14], w, h),
        "left_knee": landmark_point(lm[25], w, h),
        "right_knee": landmark_point(lm[26], w, h),
        "left_ankle": landmark_point(lm[27], w, h),
        "right_ankle": landmark_point(lm[28], w, h),
        "nose": landmark_point(lm[0], w, h),
    }

    if not landmarks["left_shoulder"] or not landmarks["right_shoulder"]:
        return None

    return {
        "landmarks": landmarks,
        "source_size": (w, h),
    }


def extract_garment_cutout(img: Image.Image, role: str = "bride") -> Image.Image:
    pose_data = detect_pose_only(img)
    if not pose_data:
        return extract_foreground_cutout(img)

    rgb = np.array(img.convert("RGB"))
    h, w = rgb.shape[:2]
    landmarks = pose_data["landmarks"]

    ls = landmarks["left_shoulder"]
    rs = landmarks["right_shoulder"]
    lh = landmarks["left_hip"] or ls
    rh = landmarks["right_hip"] or rs
    lk = landmarks["left_knee"] or lh
    rk = landmarks["right_knee"] or rh
    la = landmarks["left_ankle"] or lk
    ra = landmarks["right_ankle"] or rk

    shoulder_mid_x = (ls[0] + rs[0]) / 2.0
    shoulder_y = min(ls[1], rs[1])
    hip_y = max(lh[1], rh[1]) if lh and rh else int(h * 0.56)
    knee_y = max(lk[1], rk[1]) if lk and rk else int(h * 0.82)
    ankle_y = max(la[1], ra[1]) if la and ra else int(h * 0.94)

    is_groom = role.lower() == "groom"
    top_y = max(0, int(shoulder_y - h * 0.02))
    waist_y = min(h - 1, int(shoulder_y + max(h * 0.14, (hip_y - shoulder_y) * 0.42)))
    hip_line_y = min(h - 1, int(hip_y + h * 0.01))
    hem_y = min(
        h - 1,
        int((knee_y if is_groom else ankle_y) + (h * 0.08 if is_groom else h * 0.04))
    )

    shoulder_dist = max(1, int(((ls[0] - rs[0]) ** 2 + (ls[1] - rs[1]) ** 2) ** 0.5))
    top_w = max(160, int(shoulder_dist * (1.10 if is_groom else 1.20)))
    waist_w = max(120, int(top_w * (0.78 if is_groom else 0.60)))
    hip_w = max(140, int(top_w * (0.92 if is_groom else 1.20)))
    hem_w = max(180, int(hip_w * (1.08 if is_groom else 1.85)))

    poly = [
        (clamp(shoulder_mid_x - top_w / 2, 0, w - 1), top_y),
        (clamp(shoulder_mid_x - waist_w / 2, 0, w - 1), waist_y),
        (clamp(shoulder_mid_x - hip_w / 2, 0, w - 1), hip_line_y),
        (clamp(shoulder_mid_x - hem_w / 2, 0, w - 1), hem_y),
        (clamp(shoulder_mid_x + hem_w / 2, 0, w - 1), hem_y),
        (clamp(shoulder_mid_x + hip_w / 2, 0, w - 1), hip_line_y),
        (clamp(shoulder_mid_x + waist_w / 2, 0, w - 1), waist_y),
        (clamp(shoulder_mid_x + top_w / 2, 0, w - 1), top_y),
    ]

    mask = Image.new("L", (w, h), 0)
    draw = ImageDraw.Draw(mask)
    draw.polygon(poly, fill=255)
    mask = mask.filter(ImageFilter.GaussianBlur(1.4))

    alpha = np.array(mask, dtype=np.uint8)
    foreground_ratio = float(np.count_nonzero(alpha)) / float(alpha.size)
    if foreground_ratio < 0.02:
        return extract_foreground_cutout(img)

    rgba = img.convert("RGBA")
    rgba.putalpha(mask)

    bbox = mask.getbbox()
    if bbox:
        left, top, right, bottom = bbox
        pad_x = max(8, int((right - left) * 0.06))
        pad_y = max(8, int((bottom - top) * 0.04))
        left = max(0, left - pad_x)
        top = max(0, top - pad_y)
        right = min(w, right + pad_x)
        bottom = min(h, bottom + pad_y)
        rgba = rgba.crop((left, top, right, bottom))

    return rgba


def draw_label(draw: ImageDraw.ImageDraw, text: str, xy: tuple[int, int]) -> None:
    try:
        font = ImageFont.truetype("arial.ttf", 26)
        small_font = ImageFont.truetype("arial.ttf", 16)
    except Exception:
        font = ImageFont.load_default()
        small_font = ImageFont.load_default()

    x, y = xy
    draw.rounded_rectangle(
        (x, y, x + 500, y + 108),
        radius=24,
        fill=(28, 25, 23, 210)
    )
    draw.text((x + 24, y + 16), text, fill=(255, 255, 255), font=font)
    draw.text(
        (x + 24, y + 58),
        "Virtual try-on preview",
        fill=(244, 196, 196),
        font=small_font
    )


def build_preview_try_on(
        person: Image.Image,
        garment: Image.Image,
        role: str,
        style: str
) -> Image.Image:
    canvas_size = (1024, 1536)
    background = cover_resize(person, canvas_size).filter(
        ImageFilter.GaussianBlur(radius=26)
    )
    background = ImageEnhance.Color(background).enhance(0.82)
    dim = Image.new("RGB", canvas_size, (24, 20, 18))
    background = Image.blend(background, dim, 0.34)

    person_layer = fit_resize(person, (630, 1380))
    person_x = (canvas_size[0] - person_layer.width) // 2
    person_y = canvas_size[1] - person_layer.height - 38
    person_layer = ImageEnhance.Contrast(person_layer).enhance(1.03)
    background.paste(person_layer, (person_x, person_y))

    is_groom = role.lower() == "groom"
    garment_box = (540, 720) if is_groom else (620, 920)
    garment_layer = cover_resize(extract_garment_cutout(garment, role), garment_box)
    garment_layer = ImageEnhance.Color(garment_layer).enhance(1.05)
    garment_layer = ImageEnhance.Contrast(garment_layer).enhance(1.08)
    garment_layer = garment_layer.filter(
        ImageFilter.UnsharpMask(radius=1.4, percent=132)
    )

    overlay = Image.new("RGBA", canvas_size, (0, 0, 0, 0))
    overlay_x = (canvas_size[0] - garment_layer.width) // 2
    overlay_y = 362 if is_groom else 406

    shadow = Image.new("RGBA", garment_layer.size, (0, 0, 0, 120))
    shadow_mask = rounded_mask(garment_layer.size, 42).filter(
        ImageFilter.GaussianBlur(16)
    )
    overlay.paste(shadow, (overlay_x + 12, overlay_y + 18), shadow_mask)

    mask = rounded_mask(garment_layer.size, 42)
    garment_rgba = garment_layer.convert("RGBA")
    garment_rgba.putalpha(238)
    overlay.paste(garment_rgba, (overlay_x, overlay_y), mask)

    result = Image.alpha_composite(background.convert("RGBA"), overlay)
    wash = Image.new("RGBA", canvas_size, (255, 238, 238, 32))
    result = Image.alpha_composite(result, wash)

    draw = ImageDraw.Draw(result, "RGBA")
    role_label = "Groom suit" if is_groom else "Bride dress"
    draw_label(draw, f"{role_label} - {style or 'studio'}", (56, 56))
    draw.rounded_rectangle(
        (56, 1394, 968, 1478),
        radius=24,
        fill=(255, 255, 255, 225)
    )
    try:
        note_font = ImageFont.truetype("arial.ttf", 20)
    except Exception:
        note_font = ImageFont.load_default()
    draw.text(
        (86, 1418),
        "Normalized JPG output. Use as AI try-on preview before studio fitting.",
        fill=(74, 64, 61),
        font=note_font
    )

    return result.convert("RGB")


def build_real_try_on_fallback(
        person: Image.Image,
        garment: Image.Image,
        role: str,
        style: str
) -> Image.Image:
    preview = build_preview_try_on(person, garment, role, style)
    return preview


def build_real_try_on(
        person: Image.Image,
        garment: Image.Image,
        role: str,
        style: str
) -> Image.Image:
    features = detect_body_features(person)
    if not features:
        return build_real_try_on_fallback(person, garment, role, style)

    canvas_size = (1024, 1536)
    source_size = features["source_size"]
    landmarks = features["landmarks"]
    mask = features["mask"]

    background = cover_resize(person, canvas_size).filter(
        ImageFilter.GaussianBlur(radius=22)
    )
    background = ImageEnhance.Color(background).enhance(0.84)
    background = Image.blend(background, Image.new("RGB", canvas_size, (24, 20, 18)), 0.32)

    if mask is not None:
        alpha = Image.fromarray((mask * 255).astype(np.uint8), mode="L")
        fg = person.convert("RGBA")
        fg.putalpha(alpha)
        fg_canvas = cover_resize(fg, canvas_size)
        base = Image.alpha_composite(background.convert("RGBA"), fg_canvas)
    else:
        base = background.convert("RGBA")

    ls = map_point_to_cover(landmarks["left_shoulder"], source_size, canvas_size)
    rs = map_point_to_cover(landmarks["right_shoulder"], source_size, canvas_size)
    lh = map_point_to_cover(landmarks["left_hip"] or landmarks["left_shoulder"], source_size, canvas_size)
    rh = map_point_to_cover(landmarks["right_hip"] or landmarks["right_shoulder"], source_size, canvas_size)

    shoulder_mid = ((ls[0] + rs[0]) // 2, (ls[1] + rs[1]) // 2)
    hip_mid = ((lh[0] + rh[0]) // 2, (lh[1] + rh[1]) // 2)
    shoulder_dist = max(1, int(((ls[0] - rs[0]) ** 2 + (ls[1] - rs[1]) ** 2) ** 0.5))
    hip_dist = max(1, int(((lh[0] - rh[0]) ** 2 + (lh[1] - rh[1]) ** 2) ** 0.5))

    top_y = min(ls[1], rs[1]) + 8
    waist_y = shoulder_mid[1] + max(110, int((hip_mid[1] - shoulder_mid[1]) * 0.42))
    hip_y = hip_mid[1] - 8
    hem_y = min(canvas_size[1] - 42, hip_mid[1] + max(240, int((hip_mid[1] - shoulder_mid[1]) * 1.2)))

    is_groom = role.lower() == "groom"
    if is_groom:
        top_w = max(170, int(shoulder_dist * 0.84))
        waist_w = max(150, int(top_w * 0.78))
        hip_w = max(160, int(hip_dist * 0.9))
        hem_w = max(170, int(hip_w * 1.05))
        garment_top = max(0, top_y - 10)
        garment_bottom = min(canvas_size[1] - 1, hem_y + 18)
    else:
        top_w = max(210, int(shoulder_dist * 1.18))
        waist_w = max(160, int(top_w * 0.56))
        hip_w = max(180, int(max(hip_dist * 1.18, top_w * 0.85)))
        hem_w = max(320, int(hip_w * 1.35))
        garment_top = max(0, top_y - 8)
        garment_bottom = min(canvas_size[1] - 1, hem_y + 34)

    center_x = shoulder_mid[0]
    poly = [
        (clamp(center_x - top_w / 2, 0, canvas_size[0] - 1), garment_top),
        (clamp(center_x - waist_w / 2, 0, canvas_size[0] - 1), waist_y),
        (clamp(center_x - hip_w / 2, 0, canvas_size[0] - 1), hip_y),
        (clamp(center_x - hem_w / 2, 0, canvas_size[0] - 1), garment_bottom),
        (clamp(center_x + hem_w / 2, 0, canvas_size[0] - 1), garment_bottom),
        (clamp(center_x + hip_w / 2, 0, canvas_size[0] - 1), hip_y),
        (clamp(center_x + waist_w / 2, 0, canvas_size[0] - 1), waist_y),
        (clamp(center_x + top_w / 2, 0, canvas_size[0] - 1), garment_top),
    ]

    bbox_left = min(p[0] for p in poly)
    bbox_right = max(p[0] for p in poly)
    bbox_top = min(p[1] for p in poly)
    bbox_bottom = max(p[1] for p in poly)
    bbox_width = max(1, bbox_right - bbox_left)
    bbox_height = max(1, bbox_bottom - bbox_top)

    garment_fit = cover_resize(extract_garment_cutout(garment, role), (bbox_width, bbox_height))
    garment_fit = ImageEnhance.Color(garment_fit).enhance(1.06)
    garment_fit = ImageEnhance.Contrast(garment_fit).enhance(1.09)
    garment_fit = garment_fit.filter(ImageFilter.UnsharpMask(radius=1.2, percent=125))

    garment_layer = Image.new("RGBA", canvas_size, (0, 0, 0, 0))
    garment_layer.paste(garment_fit.convert("RGBA"), (bbox_left, bbox_top))

    mask_layer = Image.new("L", canvas_size, 0)
    ImageDraw.Draw(mask_layer).polygon(poly, fill=255)
    mask_layer = mask_layer.filter(ImageFilter.GaussianBlur(1.6))

    shadow_layer = Image.new("RGBA", canvas_size, (0, 0, 0, 0))
    shadow_box = (
        bbox_left + 10,
        bbox_top + 16,
        bbox_left + bbox_width + 10,
        bbox_top + bbox_height + 16
    )
    shadow = Image.new("RGBA", (bbox_width, bbox_height), (0, 0, 0, 98))
    shadow_mask = Image.new("L", (bbox_width, bbox_height), 0)
    ImageDraw.Draw(shadow_mask).polygon(
        [(p[0] - bbox_left, p[1] - bbox_top) for p in poly],
        fill=255
    )
    shadow_mask = shadow_mask.filter(ImageFilter.GaussianBlur(12))
    shadow_layer.paste(shadow, (shadow_box[0], shadow_box[1]), shadow_mask)

    result = Image.alpha_composite(base, shadow_layer)
    garment_rgba = garment_layer.copy()
    garment_rgba.putalpha(mask_layer)
    result = Image.alpha_composite(result, garment_rgba)

    if role.lower() == "bride":
        glow = Image.new("RGBA", canvas_size, (255, 234, 240, 24))
        result = Image.alpha_composite(result, glow)
    else:
        glow = Image.new("RGBA", canvas_size, (234, 240, 255, 18))
        result = Image.alpha_composite(result, glow)

    draw = ImageDraw.Draw(result, "RGBA")
    draw_label(draw, f"Real try-on - {style or 'studio'}", (56, 56))
    draw.rounded_rectangle(
        (56, 1394, 968, 1478),
        radius=24,
        fill=(255, 255, 255, 225)
    )
    try:
        note_font = ImageFont.truetype("arial.ttf", 20)
    except Exception:
        note_font = ImageFont.load_default()
    draw.text(
        (86, 1418),
        "Pose-guided real mode using body landmarks and segmentation.",
        fill=(74, 64, 61),
        font=note_font
    )

    return result.convert("RGB")


# ==========================
# Home API
# ==========================
@app.get("/")
def home():
    return {
        "message":
        "Wedding Dress AI Service Running"
    }


# ==========================
# Predict API
# ==========================
@app.post("/predict")
async def predict(
        file: UploadFile = File(...)
):

    try:

        # đọc ảnh upload
        img = await read_supported_image(file)

        # resize
        img = img.resize(IMG_SIZE)

        # convert numpy
        img_array = image.img_to_array(img)

        # normalize
        img_array = img_array / 255.0

        # expand dim
        img_array = np.expand_dims(
            img_array,
            axis=0
        )

        # predict
        prediction = model.predict(
            img_array
        )

        predicted_index = np.argmax(
            prediction
        )

        predicted_class = class_labels[
            predicted_index
        ]

        confidence = float(
            np.max(prediction)
        )

        return {
            "success": True,
            "body_shape":
                predicted_class,

            "confidence":
                round(confidence, 4)
        }

    except Exception as e:

        return {
            "success": False,
            "error": str(e)
        }


@app.post("/try-on")
async def wedding_try_on(
        person: UploadFile = File(...),
        garment: UploadFile = File(...),
        role: str = Form("bride"),
        style: str = Form("studio"),
        mode: str = Form(TRY_ON_PREVIEW)
):
    person_img = await read_supported_image(person)
    garment_img = await read_supported_image(garment)

    if mode == TRY_ON_REAL:
        result = build_real_try_on(
            person=person_img,
            garment=garment_img,
            role=role,
            style=style
        )
        resolved_mode = TRY_ON_REAL
    else:
        result = build_preview_try_on(
            person=person_img,
            garment=garment_img,
            role=role,
            style=style
        )
        resolved_mode = TRY_ON_PREVIEW

    buffer = io.BytesIO()
    result.save(
        buffer,
        format="JPEG",
        quality=90,
        optimize=True,
        progressive=True
    )
    buffer.seek(0)

    return StreamingResponse(
        buffer,
        media_type="image/jpeg",
        headers={
            "X-Try-On-Role": role,
            "X-Try-On-Style": style,
            "X-Try-On-Mode": resolved_mode,
        }
    )
