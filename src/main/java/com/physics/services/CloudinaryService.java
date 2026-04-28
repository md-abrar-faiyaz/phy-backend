package com.physics.services;



import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Uploads any file type (PDF, JPEG, etc.) to Cloudinary.
     * "resource_type" -> "auto" is the secret to making PDFs work!
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // Use "auto" so Cloudinary detects if it's an image, video, or raw file
        Map params = ObjectUtils.asMap(
                "resource_type", "auto",
                "use_filename", true,
                "unique_filename", true
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);


        return uploadResult.get("secure_url").toString();
    }
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.contains("cloudinary.com")) return;

        // 1. Extract the publicId correctly
        // Example: .../v1234567/my_pdf_file.pdf -> my_pdf_file
        String[] parts = fileUrl.split("/");
        String fileNameWithExtension = parts[parts.length - 1];
        String publicId = fileNameWithExtension.contains(".")
                ? fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'))
                : fileNameWithExtension;

        try {
            // 2. Try deleting as 'image' (for JPG/PNG)
            Map resultImage = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));

            // 3. Try deleting as 'raw' (for PDFs)
            // Note: Raw files often need the extension in the publicId
            Map resultRaw = cloudinary.uploader().destroy(fileNameWithExtension, ObjectUtils.asMap("resource_type", "raw"));
        } catch (Exception e) {
            System.err.println("Failed to delete from Cloudinary: " + e.getMessage());
        }
    }

    public void deleteMultipleFiles(List<String> urls) throws IOException {
        if (urls == null || urls.isEmpty()) return;

        for (String url : urls) {
            deleteFile(url); // Reuse the robust single-file logic
        }
        System.out.println("Batch deletion complete for " + urls.size() + " files.");
    }

    // Inside uploadFile or a new helper method
    public String getDownloadableUrl(String secureUrl) {
        // Inserts the 'attachment' flag into the URL to force download
        return secureUrl.replace("/upload/", "/upload/fl_attachment/");
    }
}
