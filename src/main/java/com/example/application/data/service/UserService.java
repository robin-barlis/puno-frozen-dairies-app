package com.example.application.data.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.AppUser;

@Service
public class UserService {

	@Autowired
    private final UserRepository repository;

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public Optional<AppUser> get(Integer id) {
        return repository.findById(id);
    }
    
    public AppUser findByEmailAddress(String emailAddress) {
        return repository.findByEmailAddress(emailAddress);
    }
    
    public AppUser findByUserName(String userName) {
        return repository.findByUsername(userName);
    }

    public AppUser update(AppUser user) {
    	if (user.getId() == null) {
            
            return repository.save(user);
        } else {
        	return repository.save(user);
        }
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

	public AppUser changeUserStatus(AppUser currentAppUser, boolean newStatus) {
		AppUser appUserToUpdate = repository.findById(currentAppUser.getId()).orElseGet(null);
		appUserToUpdate.setEnabled(newStatus);
		return repository.save(appUserToUpdate);
	}

}
