package com.fdurjw.llmmapperweb.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeviceModelDTO {
    private String name;
    private String protocol;
    private List<PropertyDTO> properties;
    private List<ActionDTO> actions;
}