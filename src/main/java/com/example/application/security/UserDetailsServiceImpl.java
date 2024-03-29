package com.example.application.security;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.application.data.entity.AppUser;
import com.example.application.data.service.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	// Add the logic that checks if the user exists in the database
        List<AppUser> userList = userRepository.findAll();
        
        AppUser user = userList.stream().filter(appUser -> appUser.getUsername().equalsIgnoreCase(username)).findFirst(). orElse(null);
        if (user == null) {
            throw new UsernameNotFoundException("No user present with username: " + username);
        } else {
        	boolean isExpired = user.getEndDateOfAccess() != null && user.getEndDateOfAccess().isBefore(LocalDate.now());
        	boolean isNotYetActive = user.getStartDateOfAccess() != null && user.getStartDateOfAccess().isAfter(LocalDate.now());
        	UserDetails userDetails =  User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
           
            .authorities(getAuthorities(user))
            .accountExpired(isExpired || isNotYetActive)
            .disabled(!user.getEnabled())
            .accountLocked(user.getLocked())
            .build();
//            return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
//                    getAuthorities(user));
        	return userDetails;
        }
    }

    private static List<GrantedAuthority> getAuthorities(AppUser user) {
 
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

    }

}
