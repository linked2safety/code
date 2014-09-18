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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;


/**
 * The persistent class for the result database table.
 * 
 */
@Entity
public class Result implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Basic(optional = false)
	@Column(name = "ID")
	@SequenceGenerator( name = "mySeq_results", sequenceName = "MY_SEQ", allocationSize = 1, initialValue = 1 )
	@GeneratedValue(strategy=GenerationType.IDENTITY, generator="mySeq_results")
	private int id;

	private int frequency;

	private boolean isLike;

	//bi-directional many-to-one association to Dataset
    @ManyToOne
	@JoinColumn(name="dataset_key")
	private Dataset dataset;

	//bi-directional many-to-one association to Variable
    @ManyToOne
	@JoinColumn(name="var_key")
	private Variable variable;

	//bi-directional many-to-one association to Triple
	@OneToMany(mappedBy="result")
	private Set<Triple> triples;

    public Result() {
    }

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getFrequency() {
		return this.frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public boolean getIsLike() {
		return this.isLike;
	}

	public void setIsLike(boolean isLike) {
		this.isLike = isLike;
	}

	public Dataset getDataset() {
		return this.dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}
	
	public Variable getVariable() {
		return this.variable;
	}

	public void setVariable(Variable variable) {
		this.variable = variable;
	}
	
	public Set<Triple> getTriples() {
		return this.triples;
	}

	public void setTriples(Set<Triple> triples) {
		this.triples = triples;
	}
	
}