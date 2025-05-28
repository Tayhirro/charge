package com.bupt.charger.repository;

import com.bupt.charger.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    boolean existsByCarId(String carId);
    Car findByCarId(String carId);

    List<Car> findAllByPileId(String pileId);
}
