package fit.se.be_phone_store.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import fit.se.be_phone_store.config.CloudinaryConfig;
import fit.se.be_phone_store.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Cloudinary Service for image upload
 */
@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    /**
     * Upload image to Cloudinary
     * @param file MultipartFile to upload
     * @param folder Folder path in Cloudinary
     * @return URL of uploaded image
     */
    public String uploadImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }

        // Validate file format
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !CloudinaryConfig.isValidImageFormat(originalFilename)) {
            throw new FileStorageException("Invalid image format. Allowed: jpg, jpeg, png, gif, webp");
        }

        try {
            // Prepare upload options
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("folder", folder);
            uploadOptions.put("resource_type", "image");
            uploadOptions.put("use_filename", true);
            uploadOptions.put("unique_filename", true);
            uploadOptions.put("overwrite", false);
            uploadOptions.put("quality", "auto:good");
            uploadOptions.put("fetch_format", "auto");
            
            // Check if we should use unsigned upload (for development/demo)
            // Unsigned upload requires an upload preset in Cloudinary dashboard
            String cloudName = cloudinaryConfig.getCloudName();
            if ("demo".equals(cloudName) || "your-cloud-name".equals(cloudName) || 
                cloudName == null || cloudName.isEmpty()) {
                // For unsigned upload, you need to:
                // 1. Go to Cloudinary Dashboard > Settings > Upload
                // 2. Create an unsigned upload preset
                // 3. Add it to application.properties as cloudinary.upload-preset
                // For now, we'll skip unsigned and let it fail with a clear error message
            }

            // Upload file
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    uploadOptions
            );

            // Get secure URL
            String secureUrl = (String) uploadResult.get("secure_url");
            if (secureUrl == null) {
                secureUrl = (String) uploadResult.get("url");
            }

            return secureUrl;

        } catch (IOException e) {
            throw new FileStorageException("Failed to upload image: " + e.getMessage());
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Invalid Signature")) {
                throw new FileStorageException(
                    "Cloudinary configuration error: Invalid API credentials. " +
                    "Please update cloudinary.api-key and cloudinary.api-secret in application.properties with your actual Cloudinary credentials."
                );
            }
            throw new FileStorageException("Upload failed: " + errorMessage);
        }
    }

    /**
     * Upload user avatar
     * @param file MultipartFile to upload
     * @return URL of uploaded avatar
     */
    public String uploadAvatar(MultipartFile file) {
        return uploadImage(file, "phone-ecommerce/avatars");
    }

    /**
     * Delete image from Cloudinary
     * @param imageUrl URL of image to delete
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            String publicId = CloudinaryConfig.extractPublicId(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            // Log error but don't throw - deletion is not critical
            System.err.println("Failed to delete image from Cloudinary: " + e.getMessage());
        }
    }
}

