package com.fdurjw.llmmapperweb.model;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "properties")
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String propertyName;
    private String propertyType;
    private String propertyDescrip;
    private String accessMode;

    @ManyToOne
    @JoinColumn(name = "device_model_id", nullable = false)  // Updated to device_model_id
    private DeviceModel deviceModel;  // Updated to deviceModel to match your new field name
}
