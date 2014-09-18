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

import com.deri.l2s.entities.Result;
import com.deri.l2s.entities.Variable;
import com.deri.l2s.managers.EMProvider;

public class VariablesDAO {
	
	private EntityManager em;
	
	public VariablesDAO(){
		em = EMProvider.getEMProvider().getEm();
	}
	
	public List<Variable> getAllVariables(){
		return em.createQuery("SELECT v FROM Variable v").getResultList();
	}
	
	public List<Variable> getSomeVariables(){
		return em.createQuery("SELECT v FROM Variable v WHERE v.id >= 0 and v.id <= 51").getResultList();
	}
	
	public List<Variable> getVariableById(int id){
		return em.createQuery("SELECT v FROM Variable v WHERE v.id = " + id).getResultList();
	}
	
	public void insert(Variable v){
		em.getTransaction().begin();
		em.persist(v);
		em.getTransaction().commit();
	}
	
	public void checkAndInsert(Variable v){
		if (!existsForName(v.getVariableName())){
			insert(v);
		}
	}
	
	public boolean existsForName(String name){
		Query query = em.createQuery("SELECT v FROM Variable v where v.variableName = :variableName");
		query.setParameter("variableName", name);
		if (query.getResultList().size() > 0){
			return true;
		} else {
			return false;
		}
	}

}
