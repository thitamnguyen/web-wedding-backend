package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.dto.ImageUploadResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class CloudinaryService {

    private static final String DEFAULT_FOLDER = "concepts";

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public ImageUploadResult uploadImage(MultipartFile file) {
        return uploadImage(file, DEFAULT_FOLDER);
    }

    public ImageUploadResult uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );

            return new ImageUploadResult(
                    (String) uploadResult.get("secure_url"),
                    (String) uploadResult.get("public_id")
            );
        } catch (Exception e) {
            throw new RuntimeException("Khong the upload anh len Cloudinary", e);
        }
    }

    public void deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image", "invalidate", true));
        } catch (Exception e) {
            throw new RuntimeException("Khong the xoa anh tren Cloudinary", e);
        }
    }
}
