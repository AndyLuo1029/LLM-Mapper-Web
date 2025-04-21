package com.fdurjw.llmmapperweb.repository;

import com.fdurjw.llmmapperweb.model.DeviceModel;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeviceModelRepository extends JpaRepository<DeviceModel, Long> {
    DeviceModel findByDeviceModelName(String deviceModelName);


    @Modifying
    @Transactional
    @Query("UPDATE DeviceModel dm SET dm.filePath = :filePath WHERE dm.deviceModelName = :deviceModelName")
    int updateDeviceModelFilePath(@Param("filePath") String filePath, @Param("deviceModelName") String deviceModelName);
}