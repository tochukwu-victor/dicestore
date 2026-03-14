package com.victoruk.dicestore.infrastructure.cloudService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.victoruk.dicestore.common.exception.ImageUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;



@Slf4j
@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(CloudinaryProperties props) {
        this.cloudinary = new Cloudinary(Map.of(
                "cloud_name", props.cloudName(),
                "api_key", props.apiKey(),
                "api_secret", props.apiSecret(),
                "secure", props.secure()
        ));
    }

    public String extractPublicId(Map<String, Object> uploadResult) {
        if (uploadResult.containsKey("public_id")) {
            return (String) uploadResult.get("public_id");
        }
        throw new ImageUploadException("Public ID not found in Cloudinary response", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public Map<String, Object> upload(MultipartFile multipartFile) {
        File file = null;
        try {
            file = convert(multipartFile);
            Map<String, Object> options = Map.of("folder", "ecommerce_image");
            return cloudinary.uploader().upload(file, options);
        } catch (IOException e) {
            throw new ImageUploadException("File conversion failed", e, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            throw new ImageUploadException("Cloudinary service is currently unreachable", e, HttpStatus.SERVICE_UNAVAILABLE);
        } finally {
            if (file != null) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (IOException e) {
                    log.error("Warning: Failed to delete temporary file {}", file.getAbsolutePath());
                }
            }
        }
    }

    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new ImageUploadException("Failed to delete image from cloud storage", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private File convert(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload-", "-" + Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream fo = new FileOutputStream(tempFile)) {
            fo.write(multipartFile.getBytes());
        }
        return tempFile;
    }
}