package com.example.application.data.service.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.example.application.data.service.UserRepositoryCustom;

public class UserRepositoryCustomImpl implements UserRepositoryCustom {
	
	@PersistenceContext
    private EntityManager em;

	@Override
	public int getLastEntry() {
		Query query = em.createNativeQuery("SELECT max(id) FROM public.application_user");
		query.getFirstResult();
		return 0;
	}
	
	

}
