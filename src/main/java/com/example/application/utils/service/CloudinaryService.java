package com.example.application.utils.service;

import java.io.File;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Singleton;
import com.cloudinary.utils.ObjectUtils;


@Service
public class CloudinaryService {
	
	private final Cloudinary cloudinary = Singleton.getCloudinary();

	@SuppressWarnings("rawtypes")
	public String uploadFile(File file) {
	    try {
	        Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.emptyMap());

	        return  uploadResult.get("url").toString();
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}


}
