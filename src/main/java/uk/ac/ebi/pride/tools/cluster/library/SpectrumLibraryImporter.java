package uk.ac.ebi.pride.tools.cluster.library;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import uk.ac.ebi.pride.spectracluster.repo.model.SpectrumLibraryDetail;
import uk.ac.ebi.pride.tools.cluster.dao.TransactionAwareSpectrumLibraryWriter;
import uk.ac.ebi.pride.tools.cluster.repo.ClusterRepositoryBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class SpectrumLibraryImporter {

    private static final Logger logger = LoggerFactory.getLogger(SpectrumLibraryImporter.class);

    public static final String NUMBER_OF_PEPTIDE = "NUMBER_OF_PEPTIDE";
    public static final String NUMBER_OF_SPECTRA = "NUMBER_OF_SPECTRA";
    public static final String SCIENTIFIC_NAME = "SCIENTIFIC_NAME";
    public static final String HUMAN_READABLE_NAME = "HUMAN_READABLE_NAME";
    public static final String TAXONOMY = "TAXONOMY";

    public static void main(String[] args) throws IOException, ParseException {
        if (args.length < 5) {
            System.err.println("Usage: SpectrumLibraryImporter [path to library files] [library statistic file] [species mapping file] [version] [release date]");
        }

        // version number
        String version = args[3];

        // release date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");
        Date releaseDate = simpleDateFormat.parse(args[4]);

        // Read library statistic file
        Map<String, Map<String, Long>> spectrumLibraryStats = parseStatisticFile(args[1]);

        // Read species mapping file
        Map<String, Map<String, String>> speciesMappings = parseSpeciesMappingFile(args[2]);

        // Read all the spectrum library files
        Map<String, Long> spectrumLibraries = findSpectrumLibraries(args[0]);

        // write to database
        // create data source
        ClusterRepositoryBuilder clusterRepositoryBuilder = new ClusterRepositoryBuilder("prop/cluster-database-oracle.properties");

        // create cluster reader
        DataSourceTransactionManager transactionManager = clusterRepositoryBuilder.getTransactionManager();

        // create cluster repo statistics writer
        TransactionAwareSpectrumLibraryWriter spectrumLibraryWriter = new TransactionAwareSpectrumLibraryWriter(transactionManager);

        for (Map.Entry<String, Long> spectrumLibrary : spectrumLibraries.entrySet()) {
            SpectrumLibraryDetail spectrumLibraryDetail = new SpectrumLibraryDetail();
            String spectrumLibraryFileName = spectrumLibrary.getKey();
            logger.info("Spectrum library: " + spectrumLibraryFileName);
            spectrumLibraryDetail.setFileName(spectrumLibraryFileName);
            spectrumLibraryDetail.setFileSize(spectrumLibrary.getValue());
            spectrumLibraryDetail.setVersion(version);
            spectrumLibraryDetail.setReleaseDate(releaseDate);
            // stats
            Map<String, Long> stats = spectrumLibraryStats.get(spectrumLibraryFileName);
            if (stats == null) {
                logger.warn("No stats found for spectrum library: " + spectrumLibraryFileName);
                continue;
            }
            spectrumLibraryDetail.setNumberOfSpectra(stats.get(NUMBER_OF_SPECTRA));
            spectrumLibraryDetail.setNumberOfPeptides(stats.get(NUMBER_OF_SPECTRA));
            // metadata
            Map<String, String> metadata = speciesMappings.get(spectrumLibraryFileName);
            if (metadata == null) {
                logger.warn("No metadata found for spectrum library: " + spectrumLibraryFileName);
                continue;
            }
            spectrumLibraryDetail.setSpeciesScientificName(metadata.get(SCIENTIFIC_NAME));
            spectrumLibraryDetail.setSpeciesName(metadata.get(HUMAN_READABLE_NAME));
            spectrumLibraryDetail.setTaxonomyId(new Long(metadata.get(TAXONOMY)));

            spectrumLibraryWriter.saveSpectrumLibrary(spectrumLibraryDetail);
        }

    }

    private static Map<String, Map<String, Long>> parseStatisticFile(String statisticFile) throws IOException {
        Map<String, Map<String, Long>> spectrumLibraryStats = new HashMap<String, Map<String, Long>>();
        BufferedReader statisticFileReader = new BufferedReader(new FileReader(statisticFile));
        // ingnore the first line
        String line = statisticFileReader.readLine();
        while ((line = statisticFileReader.readLine()) != null) {
            String[] parts = line.split("\t");
            String spectrumLibraryFileName = parts[0];
            Long numberOfSpectra = new Long(parts[1]);
            Long numberOfPeptides = new Long(parts[2]);

            HashMap<String, Long> content = new HashMap<String, Long>();
            content.put(NUMBER_OF_SPECTRA, numberOfSpectra);
            content.put(NUMBER_OF_PEPTIDE, numberOfPeptides);

            spectrumLibraryStats.put(spectrumLibraryFileName, content);

        }
        statisticFileReader.close();

        return spectrumLibraryStats;
    }

    private static Map<String, Map<String, String>> parseSpeciesMappingFile(String speciesMappingFile) throws IOException {
        Map<String, Map<String, String>> speciesMapping = new HashMap<String, Map<String, String>>();
        BufferedReader fileReader = new BufferedReader(new FileReader(speciesMappingFile));
        // ingnore the first line
        String line = fileReader.readLine();
        while ((line = fileReader.readLine()) != null) {
            String[] parts = line.split("\t");
            String spectrumLibraryFileName = parts[0];
            String scientificName = parts[1];
            String humanReadableName = parts[2];
            String taxonomy = parts[3];

            HashMap<String, String> content = new HashMap<String, String>();
            content.put(SCIENTIFIC_NAME, scientificName);
            content.put(HUMAN_READABLE_NAME, humanReadableName);
            content.put(TAXONOMY, taxonomy);

            speciesMapping.put(spectrumLibraryFileName, content);

        }
        fileReader.close();

        return speciesMapping;
    }

    private static Map<String, Long> findSpectrumLibraries(String spectrumLibraryFolderPath) throws IOException {
        Map<String, Long> spectrumLibraries = new HashMap<String, Long>();

        File spectrumLibraryFolder = new File(spectrumLibraryFolderPath);
        for (File file : spectrumLibraryFolder.listFiles()) {
            if (file.getName().endsWith(".msp.gz")) {
                spectrumLibraries.put(file.getName(), file.length());
            }
        }


        return spectrumLibraries;
    }

}
