package fit.se.be_phone_store.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Cloudinary Configuration (Simplified)
 * Configures Cloudinary client for image management
 */
@Configuration
@Slf4j
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Value("${cloudinary.secure:true}")
    private boolean secure;

    @Value("${cloudinary.upload.folder:phone-ecommerce/products}")
    private String uploadFolder;

    @Value("${cloudinary.upload.use-filename:true}")
    private boolean useFilename;

    @Value("${cloudinary.upload.unique-filename:true}")
    private boolean uniqueFilename;

    @Value("${cloudinary.upload.overwrite:false}")
    private boolean overwrite;

    /**
     * Cloudinary Bean Configuration
     */
    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", String.valueOf(secure));

        log.info("Initializing Cloudinary with cloud_name: {}", cloudName);

        return new Cloudinary(config);
    }

    /**
     * Default Upload Options Bean
     */
    @Bean
    public Map<String, Object> defaultUploadOptions() {
        return ObjectUtils.asMap(
                "folder", uploadFolder,
                "use_filename", useFilename,
                "unique_filename", uniqueFilename,
                "overwrite", overwrite,
                "resource_type", "auto",
                "quality", "auto",
                "fetch_format", "auto"
        );
    }

    /**
     * Product Image Upload Options
     */
    @Bean
    public Map<String, Object> productImageUploadOptions() {
        return ObjectUtils.asMap(
                "folder", uploadFolder + "/images",
                "use_filename", useFilename,
                "unique_filename", uniqueFilename,
                "overwrite", overwrite,
                "resource_type", "image",
                "quality", "auto:good",
                "fetch_format", "auto",
                "width", 1000,
                "height", 1000,
                "crop", "limit",
                "allowed_formats", "jpg,png,jpeg,webp"
        );
    }

    /**
     * Thumbnail Image Upload Options
     */
    @Bean
    public Map<String, Object> thumbnailUploadOptions() {
        return ObjectUtils.asMap(
                "folder", uploadFolder + "/thumbnails",
                "use_filename", useFilename,
                "unique_filename", uniqueFilename,
                "overwrite", overwrite,
                "resource_type", "image",
                "quality", "auto:good",
                "fetch_format", "auto",
                "width", 300,
                "height", 300,
                "crop", "fill",
                "gravity", "center"
        );
    }

    // Getter methods for configuration values
    public String getCloudName() { return cloudName; }
    public String getApiKey() { return apiKey; }
    public String getUploadFolder() { return uploadFolder; }
    public boolean isUseFilename() { return useFilename; }
    public boolean isUniqueFilename() { return uniqueFilename; }
    public boolean isOverwrite() { return overwrite; }

    // Image transformation constants
    public static final String THUMBNAIL_TRANSFORMATION = "w_200,h_200,c_fill,q_auto,f_auto";
    public static final String MEDIUM_TRANSFORMATION = "w_500,h_500,c_fill,q_auto,f_auto";
    public static final String LARGE_TRANSFORMATION = "w_1000,h_1000,c_fill,q_auto,f_auto";
    public static final String HERO_TRANSFORMATION = "w_1200,h_600,c_fill,q_auto,f_auto";
    public static final String PRODUCT_CARD_TRANSFORMATION = "w_400,h_400,c_fill,q_auto,f_auto,g_center";
    public static final String MOBILE_TRANSFORMATION = "w_300,h_300,c_fill,q_auto:low,f_auto";
    public static final String WEBP_TRANSFORMATION = "q_auto,f_webp";

    /**
     * Extract public ID from Cloudinary URL
     */
    public static String extractPublicId(String cloudinaryUrl) {
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            return null;
        }

        try {
            // Extract public ID from URL format:
            // https://res.cloudinary.com/cloud_name/image/upload/v123456/folder/public_id.jpg
            String[] parts = cloudinaryUrl.split("/");
            String fileNameWithExtension = parts[parts.length - 1];
            String fileName = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));

            // Include folder structure if present
            if (parts.length > 7) {
                StringBuilder publicId = new StringBuilder();
                for (int i = 7; i < parts.length - 1; i++) {
                    publicId.append(parts[i]).append("/");
                }
                publicId.append(fileName);
                return publicId.toString();
            }

            return fileName;
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", cloudinaryUrl, e);
            return null;
        }
    }

    /**
     * Build transformation URL
     */
    public static String buildTransformationUrl(String baseUrl, String transformation) {
        if (baseUrl == null || transformation == null) {
            return baseUrl;
        }

        return baseUrl.replace("/upload/", "/upload/" + transformation + "/");
    }

    /**
     * Validate image format
     */
    public static boolean isValidImageFormat(String filename) {
        if (filename == null) return false;

        String lowercaseFilename = filename.toLowerCase();
        return lowercaseFilename.endsWith(".jpg") ||
                lowercaseFilename.endsWith(".jpeg") ||
                lowercaseFilename.endsWith(".png") ||
                lowercaseFilename.endsWith(".webp") ||
                lowercaseFilename.endsWith(".gif");
    }

    /**
     * Generate responsive image URLs
     */
    public static Map<String, String> generateResponsiveUrls(String baseUrl) {
        Map<String, String> responsiveUrls = new HashMap<>();

        responsiveUrls.put("thumbnail", buildTransformationUrl(baseUrl, THUMBNAIL_TRANSFORMATION));
        responsiveUrls.put("medium", buildTransformationUrl(baseUrl, MEDIUM_TRANSFORMATION));
        responsiveUrls.put("large", buildTransformationUrl(baseUrl, LARGE_TRANSFORMATION));
        responsiveUrls.put("mobile", buildTransformationUrl(baseUrl, MOBILE_TRANSFORMATION));
        responsiveUrls.put("webp", buildTransformationUrl(baseUrl, WEBP_TRANSFORMATION));

        return responsiveUrls;
    }
}