package com.petadoption.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {

    ImageUploadResult uploadImage(MultipartFile file, String folder);

    void deleteImage(String publicId);

    record ImageUploadResult(String url, String publicId) {
    }
}
