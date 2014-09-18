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

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.deri.l2s.entities.LinkedInfo;
import com.deri.l2s.entities.Triple;
import com.deri.l2s.managers.EMProvider;

public class LinkedInfoDAO {
	
	private EntityManager em;
	
	public LinkedInfoDAO(){
		em = EMProvider.getEMProvider().getEm();
	}
	
	public void insert(LinkedInfo li){
		em.getTransaction().begin();
		em.persist(li);
		em.getTransaction().commit();
	}
	
	public void checkAndInsert(LinkedInfo li){
		if (!exists(li)){
			insert(li);
		}
	}
	
	public boolean exists(LinkedInfo li){
		Query query = em.createQuery("SELECT l FROM LinkedInfo l where l.linkedsource = :linkedsource and l.triple = :triple");
		query.setParameter("linkedsource", li.getLinkedsource());
		query.setParameter("triple", li.getTriple());
		if (query.getResultList().size() > 0){
			return true;
		} else {
			return false;
		}
	}

}
