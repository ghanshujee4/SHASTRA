package com.library.sdl.FileDownloadController;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileDownloadController {

    @GetMapping("/adhar/{fileName}")
    public ResponseEntity<FileSystemResource> downloadAdhar(@PathVariable String fileName) {
        File file = new File("uploads/adharCards/" + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(new FileSystemResource(file));
    }
}

