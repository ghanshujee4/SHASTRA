package com.library.sdl;

import net.coobird.thumbnailator.Thumbnails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${file.upload-dir}")  // Defined in application.properties
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        logger.info("Received file: {}", file.getOriginalFilename());

        // Ensure directory exists
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            logger.info("Creating upload directory: {}", uploadDir);
            dir.mkdirs();
        }

        // Extract file details
        String originalFileName = file.getOriginalFilename();
        String extension = getFileExtension(originalFileName).toLowerCase();
        logger.info("File extension detected: {}", extension);

        // Generate unique file name
        String newFileName = System.currentTimeMillis() + "." + extension;
        Path targetLocation = Paths.get(uploadDir, newFileName);
        logger.info("Generated unique filename: {}", newFileName);

        // Handle different file types
        if (extension.equals("svg") || extension.equals("pdf")) {
            logger.info("Saving {} file without compression", extension);
            Files.copy(file.getInputStream(), targetLocation);
        } else if (isImageFormat(extension)) {
            logger.info("Compressing image before saving: {}", newFileName);
            compressImage(file, targetLocation.toFile(), extension);
        } else {
            logger.error("Unsupported file format: {}", extension);
            throw new IOException("Unsupported file format: " + extension);
        }

        logger.info("File successfully saved at: {}", targetLocation);
        return targetLocation.toString();
    }

    private void compressImage(MultipartFile file, File outputFile, String format) throws IOException {
        // Read image file
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            logger.error("Invalid image format: {}", file.getOriginalFilename());
            throw new IOException("Unsupported image format: " + file.getOriginalFilename());
        }

        double quality = 0.9;
        long fileSize = Long.MAX_VALUE;

        logger.info("Starting image compression: {}", file.getOriginalFilename());

        // Compress until file size ≤ 200 KB
        while (fileSize > 200 * 1024 && quality > 0.1) {
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                Thumbnails.of(image)
                        .size(800, 800)  // Resize for better compression
                        .outputFormat(format)
                        .outputQuality(quality)
                        .toOutputStream(fos);
            }
            fileSize = outputFile.length();
            logger.info("Compressed file size: {} KB, Quality: {}", fileSize / 1024, quality);
            quality -= 0.2;
        }

        logger.info("Final compressed file size: {} KB", outputFile.length() / 1024);
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1);
    }

    private boolean isImageFormat(String format) {
        return format.equals("jpg") || format.equals("jpeg") || format.equals("png") || format.equals("bmp") || format.equals("gif");
    }
}
