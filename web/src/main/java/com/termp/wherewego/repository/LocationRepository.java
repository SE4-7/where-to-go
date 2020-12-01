package com.termp.wherewego.repository;


import com.termp.wherewego.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, String> {
}
