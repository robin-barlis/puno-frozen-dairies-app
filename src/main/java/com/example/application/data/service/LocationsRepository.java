package com.example.application.data.service;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.data.entity.PfdiLocation;

public interface LocationsRepository extends JpaRepository<PfdiLocation, Integer> {

}
