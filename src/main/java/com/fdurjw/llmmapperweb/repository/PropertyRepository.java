package com.fdurjw.llmmapperweb.repository;

import com.fdurjw.llmmapperweb.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}