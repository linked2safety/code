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

import com.deri.l2s.entities.Dataset;
import com.deri.l2s.entities.Result;
import com.deri.l2s.entities.Triple;
import com.deri.l2s.entities.Variable;
import com.deri.l2s.managers.EMProvider;

public class ResultsDAO {
	
	private EntityManager em;
	
	public ResultsDAO(){
		em = EMProvider.getEMProvider().getEm();
	}
	
	public List<Result> getAllResults(){
		List<Result> results = em.createQuery("SELECT r FROM Result r").getResultList();
		return results;
	}
	
	public void insert(Result r){
		em.getTransaction().begin();
		em.persist(r);
		em.getTransaction().commit();
//		em.clear();
	}
	
	public void insertAll(List<Result> results){
		em.getTransaction().begin();
		for (Result result : results) {
			em.persist(result);
		}
		em.getTransaction().commit();
	}
	
	public void checkAndInsert(Result r){
		if (!existsForDatasetVariableAndFrequency(r)){
			insert(r);
		}
	}
	
	public boolean existsForDatasetVariableAndFrequency(Result r){
		Query query = em.createQuery("SELECT r FROM Result r where r.variable = :variable and r.dataset = :dataset and r.frequency=:frequency");
		query.setParameter("dataset", r.getDataset());
		query.setParameter("variable", r.getVariable());
		query.setParameter("frequency", r.getFrequency());
		if (query.getResultList().size() > 0){
			return true;
		} else {
			return false;
		}
	}
	
	public List<Variable> getUniqueVariableFromResults(){
		Query query = em.createQuery("SELECT DISTINCT r.variable FROM Result r");
		List<Variable> vars = query.getResultList();
		return vars;
	}
}
