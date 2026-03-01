package com.badmintonshop.service;

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service for interacting with Firebase Cloud Storage.
 * <p>
 * Provides file upload capabilities used primarily by the data seeders
 * to upload product images from classpath resources into Firebase Storage
 * and retrieve their public-accessible download URLs.
 * </p>
 */
@Service
@Slf4j
public class FirebaseStorageService {

    /**
     * Uploads a file from an {@link InputStream} to Firebase Storage and returns its public URL.
     * <p>
     * After uploading, this method explicitly grants public read access ({@code allUsers})
     * to the object via ACL. This is required because the Firebase Admin SDK does not
     * automatically add a download token (unlike the Firebase Console), so without public
     * ACL the generated {@code ?alt=media} URL would return a 403 Forbidden error.
     * </p>
     *
     * @param inputStream The input stream of the file content to upload.
     * @param fileName    The destination path/name within the bucket (e.g., {@code images/product.jpg}).
     * @param contentType The MIME type of the file (e.g., {@code image/jpeg}).
     * @return The public download URL of the uploaded file.
     * @throws RuntimeException If the upload or ACL operation fails.
     */
    public String uploadFileFromStream(InputStream inputStream, String fileName, String contentType) {
        try {
            Bucket bucket = StorageClient.getInstance().bucket();

            Blob blob = bucket.create(fileName, inputStream, contentType);

            // Grant public read access so the ?alt=media URL is accessible without a token.
            // The Firebase Admin SDK does not add a download token automatically.
            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

            String publicUrl = String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucket.getName(),
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8)
            );

            log.info("File uploaded successfully to Firebase Storage. Path: '{}', URL: {}", fileName, publicUrl);
            return publicUrl;

        } catch (Exception e) {
            log.error("Failed to upload file to Firebase Storage. Path: '{}'. Reason: {}", fileName, e.getMessage());
            throw new RuntimeException("Failed to upload file to Firebase Storage: " + fileName, e);
        }
    }
}
