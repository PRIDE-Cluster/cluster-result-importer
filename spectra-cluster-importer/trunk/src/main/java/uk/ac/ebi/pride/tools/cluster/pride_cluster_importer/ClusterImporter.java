package uk.ac.ebi.pride.tools.cluster.pride_cluster_importer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.log4j.Logger;

import uk.ac.ebi.pride.tools.cluster.pride_cluster_importer.CliOptions.OPTIONS;


/**
 * Hello world!
 *
 */
public class ClusterImporter 
{
	private static final Logger logger = Logger.getLogger(ClusterImporter.class);
	private static final Pattern mergedMethodPattern = Pattern.compile("(\\d+\\.\\d+)\\s*+-\\s*\\d+\\.\\d+\\s*-\\s*(\\d+\\.\\d+)\\s*+-.*");
	private static final Pattern singleRegionPattern = Pattern.compile("(\\d+\\.\\d+).*");
	
	private String methodPostfix = "";
	private Connection databaseConnection;
	
	private PreparedStatement stmtCreateMethod;
	private PreparedStatement stmtCreateCluster;
	private PreparedStatement stmtAddSpec;
	private PreparedStatement stmtGetClusterSequences;
	private PreparedStatement stmtAddPep;
	private PreparedStatement stmtUidExists;
	
    public static void main( String[] args )
    {
    	CommandLineParser parser = new GnuParser();

		try {
			CommandLine commandLine = parser.parse(CliOptions.getOptions(), args);
					
			// HELP
			if (commandLine.hasOption(OPTIONS.HELP.getValue())) {
				printUsage();
				return;
			}
			
			// IN
			File file = null;
			if (commandLine.hasOption(OPTIONS.FILE.getValue()))
				file = new File(commandLine.getOptionValue(OPTIONS.FILE.getValue()));
			else
				throw new Exception("Missing required parameter '" + OPTIONS.FILE.getValue() + "'");
			
			if (!file.exists())
				throw new Exception("Output path must point to a directory.");
			
			ClusterImporter app = new ClusterImporter();

			app.loadClusterFile(file);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
    }
    
    private static void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("PRIDE Cluster - Cluster importer", "Imports cluster results into the PRIDE Cluster database.\n", CliOptions.getOptions(), "\n\n", true);
	}
    
    public ClusterImporter() throws Exception {
    	this.databaseConnection = getClusterConnection();
    }
    
    public void loadClusterFile(File clusterFile) throws Exception {
		if (!clusterFile.exists())
			throw new Exception("Passed cluster result file " + clusterFile.getAbsolutePath() + " does not exist.");
		if (!clusterFile.canRead())
			throw new Exception("Cannot read cluster file");
		
		System.out.println("Loading cluster results from " + clusterFile.getAbsolutePath() + "...");
		
		// used to create cluster UIDs
		int clusterCount = 1;
		Double center1 = null, center2 = null;
		
		// process the file line by line
		FileInputStream fstream = new FileInputStream(clusterFile);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		List<String> specLines = null;
		//Read File Line By Line
		ClusteringMethod method = new ClusteringMethod();
		Cluster cluster = null;
		boolean methodComplete = false;
		
		while ((line = br.readLine()) != null)   {
			if (!methodComplete) {
				if (line.startsWith("name="))
					method.setName(line.substring(5) + (methodPostfix != null ? methodPostfix : ""));
				if (line.startsWith("similarity_method="))
					method.setSimilarityMethod(line.substring(18));
				if (line.startsWith("threshold=")) {
					if (!line.contains("adaptive"))
						method.setThreshold(Double.parseDouble(line.substring(10)));
					else
						method.setThreshold(0.0);	
				}
				if (line.startsWith("fdr=")) {
					if (line.contains("NaN"))
						method.setFdr(0.0);
					else
						method.setFdr(Double.parseDouble(line.substring(4)));
				}
				if (line.startsWith("description=")) {
					method.setDescription(line.substring(12));
					methodComplete = true;
					
					// get the region
					Matcher matcherMerged = mergedMethodPattern.matcher(method.getDescription());
					if (matcherMerged.find()) {
						center1 = Double.parseDouble(matcherMerged.group(1));
						center2 = Double.parseDouble(matcherMerged.group(2));
					}
					else {
						Matcher single = singleRegionPattern.matcher(method.getDescription());
						if (!single.find())
							throw new Exception("Invalid method description encountered: " + method.getDescription());
						center1 = Double.parseDouble(single.group(1));
					}
					
					// save the method in the db
					createClusteringMethod(method);
//					logger.debug("Method " + method.getId() + " created.");
				}
			}
			else {
				line = line.trim();
				
				if (line.length() < 1)
					continue;
				else if (line.equals("=Cluster=")) {
					// check if the cluster already exists
					boolean uidExists = false;
					if (cluster != null && cluster.getUid() != null)
						uidExists = clusterExists(cluster.getUid());
					
					// process the previous spec lines
					if (!uidExists && specLines != null && specLines.size() > 1) {						
						// create the cluster if it wasn't created yet
						if (cluster.getId() == null) {
							if (cluster.getSequence().length() > 255)
								cluster.setSequence(cluster.getSequence().substring(0, 250) + "...");
							
							// if the cluster doesn't contain a UID create one
							if (cluster.getUid() == null && center1 == null)
								throw new Exception("Failed to extract center 1 form clustering method name. Failed to create cluster UID");
							if (cluster.getUid() == null) {
								if (center2 != null)
									cluster.setUid(center1 + "-" + center2 + "_" + clusterCount);
								else
									cluster.setUid(center1 + "_" + clusterCount);
								
								clusterCount++;
							}
							
							createCluster(cluster);
						}
						
						// create the spec-to-cluster link
                        List<Integer> specIds = new ArrayList<Integer>();
						for (String specLine : specLines) {
							String[] fields = specLine.split("\t");
							if (fields.length != 3) {
								databaseConnection.rollback();
								databaseConnection.close();
								throw new Exception("Invalid spectrum to cluster line found: " + line);
							}
								
							
							// create the link
                            int spectrumID = Integer.parseInt(fields[1]);
                            if (!specIds.contains(spectrumID)) {
                                specIds.add(spectrumID);
                                addSpectrumToCluster(spectrumID, cluster.getId(), Boolean.parseBoolean(fields[2]));
                            } else {
                                logger.error("duplicate spectrum in cluster: " + cluster.getId() + " spectrum: " + spectrumID);
                            }

						}
						
						updateClusterHasPeptide(cluster.getId());
					}
					
					cluster = new Cluster();
					cluster.setClusteringMethodId(method.getId());
					cluster.setIsReliable(false);
					cluster.setMods("");
					specLines = new ArrayList<String>();
				}
				
				else if (line.startsWith("uid="))
					cluster.setUid(line.substring(4));
				else if (line.startsWith("av_precursor_mz="))
					cluster.setAvPrecursorMz(Double.parseDouble(line.substring(16)));
				else if (line.startsWith("sequence="))
					cluster.setSequence(line.substring(9));
				else if (line.startsWith("consensus_mz="))
					cluster.setConsensusMz(line.substring(13));
				else if (line.startsWith("consensus_intens="))
					cluster.setConsensusIntensity(line.substring(17));
				else if (line.startsWith("threshold="))
					cluster.setThreshold(Double.parseDouble(line.substring(10)));
				else if (line.startsWith("SPEC")) {
					specLines.add(line);
				}
			}
		}
		
		if (specLines != null && specLines.size() > 1) {						
			// create the cluster if it wasn't created yet
			if (cluster.getId() == null) {
				if (cluster.getSequence().length() > 255)
					cluster.setSequence(cluster.getSequence().substring(0, 250) + "...");
				
				createCluster(cluster);
//				logger.debug("Cluster created.");
			}
			
			// create the spec-to-cluster link
            List<Integer> specIds = new ArrayList<Integer>();
            for (String specLine : specLines) {
				String[] fields = specLine.split("\t");
				if (fields.length != 3) {
					databaseConnection.rollback();
					databaseConnection.close();
					throw new Exception("Invalid spectrum to cluster line found: " + line);
				}
				
				// create the link
                int spectrumID = Integer.parseInt(fields[1]);
                if (!specIds.contains(spectrumID)) {
                    specIds.add(spectrumID);
                    addSpectrumToCluster(spectrumID, cluster.getId(), Boolean.parseBoolean(fields[2]));
                } else {
                    logger.error("duplicate spectrum in cluster: " + cluster.getId() + " spectrum: " + spectrumID);
                }
            }
			
			updateClusterHasPeptide(cluster.getId());
		}
		
		databaseConnection.commit();
		databaseConnection.close();
		System.out.println("Cluster file successfully loaded into database.");
	}
    
    private void updateClusterHasPeptide(int clusterId) throws Exception {
    	if (stmtGetClusterSequences == null) {
    		stmtGetClusterSequences = databaseConnection.prepareStatement(
    				"SELECT sequence, COUNT(*) AS count " +
    				"FROM peptide p, cluster_has_spectrum cs " +
    				"WHERE p.spectrum_id = cs.spectrum_id AND cs.cluster_id = ? " +
    				"GROUP BY p.sequence " +
    				"ORDER BY count DESC");
    	}
    	if (stmtAddPep == null) {
    		stmtAddPep = databaseConnection.prepareCall(
    				"INSERT INTO cluster_has_peptide (cluster_id, peptide_id, rank, ratio) " +
    				"SELECT " +
    				"	cluster_id, id, ?, ? " +
    				"FROM " +
    				"	peptide p, cluster_has_spectrum cs " +
    				"WHERE " +
    				"	p.spectrum_id = cs.spectrum_id AND " +
    				"	cs.cluster_id = ? AND " +
    				"	p.sequence = ?"
    		);
    	}
    	
    	stmtGetClusterSequences.setInt(1, clusterId);
    	
    	ResultSet rs = stmtGetClusterSequences.executeQuery();
    	
    	Map<String, Integer> sequences = new HashMap<String, Integer>();
    	Set<String> originalSequences = new HashSet<String>();
    	Map<String, Integer> ranks = new HashMap<String, Integer>();
    	double totalSequences = 0;
    	int rank = 1;
    	
    	while (rs.next()) {
    		String sequence = rs.getString("sequence").replaceAll("I", "L");
    		
    		if (!sequences.containsKey(sequence)) {
    			sequences.put(sequence, rs.getInt("count"));
    			ranks.put(sequence, rank); // use the lowest rank
    			rank++;
    		}
    		else {
    			sequences.put(sequence, sequences.get(sequence) + rs.getInt("count"));
    		}
    		
    		originalSequences.add(rs.getString("sequence"));
    		
    		totalSequences += rs.getInt("count");    		
    	}
    	
    	// update the peptides
    	for (String originalSequence : originalSequences) {
    		String sequence = originalSequence.replaceAll("I", "L");
    		
    		stmtAddPep.setInt(1, ranks.get(sequence));
    		stmtAddPep.setDouble(2, (double) ( (double) sequences.get(sequence) / totalSequences));
    		stmtAddPep.setInt(3, clusterId);
    		stmtAddPep.setString(4, originalSequence);
    		
    		stmtAddPep.executeUpdate();
    	}
    }

	private void addSpectrumToCluster(int specId, Integer clusterId,
			boolean isTarget) throws Exception {
		if (stmtAddSpec == null) {
			stmtAddSpec = databaseConnection.prepareStatement("" +
				"INSERT INTO cluster_has_spectrum (cluster_id, spectrum_id) VALUES (?, ?)"
			);
		}
		
		stmtAddSpec.clearParameters();
		stmtAddSpec.setLong(1, clusterId);
		stmtAddSpec.setLong(2, specId);
		
		stmtAddSpec.executeUpdate();
	}

	private void createCluster(Cluster cluster) throws Exception {
		if (stmtCreateCluster == null) {
			stmtCreateCluster = databaseConnection.prepareStatement(
				"INSERT INTO cluster (clustering_method_id, uid, av_precursor_mz, consensus_spec_mz, consensus_spec_intens, threshold) VALUES(?, ?, ?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
			);
		}

		stmtCreateCluster.clearParameters();
		stmtCreateCluster.setLong(1, cluster.getClusteringMethodId());
		stmtCreateCluster.setString(2, cluster.getUid());
		stmtCreateCluster.setDouble(3, cluster.getAvPrecursorMz());
		stmtCreateCluster.setString(4, cluster.getConsensusMz());
		stmtCreateCluster.setString(5, cluster.getConsensusIntensity());
		stmtCreateCluster.setDouble(6, 0.0);
		
		stmtCreateCluster.executeUpdate();
		
		int id = -1;
		ResultSet rs = stmtCreateCluster.getGeneratedKeys();
		if (rs.next()){
		    id = rs.getInt(1);
		}
		
		cluster.setId(id);
	}

	private void createClusteringMethod(ClusteringMethod method) throws Exception {
		if (stmtCreateMethod == null) {
			stmtCreateMethod = databaseConnection.prepareStatement(
				"INSERT INTO clustering_method(name, iterations, threshold, region) VALUES(?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
			);
		}
		
		int pos = method.getName().lastIndexOf('R');
		if (pos < 0)
			throw new Exception("Invalid method name encountered: " + method.getName());
		
		stmtCreateMethod.clearParameters();
		stmtCreateMethod.setString(1, method.getName().substring(0, pos));
		stmtCreateMethod.setInt(2, Integer.parseInt(method.getName().substring(pos + 1)));
		stmtCreateMethod.setDouble(3, method.getThreshold());
		stmtCreateMethod.setString(4, method.getDescription());
		
		stmtCreateMethod.executeUpdate();
		
		int id = -1;
		ResultSet rs = stmtCreateMethod.getGeneratedKeys();
		if (rs.next()){
		    id = rs.getInt(1);
		}
		
		method.setId(id);
	}
	
	/**
	 * Checks whether a cluster with the passed UID already exists.
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	private boolean clusterExists(String uid) throws Exception {
		if (stmtUidExists == null) {
			stmtUidExists = databaseConnection.prepareStatement("SELECT COUNT(*) FROM cluster WHERE uid = ?");
		}
		
		stmtUidExists.clearParameters();
		stmtUidExists.setString(1, uid);
		
		ResultSet rs = stmtUidExists.executeQuery();
		if (!rs.next())
			throw new Exception("Failed to check whether UID already exists");
		
		return rs.getInt(1) > 0;
	}
	
	private Connection getClusterConnection() throws Exception {

		// load the poperties
		Properties properties = new Properties();
		properties.load(this.getClass().getResourceAsStream("/pride_cluster_db.properties"));
		
		// register the JDBC driver
		Class.forName("com.mysql.jdbc.Driver");
		
		// create the connection url
		String url = "jdbc:mysql://" + properties.getProperty("pride_cluster_db.host") +
			        ":" + properties.getProperty("pride_cluster_db.port") +
			        "/" + properties.getProperty("pride_cluster_db.dbname");
		
		logger.debug("Connecting to the PRIDE Cluster database at " + properties.getProperty("pride_cluster_db.host") + "...");
				
		// connect to the database
		Connection connection = DriverManager.getConnection(	url, 
													properties.getProperty("pride_cluster_db.user"), 
													properties.getProperty("pride_cluster_db.pass"));
		
		connection.setAutoCommit(false);
		
		return connection;
	}
}