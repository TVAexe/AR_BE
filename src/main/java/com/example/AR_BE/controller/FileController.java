package com.example.AR_BE.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.AR_BE.domain.request.FileNameDTORequest;
import com.example.AR_BE.domain.response.FileURLDTOResponse;
import com.example.AR_BE.service.FileService;

@RestController
@RequestMapping("/api/v1")
public class FileController {
    private final FileService fileService;
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/files/multiple")
    public ResponseEntity<FileURLDTOResponse> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String fileUrl = fileService.uploadFile(file);
                fileUrls.add(fileUrl);
            } catch (IOException e) {
                return ResponseEntity.badRequest().body(null);
            }
        }
        return ResponseEntity.ok(new FileURLDTOResponse(fileUrls));
    }


    @PostMapping("/files/urls")
    public ResponseEntity<FileURLDTOResponse> getFileUrls(@RequestBody FileNameDTORequest fileNames) {
        List<String> fileUrls = new ArrayList<>();
        if (fileNames.getFileNames() == null) {
            return ResponseEntity.badRequest().body(null);
        }
        for (String fileName : fileNames.getFileNames()) {
            try {
                String fileUrl = fileService.getFileUrl(fileName);
                fileUrls.add(fileUrl);
            } catch (Exception e) {
                continue;
            }
        }
        return ResponseEntity.ok(new FileURLDTOResponse(fileUrls));
    }
}
