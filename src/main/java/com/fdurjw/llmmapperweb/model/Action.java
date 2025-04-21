package com.fdurjw.llmmapperweb.model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "actions")
public class Action {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String actionName;
    private String actionDescrip;

    @Column(columnDefinition = "TEXT")
    private String inputSchema;

    @Column(columnDefinition = "TEXT")
    private String outputSchema;

    @ManyToOne
    @JoinColumn(name = "device_model_id", nullable = false)  // Updated to device_model_id
    private DeviceModel deviceModel;  // Updated to deviceModel to match your new field name
}