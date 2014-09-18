package com.deri.l2s.support;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import org.apache.log4j.Logger;

public class ProjectPropertyHandler {
	
	private static Logger logger = Logger.getLogger(ProjectPropertyHandler.class);
	
	private static ProjectPropertyHandler instance = null;
	
//	private String borkerURL;
//	private String[] topics;
	private String[] endpoints;
//	private String runTime;
//	
//	//URL of the DBStore
//	private String dburl;
//	private String dbuser;
//	private String dbpass;
//	/*
//	 * Graph name where the values are to be stored. 
//	 * Note: It is different from namedGraph, namedGraph is used only for retrieving configuration Instances.
//	*/
//	private String graph;
	
	//Named Graph containing the configuration instances
//	private String namedGraph;
//	
//	private boolean doAll;
//	private boolean showMessages;
//	
//	private String sparqlEndPoint;
	
	public static ProjectPropertyHandler getInstance(){
		if (instance == null){
			instance = new ProjectPropertyHandler();
		}
		return instance;
	}
	
	public static ProjectPropertyHandler getInstance(ProjectPropertyHandler configurations){
		instance = getInstance();
//		instance.setBorkerURL(configurations.getBorkerURL());
//		instance.setDbpass(configurations.getDbpass());
//		instance.setDburl(configurations.getDburl());
//		instance.setDbuser(configurations.getDbuser());
//		instance.setDoAll(configurations.isDoAll());
//		instance.setGraph(configurations.getGraph());
//		instance.setNamedGraph(configurations.getNamedGraph());
//		instance.setRunTime(configurations.getRunTime());
//		instance.setShowMessages(configurations.isShowMessages());
//		instance.setTopics(configurations.getTopics());
		return instance;
	}

	protected ProjectPropertyHandler() {
		try {
			logger.info("Configurations Loaded!");
			PropertyResourceBundle bundle = new PropertyResourceBundle(this.getPropertiesFromClasspath("config.properties"));
//			this.setBorkerURL(bundle.getString("brokerurl"));
//			this.setTopics(bundle.getString("topics").split(","));
			String[] dataSetStrings = bundle.getString("datasets").split(",");
//			ArrayList<Dataset> ds = new ArrayList<Dataset>();
//			for (String datasetString : dataSetStrings) {
//				String[] subString = datasetString.split(";");
//				Dataset d = new Dataset();
//				d.setEndPoint(subString[0]);
//				d.setName(subString[1]);
//				ds.add(d);
//			}
//			this.setDatasets(ds);
//			this.setRunTime(bundle.getString("runtime"));
//			this.setDburl(bundle.getString("dburl"));
//			this.setDbuser(bundle.getString("dbuser"));
//			this.setDbpass(bundle.getString("dbpassword"));
//			this.setGraph(bundle.getString("graph"));
//			this.setNamedGraph(bundle.getString("configGraph"));
//			this.setSparqlEndPoint(bundle.getString("sparqlEndpoint"));
//			String doAllCheck = bundle.getString("doAll");
//			if (doAllCheck.equals("1")){
//				this.setDoAll(true);
//			} else {
//				this.setDoAll(false);
//			}
//			String showMessageCheck = bundle.getString("showmessage");
//			if (showMessageCheck.equals("1")){
//				this.setShowMessages(true);
//			} else {
//				this.setShowMessages(false);
//			}
//			if(this.getRunTime() == "0" || this.getRunTime().equals("0")){
//				this.setRunTime("");
//			}
		} catch (MissingResourceException e) {
			System.out.println(e.getMessage()) ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private InputStream getPropertiesFromClasspath(String propFileName) {
	    // loading xmlProfileGen.properties from the classpath
	    Properties props = new Properties();
	    InputStream inputStream = this.getClass().getClassLoader()
	        .getResourceAsStream(propFileName);

	    if (inputStream == null) {
	    	
	    }

	    return inputStream;
	}
	
//	public String getBorkerURL() {
//		return borkerURL;
//	}
//
//	public void setBorkerURL(String borkerURL) {
//		this.borkerURL = borkerURL;
//	}
//
//	public String[] getTopics() {
//		return topics;
//	}
//
//	public void setTopics(String[] topics) {
//		this.topics = topics;
//	}
	
//	public String getRunTime() {
//		return runTime;
//	}
//
//	public void setRunTime(String runTime) {
//		this.runTime = runTime;
//	}
//
//	public String getDburl() {
//		return dburl;
//	}
//
//	public void setDburl(String dburl) {
//		this.dburl = dburl;
//	}
//
//	public String getDbuser() {
//		return dbuser;
//	}
//
//	public void setDbuser(String dbuser) {
//		this.dbuser = dbuser;
//	}
//
//	public String getDbpass() {
//		return dbpass;
//	}
//
//	public void setDbpass(String dbpass) {
//		this.dbpass = dbpass;
//	}

//	public String getGraph() {
//		return graph;
//	}
//
//	public void setGraph(String graph) {
//		this.graph = graph;
//	}
//
//	public boolean isDoAll() {
//		return doAll;
//	}
//
//	public void setDoAll(boolean doAll) {
//		this.doAll = doAll;
//	}
//
//	public boolean isShowMessages() {
//		return showMessages;
//	}
//
//	public void setShowMessages(boolean showMessages) {
//		this.showMessages = showMessages;
//	}
//
//	public String getNamedGraph() {
//		return namedGraph;
//	}
//
//	public void setNamedGraph(String namedGraph) {
//		this.namedGraph = namedGraph;
//	}
//
//	public String getSparqlEndPoint() {
//		return sparqlEndPoint;
//	}
//
//	public void setSparqlEndPoint(String sparqlEndPoint) {
//		this.sparqlEndPoint = sparqlEndPoint;
//	}

	public String[] getEndpoints() {
		return endpoints;
	}

	public void setEndpoints(String[] endpoints) {
		this.endpoints = endpoints;
	}

//	public ArrayList<Dataset> getDatasets() {
//		return datasets;
//	}
//
//	public void setDatasets(ArrayList<Dataset> datasets) {
//		this.datasets = datasets;
//	}

}