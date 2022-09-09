package com.example.application.data.service;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.application.data.entity.PfdiRoles;

public interface RolesRepository extends JpaRepository<PfdiRoles, Integer> {

}
