package com.petadoption.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.petadoption.exception.BusinessException;
import com.petadoption.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryImageStorageServiceImpl implements ImageStorageService {

    private final Cloudinary cloudinary;

    @Override
    public ImageUploadResult uploadImage(MultipartFile file, String folder) {

        try {

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder));

            return new ImageUploadResult(
                    (String) result.get("secure_url"),
                    (String) result.get("public_id"));

        } catch (IOException ex) {

            throw new BusinessException(
                    "Failed to upload image: " + ex.getMessage());
        }
    }

    @Override
    public void deleteImage(String publicId) {

        try {

            cloudinary.uploader().destroy(
                    publicId, ObjectUtils.emptyMap());

        } catch (IOException ex) {

            throw new BusinessException(
                    "Failed to delete image: " + ex.getMessage());
        }
    }
}
