package com.deri.l2s.entities;

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

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;


/**
 * The persistent class for the triple database table.
 * 
 */
@Entity
public class Triple implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Basic(optional = false)
	@Column(name = "ID")
	@SequenceGenerator( name = "mySeq_triple", sequenceName = "MY_SEQ", allocationSize = 1, initialValue = 1 )
	@GeneratedValue(strategy=GenerationType.IDENTITY, generator="mySeq_triple")
	private int id;

	private String object;

	private String predicate;

	private String subject;

	//bi-directional many-to-one association to LinkedInfo
	@OneToMany(mappedBy="triple", cascade = CascadeType.PERSIST)
	private Set<LinkedInfo> linkedInfos;

	//bi-directional many-to-one association to Result
    @ManyToOne
	private Result result;

    public Triple() {
    }

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getObject() {
		return this.object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	public String getPredicate() {
		return this.predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getSubject() {
		return this.subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Set<LinkedInfo> getLinkedInfos() {
		return this.linkedInfos;
	}

	public void setLinkedInfos(Set<LinkedInfo> linkedInfos) {
		this.linkedInfos = linkedInfos;
	}
	
	public Result getResult() {
		return this.result;
	}

	public void setResult(Result result) {
		this.result = result;
	}
	
}