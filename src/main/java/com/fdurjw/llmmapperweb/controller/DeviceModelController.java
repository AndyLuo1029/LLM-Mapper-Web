package com.fdurjw.llmmapperweb.controller;

import com.fdurjw.llmmapperweb.dto.DeviceModelDTO;
import com.fdurjw.llmmapperweb.model.DeviceModel;
import com.fdurjw.llmmapperweb.service.DeviceModelService;
import com.fdurjw.llmmapperweb.service.MapperGenerateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;

@RestController
@RequestMapping("/deviceModels")
@CrossOrigin(origins = "*")
public class DeviceModelController {
    @Autowired
    private DeviceModelService deviceModelService;

    @Autowired
    private MapperGenerateService mapperGenerateService;

    @PostMapping("/addDeviceModel")
    public ResponseEntity<DeviceModel> createDeviceModel(@RequestBody DeviceModelDTO deviceModelDTO) {
        try {
            // 1. 保存数据库信息
            DeviceModel deviceModel = deviceModelService.saveDeviceModel(deviceModelDTO);

            // 2. 调用生成函数，生成 mapper 并打包
            String generatedFilePath = mapperGenerateService.generateMapperCode(deviceModelDTO.getName());

            // 3. 将打包文件路径存入数据库
            deviceModel.setFilePath(generatedFilePath);
            deviceModel = deviceModelService.updateDeviceModelCodeFile(deviceModel);

            return ResponseEntity.status(201).body(deviceModel);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadDeviceModel(@RequestParam String devicemodel) {
        try {
            // Logic to fetch the compressed file (e.g., a zip file) using the devicemodel identifier
            File file = deviceModelService.fetchDeviceModelFile(devicemodel);

            // Check if the file exists
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            // Create a resource from the file
            Resource resource = new FileSystemResource(file);

            // Return the file as a response with the proper headers for a downloadable .zip file
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)  // MIME type for binary content
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(null); // Return error with correct status
        }
    }
}
