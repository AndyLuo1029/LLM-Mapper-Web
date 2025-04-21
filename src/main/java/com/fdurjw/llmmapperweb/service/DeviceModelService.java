package com.fdurjw.llmmapperweb.service;

import com.fdurjw.llmmapperweb.dto.DeviceModelDTO;
import com.fdurjw.llmmapperweb.model.DeviceModel;
import com.fdurjw.llmmapperweb.model.Property;
import com.fdurjw.llmmapperweb.model.Action;
import com.fdurjw.llmmapperweb.repository.DeviceModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.List;

@Service
public class DeviceModelService {
    @Autowired
    private DeviceModelRepository deviceModelRepository;

    public DeviceModel saveDeviceModel(DeviceModelDTO deviceModelDTO) {
        DeviceModel deviceModel = new DeviceModel();
        deviceModel.setDeviceModelName(deviceModelDTO.getName());
        deviceModel.setProtocolName(deviceModelDTO.getProtocol());

        // 处理 properties
        List<Property> properties = deviceModelDTO.getProperties().stream().map(propDTO -> {
            Property property = new Property();
            property.setPropertyName(propDTO.getName());
            property.setPropertyType(propDTO.getType());
            property.setPropertyDescrip(propDTO.getDescription());
            property.setAccessMode(propDTO.getAccessMode());
            property.setDeviceModel(deviceModel);
            return property;
        }).toList();
        deviceModel.setProperties(properties);

        // 处理 actions
        List<Action> actions = deviceModelDTO.getActions().stream().map(actDTO -> {
            Action action = new Action();
            action.setActionName(actDTO.getName());
            action.setActionDescrip(actDTO.getDescription());
            action.setInputSchema(actDTO.getInputSchema());
            action.setOutputSchema(actDTO.getOutputSchema());
            action.setDeviceModel(deviceModel);
            return action;
        }).toList();
        deviceModel.setActions(actions);

        return deviceModelRepository.save(deviceModel);
    }

    // Method to fetch the device model file
    public File fetchDeviceModelFile(String devicemodel) {
        // Fetch the device model from the database based on the devicemodel name
        DeviceModel deviceModel = deviceModelRepository.findByDeviceModelName(devicemodel);

        if (deviceModel == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "DeviceModel not found for devicemodel: " + devicemodel);
        }

        // Get the file path from the DeviceModel (assuming the filePath is a field in DeviceModel)
        String filePath = deviceModel.getFilePath(); // Adjust this if the path is stored elsewhere

        if (filePath == null || filePath.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not created for devicemodel: " + devicemodel);
        }

        // Construct the file object using the stored path
        File file = new File(filePath);

        // Check if file exists and is a .zip file
        if (file.exists() && file.getName().endsWith(".zip")) {
            return file; // Return the compressed file
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compressed file not found for devicemodel: " + devicemodel);
        }
    }

    // Method to update deviceModel after generated Mapper code
    public DeviceModel updateDeviceModelCodeFile(DeviceModel deviceModel) {
        // Find the DeviceModel by its name
        DeviceModel existingDeviceModel = deviceModelRepository.findByDeviceModelName(deviceModel.getDeviceModelName());

        // If the DeviceModel is not found, throw an exception
        if (existingDeviceModel == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DeviceModel not found for devicemodel: " + deviceModel.getDeviceModelName());
        }

        // Only update the filePath field
        int updatedRows = deviceModelRepository.updateDeviceModelFilePath(deviceModel.getFilePath(), deviceModel.getDeviceModelName());

        // Check if the update was successful
        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update the file path");
        }

        // Return the updated DeviceModel with the new filePath
        existingDeviceModel.setFilePath(deviceModel.getFilePath());
        return existingDeviceModel;
    }
}