package com.prueba.transport_orders.service;

import org.springframework.web.multipart.MultipartFile;


import java.util.UUID;

public interface FileStorageService {

     String storeDocument(UUID assignmentId, MultipartFile file);

    String storeImage(UUID assignmentId, MultipartFile file);

}
