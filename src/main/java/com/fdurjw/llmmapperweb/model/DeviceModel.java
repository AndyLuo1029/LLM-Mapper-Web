package com.fdurjw.llmmapperweb.model;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "devices")
public class DeviceModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceModelName;
    private String protocolName;
    private String filePath;

    @OneToMany(mappedBy = "deviceModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Property> properties;

    @OneToMany(mappedBy = "deviceModel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Action> actions;
}