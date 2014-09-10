/*
 * Copyright (C) 2008-2012, fluid Operations AG
 *
 * FedX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fluidops.fedx;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.varia.NullAppender;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;

import com.fluidops.fedx.exception.FedXException;
import com.fluidops.fedx.exception.FedXRuntimeException;
import com.fluidops.fedx.structures.Endpoint;
import com.fluidops.fedx.util.EndpointFactory;
import com.fluidops.fedx.util.QueryStringUtil;
import com.fluidops.fedx.util.Version;


/**
 * The command line interface for federated query processing with FedX.
 * 
 * <code>
 * Usage:
 * > FedX [Configuration] [Federation Setup] [Output] [Queries]
 * > FedX -{help|version}
 * 
 * WHERE
 * [Configuration] (optional)
 * Optionally specify the configuration to be used
 *      -c path/to/fedxConfig
 *      -verbose {0|1|2|3}
 *      -logtofile
 *      -p path/to/prefixConfig
 *      
 * [Federation Setup] (optional)
 * Specify one or more federation members
 *      -s urlToSparqlEndpoint
 *      -l path/to/NativeStore
 *      -d path/to/dataconfig.ttl
 *      
 * [Output] (optional)
 * Specify the output options, default stdout. Files are created per query to results/%outputFolder%/q_%id%.{json|xml},
 * where the outputFolder is the current timestamp, if not specified otherwise.
 *      -f {STDOUT,JSON,XML}
 *      -folder outputFolder
 *      
 * [Queries]
 * Specify one or more queries, in file: separation of queries by empty line
 *      -q sparqlquery
 *      @q path/to/queryfile
 *      
 * Notes:
 * The federation members can be specified explicitly (-s,-l,-d) or implicitly as 'dataConfig' 
 * via the fedx configuration  (-f)
 * 
 * If no PREFIX declarations are specified in the configurations, the CLI provides
 * some common PREFIXES, currently rdf, rdfs and foaf. 
 * </code>
 * 
 * 
 * @author Andreas Schwarte
 *
 */
public class CLI {

	/**
	 * 
	 * @author
	 *
	 */
	protected enum OutputFormat { 
		STDOUT, JSON, XML; 
	}
	
	protected static Logger logger = Logger.getLogger(CLI.class);
	protected String fedxConfig=null;
	protected int verboseLevel=0;
	protected boolean logtofile = false;
	protected String prefixDeclarations = null;
	protected List<Endpoint> endpoints = new ArrayList<Endpoint>();
	protected OutputFormat outputFormat = OutputFormat.STDOUT;
	protected List<String> queries = new ArrayList<String>();
	protected Repository repo = null;
	protected String outFolder = null;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new CLI().run(args);
		} catch (Exception e) {
			logger.log(Level.ERROR, "an exception was thrown", e);
			System.exit(1);
		}
	}

	
	
	/**
	 * 
	 * @param args
	 */
	public void run(String[] args) {
		configureRootLogger();
		
		logger.info("FedX Cli " + Version.getLongVersion());
		
		// parse the arguments and construct config
		parse(args);
		
		// activate logging to stdout if verbose is set
		configureLogging();
						
		
		if (Config.getConfig().getDataConfig()!=null) {
			// currently there is no duplicate detection, so the following is a hint for the user
			// can cause problems if members are explicitly specified (-s,-l,-d) and via the fedx configuration
			if (endpoints.size()>0) {
				logger.warn("WARN: Mixture of implicitely and explicitely specified federation members, dataConfig used: " + Config.getConfig().getDataConfig());
			}
			try {
				List<Endpoint> additionalEndpoints = EndpointFactory.loadFederationMembers(new File(Config.getConfig().getDataConfig()));
				endpoints.addAll(additionalEndpoints);
			} catch (FedXException e) {
				logger.log(Level.ERROR, "Failed to load implicitly specified data sources from fedx configuration. Data config is: " + Config.getConfig().getDataConfig(), e);
				error("Failed to load implicitly specified data sources from fedx configuration. Data config is: " + Config.getConfig().getDataConfig() + ". Details: " + e.getMessage(), false);
			}
		}
		
		// generic checks
		if (endpoints.size()==0) {
			logger.log(Level.ERROR, "No federation members specified. At least one data source is required.");
			error("No federation members specified. At least one data source is required.", true);
		}
		
		if (queries.size()==0) {
			logger.log(Level.ERROR, "No queries specified");
			error("No queries specified", true);
		}
		
		// setup the federation
		try {
			repo = FedXFactory.initializeFederation(endpoints);
		} catch (FedXException e) {
			logger.log(Level.ERROR, "Problem occured while setting up the federation:", e);
			error("Problem occured while setting up the federation: " + e.getMessage(), false);
		}
		
		// initialize default prefix declarations (if the user did not specify anything)
		if (Config.getConfig().getPrefixDeclarations()==null) {
			initDefaultPrefixDeclarations();
		}
		
		int count=1;
		for (String queryString : queries) {
			System.out.println("Running Query " + count);
			try {
				runQuery(queryString, count);
			} catch (QueryEvaluationException e) {
				logger.log(Level.ERROR, "Query " + count + " could not be evaluated: \n", e);
				error("Query " + count + " could not be evaluated: \n" + e.getMessage(), false);
			}
			count++;
		}
		
		try {
			FederationManager.getInstance().shutDown();
		} catch (FedXException e) {
			logger.log(Level.WARN, "Federation could not be shut down:", e);
		}
		logger.info("Done.");
		System.exit(0);
 	}

	
	
	
	/**
	 * 
	 * @param _args
	 */
	protected void parse(String[] _args) {
		if (_args.length==0) {
			printUsage(true);
		}
		
		if (_args.length==1 && _args[0].equals("-help"))  {
			printUsage(true);		
		}
				
		List<String> args = new LinkedList<String>(Arrays.asList(_args));
		
		parseConfiguaration(args, false);
		
		// initialize config
		try {
			// fedxConfig may be null (default values)
			Config.initialize(fedxConfig);
			if (prefixDeclarations!=null) {
				// override in config
				Config.getConfig().set("prefixDeclarations", prefixDeclarations);
			}
		} catch (FedXException e) {
			logger.log(Level.ERROR, "Problem occured while setting up the federation: ", e);
			error("Problem occured while setting up the federation: " + e.getMessage(), false);
		}		
		
		parseEndpoints(args, false);
		parseOutput(args);
		parseQueries(args);
		
		if (outFolder==null) {
			outFolder = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date());
		}

	}
	
	/**
	 * Parse the FedX Configuration,
	 *  1) -c path/to/fedxconfig.prop
	 *  2) -verbose [%lvl$]
	 *  3) -logtofile
	 *  4) -p path/to/prefixDeclaration.prop	 
	 *  
	 *  WHERE lvl is 0=off (default), 1=INFO, 2=DEBUG, 3=TRACE
	 *  
	 * @param args
	 */
	protected void parseConfiguaration(List<String> args, boolean printError) {
		String arg = args.get(0);
		
		// fedx config
		if (arg.equals("-c")) {
			// remove -c
			readArg(args);
			// remove path
			fedxConfig = readArg(args, "path/to/fedxConfig.ttl");
		}
		
		// verbose
		else if (arg.equals("-verbose")) {
			verboseLevel = 1;
			readArg(args);
			try {
				verboseLevel = Integer.parseInt(args.get(0));
				readArg(args);
			} catch (Exception numberFormat) {
				logger.log(Level.ERROR, "an exception was thrown", numberFormat);
			}
		}
		
		// logtofile
		else if (arg.equals("-logtofile")) {
			readArg(args);
			logtofile = true;
		}
		
		// prefixConfiguration
		else if (arg.equals("-p")) {
			readArg(args);
			prefixDeclarations = readArg(args, "path/to/prefixDeclarations.prop");
		} else {
			if (printError) {
				logger.log(Level.ERROR, "Unxpected Configuration Option: " + arg);
				error("Unxpected Configuration Option: " + arg, false);
			} else {
				return;
			}
		}
		
		parseConfiguaration(args, false);
	}
	
	
	/**
	 * Parse the endpoints, i.e. federation members
	 *  1) SPARQL Endpoint: -s url
	 *  2) Local NativeStore: -l path/to/NativeStore
	 *  3) Dataconfig: -d path/to/dataconfig.ttl
	 *  
	 * @param args
	 */
	protected void parseEndpoints(List<String> args, boolean printError) {
		String arg = args.get(0);
		
		if (arg.equals("-s")) {
			// remove -s
			readArg(args);
			String url = readArg(args,"urlToSparqlEndpoint");
			try {
				Endpoint endpoint = EndpointFactory.loadSPARQLEndpoint(url);
				endpoints.add(endpoint);
			} catch (FedXException e) {
				logger.log(Level.ERROR, "SPARQL endpoint " + url + " could not be loaded: ", e);
				error("SPARQL endpoint " + url + " could not be loaded: " + e.getMessage(), false);
			}			
		} else if (arg.equals("-l")) {
			// remove -l
			readArg(args);
			String path = readArg(args,"path/to/NativeStore");
			try {
				Endpoint endpoint = EndpointFactory.loadNativeEndpoint(path);
				endpoints.add(endpoint);
			} catch (FedXException e) {
				logger.log(Level.ERROR, "NativeStore " + path + " could not be loaded: ", e);
				error("NativeStore " + path + " could not be loaded: " + e.getMessage(), false);
			}
		} else if (arg.equals("-d")) {
			// remove -d
			readArg(args);
			String dataConfig = readArg(args,"path/to/dataconfig.ttl");
			try {
				List<Endpoint> ep = EndpointFactory.loadFederationMembers(new File(dataConfig));
				endpoints.addAll(ep);
			} catch (FedXException e) {
				logger.log(Level.ERROR, "Data config '" + dataConfig + "' could not be loaded: ", e);
				error("Data config '" + dataConfig + "' could not be loaded: " + e.getMessage(), false);
			}
		} else {			
			if (printError) {
				logger.log(Level.ERROR, "Expected at least one federation member (-s, -l, -d), was: " + arg);
				error("Expected at least one federation member (-s, -l, -d), was: " + arg, false);
			} else {
				return;
			}
		}
		
		parseEndpoints(args, false);
	}
	
	
	/**
	 * Parse output options
	 *  1) Format: -f {STDOUT,XML,JSON}
	 *  2) OutputFolder: -folder outputFolder 
	 * 
	 * @param args
	 */
	protected void parseOutput(List<String> args) {
		
		String arg = args.get(0);
		
		if (arg.equals("-f")) {
			// remove -s
			readArg(args);
			
			String format = readArg(args, "output format {STDOUT, XML, JSON}");
			
			if (format.equals("STDOUT")) {
				outputFormat = OutputFormat.STDOUT;
			}
			else if (format.equals("JSON")) {
				outputFormat = OutputFormat.JSON;
			}
			else if (format.equals("XML")) {
				outputFormat = OutputFormat.XML;
			} else {
				logger.log(Level.ERROR, "Unexpected output format: " + format + ". Available options: STDOUT,XML,JSON");
				error("Unexpected output format: " + format + ". Available options: STDOUT,XML,JSON", false);
			}
		} else if (arg.equals("-folder")) {
			// remove -folder
			readArg(args);
			
			outFolder = readArg(args, "outputFolder");
		} else {
			return;
		}
		
		parseOutput(args);
	}
	
	
	/**
	 * Parse query input
	 *  1) Querystring: -q SparqlQuery
	 *  2) File: @q path/to/QueryFile
	 *  
	 * @param args
	 */
	protected void parseQueries(List<String> args) {
		String arg = args.get(0);
		
		if (arg.equals("-q")) {
			// remove -q
			readArg(args);
			String query = readArg(args, "SparqlQuery");
			queries.add(query);
		} else if (arg.equals("@q")) {
			// remove @q
			readArg(args);
			String queryFile = readArg(args, "path/to/queryFile");
			try {	
				List<String> q = QueryStringUtil.loadQueries(queryFile);
				queries.addAll(q);
			} catch (IOException e) {
				logger.log(Level.ERROR, "Error loading query file '" + queryFile + "': ", e);
				error("Error loading query file '" + queryFile + "': " + e.getMessage(), false);
			}
		} else {
			logger.log(Level.ERROR, "Unexpected query argument: " + arg);
			error("Unexpected query argument: " + arg, false);
		}
		
		if (args.size()>0) {
			parseQueries(args);
		}
	}
	
	/**
	 * 
	 * @param args
	 * @param expected
	 * @return
	 */
	protected String readArg(List<String> args, String... expected) {
		if (args.size()==0) {
			logger.log(Level.ERROR, "Unexpected end of args, expected: " + expected);
			error("Unexpected end of args, expected: " + expected, false);
		}
		return args.remove(0);
	}
	
	/**
	 * initializes default prefix declarations from com.fluidops.fedx.commonPrefixesCli.prop
	 */
	protected void initDefaultPrefixDeclarations() {
		
		QueryManager qm = FederationManager.getInstance().getQueryManager();
		Properties props = new Properties();
		try	{
			props.load(CLI.class.getResourceAsStream("/com/fluidops/fedx/commonPrefixesCli.prop"));
		} catch (IOException e)	{
			logger.log(Level.ERROR, "Error loading prefix properties: ", e);
			throw new FedXRuntimeException("Error loading prefix properties: " + e.getMessage());
		}
		
		for (String ns : props.stringPropertyNames()) {
			// register namespace/prefix pair
			qm.addPrefixDeclaration(ns, props.getProperty(ns));
		}
	}
	
	/**
	 * 
	 * @param queryString
	 * @param queryId
	 * @throws QueryEvaluationException
	 */
	protected void runQuery(String queryString, int queryId) throws QueryEvaluationException {
			
		TupleQuery query;
		try {
			query = QueryManager.prepareTupleQuery(queryString);
		} catch (MalformedQueryException e) {
			logger.log(Level.ERROR, "Query is malformed: ", e);
			throw new QueryEvaluationException("Query is malformed: " + e.getMessage());
		} 
		int count = 0;
		long start = System.currentTimeMillis();
		
		TupleQueryResult res = query.evaluate();
				
		if (outputFormat == OutputFormat.STDOUT) {
			while (res.hasNext()) {
				System.out.println(res.next());
				count++;
			}
		} else if (outputFormat == OutputFormat.JSON) {
			
			File out = new File("results/"+ outFolder + "/q_" + queryId + ".json");
			out.getParentFile().mkdirs();
			
			logger.info("Results are being written to " + out.getPath());
			
			try {
				SPARQLResultsJSONWriter w = new SPARQLResultsJSONWriter(new FileOutputStream(out));
				w.startQueryResult(res.getBindingNames());
				
				while (res.hasNext()) {
					w.handleSolution(res.next());
					count++;
				}
				
				w.endQueryResult();
			} catch (IOException e) {
				logger.log(Level.ERROR, "IO Error while writing results of query " + queryId + " to JSON file: ", e);
				error("IO Error while writing results of query " + queryId + " to JSON file: " + e.getMessage(), false);
			} catch (TupleQueryResultHandlerException e) {
				logger.log(Level.ERROR, "Tuple result error while writing results of query " + queryId + " to JSON file: ", e);
				error("Tuple result error while writing results of query " + queryId + " to JSON file: " + e.getMessage(), false);
			}
		} else if (outputFormat == OutputFormat.XML) {
			
			File out = new File("results/" + outFolder + "/q_" + queryId + ".xml");
			out.getParentFile().mkdirs();
			
			System.out.println("Results are being written to " + out.getPath());
			
			try {
				SPARQLResultsXMLWriter w = new SPARQLResultsXMLWriter(new FileOutputStream(out));
				w.startQueryResult(res.getBindingNames());
				
				while (res.hasNext()) {
					w.handleSolution(res.next());
					count++;
				}
				
				w.endQueryResult();
				
			} catch (IOException e) {
				logger.log(Level.ERROR, "IO Error while writing results of query " + queryId + " to XML file: ", e);
				error("IO Error while writing results of query " + queryId + " to XML file: " + e.getMessage(), false);
			} catch (TupleQueryResultHandlerException e) {
				logger.log(Level.ERROR, "Tuple result error while writing results of query " + queryId + " to JSON file: ", e);
				error("Tuple result error while writing results of query " + queryId + " to JSON file: " + e.getMessage(), false);
			}
		}
		
		long duration = System.currentTimeMillis() - start;		// the duration in ms
		
		logger.info("Done query " + queryId + ": duration=" + duration + "ms, results=" + count);
	}
	
	
	
	/**
	 * Print an error and exit
	 * 
	 * @param errorMsg
	 */
	protected void error(String errorMsg, boolean printHelp) {
		if (printHelp) {
			logger.info("");
			printUsage();
		}
		System.exit(1);
	}
	
	
	/**
	 * Print the documentation
	 */
	protected void printUsage(boolean... exit) {
		
		logger.info("Usage:");
		logger.info("> FedX [Configuration] [Federation Setup] [Output] [Queries]");
		logger.info("> FedX -{help|version}");
		logger.info("");
		logger.info("WHERE");
		logger.info("[Configuration] (optional)");
		logger.info("Optionally specify the configuration to be used");
		logger.info("\t-c path/to/fedxConfig");
		logger.info("\t-verbose {0|1|2|3}");
		logger.info("\t-logtofile");
		logger.info("\t-p path/to/prefixDeclarations");
		logger.info("");
		logger.info("[Federation Setup] (optional)");
		logger.info("Specify one or more federation members");
		logger.info("\t-s urlToSparqlEndpoint");
		logger.info("\t-l path/to/NativeStore");
		logger.info("\t-d path/to/dataconfig.ttl");
		logger.info("");
		logger.info("[Output] (optional)");
		logger.info("Specify the output options, default stdout. Files are created per query to results/%outputFolder%/q_%id%.{json|xml}, where the outputFolder is the current timestamp, if not specified otherwise.");
		logger.info("\t-f {STDOUT,JSON,XML}");
		logger.info("\t-folder outputFolder");
		logger.info("");
		logger.info("[Queries]");
		logger.info("Specify one or more queries, in file: separation of queries by empty line");
		logger.info("\t-q sparqlquery");
		logger.info("\t@q path/to/queryfile");
		logger.info("");
		logger.info("Examples:");
		logger.info("Please have a look at the examples attached to this package.");
		logger.info("");
		logger.info("Notes:");
		logger.info("The federation members can be specified explicitely (-s,-l,-d) or implicitely as 'dataConfig' via the fedx configuration (-f)");
		logger.info("If no PREFIX declarations are specified in the configurations, the CLI provides some common PREFIXES, currently rdf, rdfs and foaf. ");
		
		if (exit.length!=0 && exit[0]) {
			System.exit(0);
		}
	}
	
	
	/**
	 * Activate logging if -verbose is enabled.
	 * 
	 * Verbose level: 0=off (default), 1=INFO, 2=DEBUG, 3=ALL
	 */
	protected void configureLogging() {
		
		//Logger rootLogger = Logger.getRootLogger();
		Logger l = Logger.getLogger("com.fluidops.fedx");
		Logger rootLogger = Logger.getRootLogger();
		if (verboseLevel>0) {
			//Logger pkgLogger = rootLogger.getLoggerRepository().getLogger("com.fluidops.fedx"); 
			if (verboseLevel==1) {
				rootLogger.setLevel(Level.INFO);
				l.setLevel(Level.INFO);
			} else if (verboseLevel==1) {
				rootLogger.setLevel(Level.DEBUG);
				l.setLevel(Level.DEBUG);
			} else if (verboseLevel>2) {
				rootLogger.setLevel(Level.ALL);
				l.setLevel(Level.ALL);
			}
				
			if (logtofile) {
				try {
					l.addAppender( new FileAppender(new PatternLayout("%5p %d{yyyy-MM-dd hh:mm:ss} [%t] (%F:%L) - %m%n"), "logs/fedx_cli.log"));
				} catch (IOException e) {
					logger.log(Level.WARN, "File Logging could not be initialized: ", e);
				}				
			} else {
				l.addAppender(new ConsoleAppender(new PatternLayout("%5p [%t] (%F:%L) - %m%n")));
			}
		}		
	}
	
	/**
	 * 
	 */
	protected void configureRootLogger() {
		
		Logger rootLogger = Logger.getRootLogger();
		if (!rootLogger.getAllAppenders().hasMoreElements()) {
			rootLogger.setLevel(Level.ALL); 
			rootLogger.addAppender(new NullAppender() );      
		}
	}
}
