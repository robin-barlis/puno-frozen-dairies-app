package com.example.application.data.service;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.data.entity.PfdiLocation;
import com.example.application.data.entity.PfdiPosition;

public interface PositionsRepository extends JpaRepository<PfdiPosition, Integer> {

}
