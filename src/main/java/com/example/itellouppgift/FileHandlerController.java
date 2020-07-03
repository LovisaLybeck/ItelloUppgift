package com.example.itellouppgift;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/handler")
public class FileHandlerController {

    private final FileHandlerService fileHandlerService;

    public FileHandlerController(FileHandlerService fileHandlerService) {
        this.fileHandlerService = fileHandlerService;
    }

    @PostMapping(value="/inputFile")
    public ResponseEntity postFile(@RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok().body(fileHandlerService.inputFile(file));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
