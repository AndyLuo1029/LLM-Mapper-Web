package com.fdurjw.llmmapperweb.dto;

import lombok.Data;

@Data
public class ActionDTO {
    private String name;
    private String description;
    private String inputSchema;
    private String outputSchema;
}