/*
 * Copyright (C) 2014 CERTH - zeginis@iti.gr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This component bundles PostgreSQL JDBC driver, which is available under 
 * BSD license. You may download the source code and associated license at 
 * http://jdbc.postgresql.org/
 * 
 * This component bundles Apache Jena library, which is available under 
 * Apache v2 licence . You may download the source code and associated license at 
 * https://jena.apache.org/
 *
 * This component bundles Java SE, which is available under BCL license. 
 * You may download the source code and associated license at  http://www.oracle.com/
 * The java BCL license clarifies that:
 *   "Use of the Commercial Features for any commercial or production purpose 
 *    requires a separate license from Oracle. “Commercial Features” means those 
 *    features identified Table 1-1 (Commercial Features In Java SE Product Editions)
 *    of the Java SE documentation accessible at
 *    http://www.oracle.com/technetwork/java/javase/documentation/index.html"  
 *
 *
 *
 *  The source files are available at:  https://github.com/linked2safety/code
 */

package eu.l2s_script;

/**
 *
 * @author Dimitris Zeginis
 */
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSetFormatter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class repeatWorkflows {

    private static String dbURL;
    private static String dbUsername;
    private static String dbPassword;
    private static String SPARQLendpoint;
    private static String galaxyToolsDir;
    private static String datasetDir;
    private static String outputDir;
    private static String clean;

    private static Map<String, l2stool> l2sTools = new HashMap<String, l2stool>() {

        {
            put("Missing Data Test", new l2stool("qualitycontrol", "QualityControl/runMissingData.sh"));
            put("Allele Frequency Test", new l2stool("qualitycontrol", "QualityControl/runAlleleFreq.sh"));
            put("Hardy Weinberg Equilibrium Test", new l2stool("qualitycontrol", "QualityControl/runHWE.sh"));
            put("Fisher Exact Test", new l2stool("statisticalalgorithm", "SingleHypothesisTesting/runFisherExact.sh"));
            put("Chi Square Test", new l2stool("statisticalalgorithm", "SingleHypothesisTesting/runChiSquare.sh"));
            put("Linkage Disequilibrium", new l2stool("statisticalalgorithm", "SingleHypothesisTesting/runLD.sh"));
            put("Binomial Logistic Regression", new l2stool("statisticalalgorithm", "SingleHypothesisTesting/runBinomialLR.sh"));
            put("Odds Ratio", new l2stool("statisticalalgorithm", "SingleHypothesisTesting/runOR.sh"));
            put("C4.5 Decision Trees Algorithm", new l2stool("dataminingalgorithm", "DataMining/runDecisionTrees.sh"));
            put("Apriori Association Rules Algorithm", new l2stool("dataminingalgorithm", "DataMining/runAssociationRules.sh"));
            put("Random Forest Algorithm", new l2stool("dataminingalgorithm", "DataMining/runRandomForest.sh"));
            put("Rough Set Dimensionality Reduction", new l2stool("dimensionalityreductionalgorithm", "DimensionalityReduction/runReduceDataRSFS.sh"));
            put("Information Gain Dimensionality Reduction", new l2stool("dimensionalityreductionalgorithm", "DimensionalityReduction/runReduceDataIGFS.sh"));
            put("Chi Squared Dimensionality Reduction", new l2stool("dimensionalityreductionalgorithm", "DimensionalityReduction/runReduceDataCSFS.sh"));
            put("Set Target", new l2stool("settargetvariable", "SetTarget/runSetTarget.sh"));
            put("Non Applicable Data Test", new l2stool("qualitycontrol", "QualityControl/runNonApplicableData.sh"));
        }

    };

    public static void main(String[] argv) {

        try {

            Properties prop = new Properties();

            //load a properties file
            prop.load(new FileInputStream("config.properties"));

            //get the property values
            dbURL = prop.getProperty("dbURL");
            dbUsername = prop.getProperty("dbUsername");
            dbPassword = prop.getProperty("dbPassword");
            SPARQLendpoint = prop.getProperty("SPARQLendpoint");
            galaxyToolsDir = prop.getProperty("galaxyToolsDir");
            datasetDir = prop.getProperty("datasetDir");
            outputDir = prop.getProperty("outputDir");
            clean = prop.getProperty("clean");

            File theDatasetDir = new File(datasetDir);

            // if the directory does not exist, create it
            if (!theDatasetDir.exists()) {
                theDatasetDir.mkdir();
            }

            File theoutputDir = new File(outputDir);

            // if the directory does not exist, create it
            if (!theoutputDir.exists()) {
                theoutputDir.mkdir();
            }

            //load postgres driver
            Class.forName(
                    "org.postgresql.Driver");
            //connect to DB
            Connection connection = DriverManager.getConnection(
                    "jdbc:postgresql://" + dbURL, dbUsername, dbPassword);

            Statement st_workflow = connection.createStatement();
            //Get workflows and datasetids
            ResultSet rs_workflow = st_workflow.executeQuery("SELECT workflow, datasetid, workflowid, userid, periodicbp_id"
                    + " FROM workflowused");

            while (rs_workflow.next()) {
                //read workflow string
                String workflow_str = rs_workflow.getString(1);
                //read datasetid string
                String datasetid = rs_workflow.getString(2);
                //read workflowid string
                String workflowid = rs_workflow.getString(3);
                //read user ID (user that executec the workflow)
                String userid = rs_workflow.getString(4);
                //read periodicbp_id
                String periodicbp_id = rs_workflow.getString(5);

                //Get user mail
                String usermail = getUserMail(connection, userid);

                //Get workflow steps
                String[] workflowsteps = workflow_str.split(",");
                String input = datasetDir + "dataset_" + datasetid + ".csv";
                System.out.println("START WORKFLOW");
                for (String step : workflowsteps) {
                    boolean error = false;
                    if (step.equals("input_data")) {
                        error = writeDataset2File(datasetid, connection, usermail);
                    } else {
                        String algorithmParamSetting = getAlgorithmParamSetting(connection,
                                l2sTools.get(step).getSQLtable(), step, workflowid);

                        input = runTool(step, " " + input, algorithmParamSetting, usermail, workflowid);

                    }

                    if (error) {
                        break;
                    }
                }
                System.out.println("END WORKFLOW\n\n");

                if (clean.equals("true")) {
                    Statement del_association = connection.createStatement();
                    del_association.executeUpdate("DELETE FROM association WHERE workflowid=" + workflowid);
                    del_association.close();

                    Statement del_associationrule = connection.createStatement();
                    del_associationrule.executeUpdate("DELETE FROM associationrule WHERE workflowid=" + workflowid);
                    del_associationrule.close();

                    Statement del_dataminingalgorithm = connection.createStatement();
                    del_dataminingalgorithm.executeUpdate("DELETE FROM dataminingalgorithm WHERE workflowid=" + workflowid);
                    del_dataminingalgorithm.close();

                    Statement del_dimensionalityreductionalgorithm = connection.createStatement();
                    del_dimensionalityreductionalgorithm.executeUpdate("DELETE FROM dimensionalityreductionalgorithm WHERE workflowid=" + workflowid);
                    del_dimensionalityreductionalgorithm.close();

                    Statement del_qualitycontrol = connection.createStatement();
                    del_qualitycontrol.executeUpdate("DELETE FROM qualitycontrol WHERE workflowid=" + workflowid);
                    del_qualitycontrol.close();

                    Statement del_statisticalalgorithm = connection.createStatement();
                    del_statisticalalgorithm.executeUpdate("DELETE FROM statisticalalgorithm WHERE workflowid=" + workflowid);
                    del_statisticalalgorithm.close();

                    Statement del_settargetvariable = connection.createStatement();
                    del_settargetvariable.executeUpdate("DELETE FROM settargetvariable WHERE workflowid=" + workflowid);
                    del_settargetvariable.close();

                    Statement del_workflowused = connection.createStatement();
                    del_workflowused.executeUpdate("DELETE FROM workflowused WHERE workflowid=" + workflowid);
                    del_workflowused.close();

                    //check if the dataset is used to more than one workflows, if its not then delete it
                    Statement st_count = connection.createStatement();
                    ResultSet rs_count = st_count.executeQuery("SELECT count(datasetid) FROM workflowused where datasetid=" + datasetid);
                    if (rs_count.next()) {
                        int count = Integer.parseInt(rs_count.getString(1));
                        if (count == 0) {
                            Statement del_datasetused = connection.createStatement();
                            del_datasetused.executeUpdate("DELETE FROM datasetused WHERE datasetid=" + datasetid);
                            del_datasetused.close();
                        }

                    }
                    rs_count.close();

                    Statement del_periodicbatchprocess = connection.createStatement();
                    del_periodicbatchprocess.executeUpdate("DELETE FROM periodicbatchprocess WHERE periodicbp_id=" + periodicbp_id);
                    del_periodicbatchprocess.close();
                }
            }

            rs_workflow.close();

            st_workflow.close();

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();

        } catch (ClassNotFoundException e) {
            System.out.println("Where is your PostgreSQL JDBC Driver? Include in your library path!");
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(repeatWorkflows.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String getParameters(String setting) {
        String parameters = "";
        String[] tokens = setting.split(" ");
        for (String token : tokens) {
            if (!token.contains("association_rule_type")) {
                parameters += token.substring(token.lastIndexOf("=") + 1) + " ";
            }
        }
        return parameters;
    }

    private static String runTool(String step, String input, String setting, String usermail, String workflowid) {
        String toolOutput = "";

        //String executableStr = "sh " + galaxyToolsDir + l2sTools.get(step).getExecutable() + input + " ";
        String executableStr = "sh " + galaxyToolsDir + l2sTools.get(step).getExecutable();
        if (step.equals("Missing Data Test")) {///1  DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += input + " " + getParameters(setting) + " " + toolOutput + " " + usermail;

        } else if (step.equals("Allele Frequency Test")) { ///1 DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += input + " " + getParameters(setting) + " " + toolOutput + " " + usermail;

        } else if (step.equals("Hardy Weinberg Equilibrium Test")) {///1 DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += input + " " + getParameters(setting) + " " + toolOutput + " " + usermail;

        } else if (step.equals("Fisher Exact Test")) {//2  DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += input + " " + toolOutput + " " + usermail;

        } else if (step.equals("Chi Square Test")) {//2 DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += input + " " + toolOutput + " " + getParameters(setting) + " " + usermail;

        } else if (step.equals("Linkage Disequilibrium")) {//1 DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += input + " " + getParameters(setting) + " " + toolOutput + " " + usermail;

        } else if (step.equals("Binomial Logistic Regression")) {//1 DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += input + " " + getParameters(setting) + " " + toolOutput + " " + usermail;

        } else if (step.equals("Odds Ratio")) {//1 DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += input + " " + getParameters(setting) + " " + toolOutput + " " + usermail;

        } else if (step.equals("Rough Set Dimensionality Reduction")) {//1 DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += " " + getParameters(setting) + " " + input + " " + toolOutput + " " + usermail;

        } else if (step.equals("C4.5 Decision Trees Algorithm")) { //DONE 
            //The same step name is used for cross validation and percentage split
            if (setting.contains("cross_validation")) {
                executableStr = executableStr.replaceAll("runDecisionTrees", "runDecisionTrees_cv");
            }

            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            String weka = galaxyToolsDir + "DataMining/weka-3-7-7.jar";
            String params = getParameters(setting);
            String removeMissingVal = params.substring(0, params.indexOf(" "));
            String restVals = params.substring(params.indexOf(" "));
            String pdffile = outputDir + step.replaceAll(" ", "_") + "_pdf_" + workflowid;
            executableStr += " " + removeMissingVal + " " + input + " " + restVals + " "
                    + weka + " " + toolOutput + " " + pdffile + " " + usermail;

        } else if (step.equals("Apriori Association Rules Algorithm")) {//1 DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            String weka = galaxyToolsDir + "DataMining/weka-3-7-7.jar";
            String params = getParameters(setting);
            String removeMissingVal = params.substring(0, params.indexOf(" "));
            String restVals = params.substring(params.indexOf(" "));
            executableStr += " " + removeMissingVal + " " + input + " " + restVals + " "
                    + weka + " " + toolOutput + " " + usermail;

        } else if (step.equals("Random Forest Algorithm")) { //DONE 
            if (setting.contains("cross_validation")) {
                executableStr = executableStr.replaceAll("runRandomForest", "runRandomForest_cv");
            }

            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            String weka = galaxyToolsDir + "DataMining/weka-3-7-7.jar";
            String params = getParameters(setting);
            String removeMissingVal = params.substring(0, params.indexOf(" "));
            String restVals = params.substring(params.indexOf(" "));
            String pdffile = outputDir + step.replaceAll(" ", "_") + "_pdf_" + workflowid;
            executableStr += " " + removeMissingVal + " " + input + " " + restVals + " "
                    + weka + " " + toolOutput + " " + pdffile + " " + usermail;

        } else if (step.equals("Information Gain Dimensionality Reduction")) { //3 DONE EINAI ETSI SEIRA PARAM?

            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            String weka = galaxyToolsDir + "DimensionalityReduction/weka-3-6-7.jar";
            String params = getParameters(setting);
            String removeMissingVal = params.substring(0, params.indexOf(" "));
            String restVals = params.substring(params.indexOf(" "));
            executableStr += " " + removeMissingVal + " " + input + " " + restVals + " "
                    + weka + " " + toolOutput + " " + usermail;

        } else if (step.equals("Chi Squared Dimensionality Reduction")) { //3 DONE EINAI ETSI SEIRA PARAM?

            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            String weka = galaxyToolsDir + "DimensionalityReduction/weka-3-6-7.jar";
            String params = getParameters(setting);
            String removeMissingVal = params.substring(0, params.indexOf(" "));
            String restVals = params.substring(params.indexOf(" "));
            executableStr += " " + removeMissingVal + " " + input + " " + restVals + " "
                    + weka + " " + toolOutput + " " + usermail;

        } else if (step.equals("Set Target")) { //3 DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += input + " " + toolOutput + " " + getParameters(setting) + " " + usermail;

        } else if (step.equals("Non Applicable Data Test")) { //3 DONE
            toolOutput = outputDir + step.replaceAll(" ", "_") + "_output_" + workflowid;
            executableStr += input + " " + getParameters(setting) + " " + toolOutput + " " + usermail;

        }
        System.out.println("Executing: " + executableStr);
        try {
            Process p1 = Runtime.getRuntime().exec(executableStr);
            p1.waitFor();
        } catch (IOException ex) {
            ex.printStackTrace();

        } catch (InterruptedException ex) {
            Logger.getLogger(repeatWorkflows.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return toolOutput;

    }

    private static String getAlgorithmParamSetting(Connection connection, String tableName, String algorithmName, String workflowid) {
        String algorithmParamSetting = "";

        try {
            Statement st_paramsetting = connection.createStatement();
            String sqlQuery = "";
            if (tableName.equals("settargetvariable")) {
                sqlQuery = "SELECT algorithmparamssetting "
                        + "FROM " + tableName
                        + " WHERE workflowid='" + workflowid + "'";

            } else {
                //Get workflows and datasetids
                sqlQuery = "SELECT algorithmparamssetting "
                        + "FROM " + tableName
                        + " WHERE algorithm_name='" + algorithmName + "' AND workflowid='" + workflowid + "'";
            }
            ResultSet rs_paramsetting = st_paramsetting.executeQuery(sqlQuery);

            if (rs_paramsetting.next()) {
                //read workflow string
                algorithmParamSetting = rs_paramsetting.getString(1);
            }

        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }

        return algorithmParamSetting;

    }

    private static String getUserMail(Connection connection, String userid) {
        String usermail = "";

        try {
            Statement st_paramsetting = connection.createStatement();
            //Get workflows and datasetids
            String sqlQuery = "SELECT email_address "
                    + "FROM users"
                    + " WHERE userid='" + userid + "'";
            ResultSet rs_paramsetting = st_paramsetting.executeQuery(sqlQuery);

            if (rs_paramsetting.next()) {
                //read workflow string
                usermail = rs_paramsetting.getString(1);
            }
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }
        return usermail;
    }

    private static boolean writeDataset2File(String datasetid, Connection connection, String usermail) {
        boolean error = false;
        try {

            Statement st_dataset = connection.createStatement();
            //get SPARQL query 
            ResultSet rs_dataset = st_dataset.executeQuery("SELECT sparql_query FROM datasetused"
                    + " where datasetid='" + datasetid + "'");

            if (rs_dataset.next()) {
                //THE SPARQL QUERY NEED TO BE URLENCODED AT THE DB
                String sparql_query = rs_dataset.getString(1);

                //call Pano's code to get the CSV
                URL url = new URL(SPARQLendpoint + sparql_query);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                String result = "";
                boolean first = true;
                while ((line = rd.readLine()) != null) {
                    if (first) {
                        result += line;
                        first = false;
                    } else {
                        result += "\n" + line;
                    }
                }
                rd.close();

                //write dataset to file
                PrintWriter writer = new PrintWriter(datasetDir + "dataset_" + datasetid + ".csv");
                writer.print(result);
                writer.close();
                
                //NEED TO PARAMETRIZE THE fakeXML.xml path
                String execString = "python " + galaxyToolsDir + "KnowledgeExtractionAndFilteringMechanism/workflowLogging.py "
                        + usermail + " " + "/ " + datasetDir + "dataset_" + datasetid + ".csv "
                        + galaxyToolsDir + "data_source/fakeXML.xml 0";
                Process p1 = Runtime.getRuntime().exec(execString);
                p1.waitFor();
                System.out.println(execString);
            }
        } catch (QueryParseException qpe) {
            System.out.println("Cannot parse SPARQL query");
            error = true;
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            error = true;
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            error = true;
        } catch (IOException e) {
            System.out.println("Cannot run workflowlogging");
            e.printStackTrace();
            error = true;
        } catch (InterruptedException ex) {
            Logger.getLogger(repeatWorkflows.class.getName()).log(Level.SEVERE, null, ex);
        }
        return error;
    }

    private static boolean writeDataset2File_old(String datasetid, Connection connection, String usermail) {
        boolean error = false;
        try {
            Statement st_dataset = connection.createStatement();
            //get SPARQL query 
            ResultSet rs_dataset = st_dataset.executeQuery("SELECT sparql_query FROM datasetused"
                    + " where datasetid='" + datasetid + "'");

            if (rs_dataset.next()) {
                String sparql_query = rs_dataset.getString(1);

                //NEED TO REMOVE THIS!!!!!!!!!!!!!               
                sparql_query = "select ?patienthasdiabetes  ?bmi ?hasdislipidemia ?Count from <http://www.linked2safety-project.eu/graph/0001> where"
                        + " { ?a <http://www.linked2safety-project.eu/properties/patientHasDiabetes> ?patienthasdiabetes."
                        + "  ?a <http://www.linked2safety-project.eu/properties/BMI> ?bmi. "
                        + "  ?a <http://www.linked2safety-project.eu/properties/hasHypertension> ?hasdislipidemia. "
                        + " ?a <http://purl.org/linked-data/sdmx/2009/measure#Cases> ?Count.}";

                QueryExecution qe = QueryExecutionFactory.sparqlService(SPARQLendpoint, sparql_query);

                //execute SPARQL query
                com.hp.hpl.jena.query.ResultSet datacude = qe.execSelect();

                //tmt file to store datacubes for workflow
                FileOutputStream fout = new FileOutputStream(datasetDir + "dataset_" + datasetid + ".csv");

                //write dataset to csv file
                ResultSetFormatter.outputAsCSV(fout, datacude);

                // PrintWriter out = new PrintWriter(datasetDir + "workflowinput_" + datasetid + ".xml");
                //   out.println("<Workflow description_of_workflow=\"input_data\" date_of_workflow=\"21 Mar 2013\">\n"
                //           + "<Dataset SPARQL_query=\"" + sparql_query + "\" time=\"2010-03-08 14:59:30.252\"/>\n"
                //            + "</Workflow>");
                //    out.close();
                String execString = "python " + galaxyToolsDir + "KnowledgeExtractionAndFilteringMechanism/workflowLogging.py "
                        + usermail + " " + "/ " + datasetDir + "dataset_" + datasetid + ".csv "
                        + outputDir + "fakeXML.xml 0";
                Process p1 = Runtime.getRuntime().exec(execString);
                p1.waitFor();
                System.out.println(execString);

            }
        } catch (QueryParseException qpe) {
            System.out.println("Cannot parse SPARQL query");
            error = true;
            //  qpe.printStackTrace();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            error = true;
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            error = true;
        } catch (IOException e) {
            System.out.println("Cannot run workflowlogging");
            e.printStackTrace();
            error = true;

        } catch (InterruptedException ex) {
            Logger.getLogger(repeatWorkflows.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return error;
    }
}
