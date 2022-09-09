package com.example.application.data.service;

import com.example.application.data.entity.AppUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<AppUser> get(Integer id) {
        return repository.findById(id);
    }

    public AppUser update(AppUser entity) {
        return repository.save(entity);
    }

    public void delete(Integer id) {
        repository.deleteById(id);
    }

    public Page<AppUser> list(Pageable pageable) {
        return repository.findAll(pageable);
    }
    
    public List<AppUser> listAll(Sort sort) {
        return repository.findAll(sort);
    }

    public int count() {
        return (int) repository.count();
    }
    
    
    public int getLastId() {
        return (int) repository.findLastId();
    }

	public void changeUserStatus(AppUser currentAppUser, boolean newStatus) {
		AppUser appUserToUpdate = repository.findById(currentAppUser.getId()).orElseGet(null);
		appUserToUpdate.setEnabled(newStatus);
		repository.save(appUserToUpdate);
	}


}
