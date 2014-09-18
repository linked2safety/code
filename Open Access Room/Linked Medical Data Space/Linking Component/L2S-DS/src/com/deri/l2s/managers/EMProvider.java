package com.deri.l2s.managers;

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
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EMProvider {
	
	private static EMProvider emprovider;
	private EntityManager em;
	
	protected EMProvider(){
		if (emprovider == null){
			EntityManagerFactory factory = Persistence.createEntityManagerFactory("L2S-DS");
			em = factory.createEntityManager();
			this.setEm(em);
		}
	}
	
	public static EMProvider getEMProvider(){
		if (emprovider == null){
			emprovider = new EMProvider();
		}
		return emprovider;
	}

	public EntityManager getEm() {
		return em;
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

}
