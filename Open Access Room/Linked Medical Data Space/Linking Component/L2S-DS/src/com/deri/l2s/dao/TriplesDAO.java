package com.deri.l2s.dao;

/*
 *  Linked2Safety SPARQL Endpoint Discovery and Linking Framework
 *  
 *  Copyright (C) 2014 Muntazir Mehdi <muntazir.mehdi@insight-centre.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.deri.l2s.entities.Triple;
import com.deri.l2s.entities.Variable;
import com.deri.l2s.managers.EMProvider;

public class TriplesDAO {
	
	private EntityManager em;
	
	public TriplesDAO(){
		em = EMProvider.getEMProvider().getEm();
	}
	
	public void insert(Triple t){
		em.getTransaction().begin();
		em.persist(t);
		em.getTransaction().commit();
//		em.clear();
	}
	
	public void insertAll(List<Triple> triples){
		em.getTransaction().begin();
		for (Triple triple : triples) {
			em.persist(triple);
		}
		em.getTransaction().commit();
	}
	
	public void checkAndInsert(Triple t){
		if (!existsForSubject(t.getSubject())){
			insert(t);
		}
	}
	
	public boolean existsForSubject(String subject){
		Query query = em.createQuery("SELECT t FROM Triple t where t.subject = :subject");
		query.setParameter("subject", subject);
		if (query.getResultList().size() > 0){
			return true;
		} else {
			return false;
		}
	}

}
