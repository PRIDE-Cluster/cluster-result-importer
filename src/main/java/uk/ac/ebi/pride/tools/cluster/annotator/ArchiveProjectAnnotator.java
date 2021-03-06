package uk.ac.ebi.pride.tools.cluster.annotator;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileSource;
import uk.ac.ebi.pride.archive.dataprovider.file.ProjectFileType;
import uk.ac.ebi.pride.archive.repo.assay.Assay;
import uk.ac.ebi.pride.archive.repo.assay.AssayRepository;
import uk.ac.ebi.pride.archive.repo.file.ProjectFile;
import uk.ac.ebi.pride.archive.repo.file.ProjectFileRepository;
import uk.ac.ebi.pride.archive.repo.project.Project;
import uk.ac.ebi.pride.archive.repo.project.ProjectRepository;
import uk.ac.ebi.pride.jmztab.model.MsRun;
import uk.ac.ebi.pride.jmztab.model.PSM;
import uk.ac.ebi.pride.spectracluster.io.ParserUtilities;
import uk.ac.ebi.pride.spectracluster.repo.dao.cluster.IClusterWriteDao;
import uk.ac.ebi.pride.spectracluster.repo.model.AssayDetail;
import uk.ac.ebi.pride.spectracluster.repo.model.PSMDetail;
import uk.ac.ebi.pride.spectracluster.repo.model.SpectrumDetail;
import uk.ac.ebi.pride.spectracluster.spectrum.ISpectrum;
import uk.ac.ebi.pride.tools.cluster.dao.TransactionAwareClusterWriter;
import uk.ac.ebi.pride.tools.cluster.repo.ArchiveRepositoryBuilder;
import uk.ac.ebi.pride.tools.cluster.repo.ClusterRepositoryBuilder;
import uk.ac.ebi.pride.tools.cluster.utils.Constants;
import uk.ac.ebi.pride.tools.cluster.utils.MzTabIndexer;
import uk.ac.ebi.pride.tools.cluster.utils.SummaryFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

/**
 * Add annotation for a particular project
 * This class makes two assumptions:
 * <p/>
 * 1. Both mzTab and MGF files have been generated already
 * 2. Project has already been made public
 * <p/>
 * The loading steps consist the following steps for each assay:
 * <p/>
 * 1. Load and insert the assay details into the database
 * 2. Get generated mzTab files and MGF files
 * 3. For each mzTab file, find its related MGF files
 * 4. Insert spectra into database
 * 5. Insert PSMs into database
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ArchiveProjectAnnotator {

    public static final Logger logger = LoggerFactory.getLogger(ArchiveProjectAnnotator.class);

    private final ArchiveRepositoryBuilder archiveRepositoryBuilder;
    private final IClusterWriteDao clusterWriter;
    private final String archiveFileRootPath;

    public ArchiveProjectAnnotator(ArchiveRepositoryBuilder archiveRepositoryBuilder,
                                   IClusterWriteDao clusterWriter,
                                   String archiveFileRootPath) {
        this.archiveRepositoryBuilder = archiveRepositoryBuilder;
        this.clusterWriter = clusterWriter;
        this.archiveFileRootPath = archiveFileRootPath;
    }

    public void annotate(String projectAccession) {
        logger.info("Trying to annotation project: {}", projectAccession);

        ProjectRepository projectRepository = archiveRepositoryBuilder.getProjectRepository();
        Project project = projectRepository.findByAccession(projectAccession);

        // check whether project is already public
        if (!project.isPublicProject()) {
            String msg = "Project must be public: " + project.getAccession();
            logger.error(msg);
            throw new IllegalStateException(msg);
        }

        // project file path
        String projectFilePath = getFilePath(archiveFileRootPath, project);

        // get all the assays
        AssayRepository assayRepository = archiveRepositoryBuilder.getAssayRepository();
        List<Assay> assays = assayRepository.findAllByProjectId(project.getId());
        for (Assay assay : assays) {
            logger.info("Storing assay {} for project {}", assay.getAccession(), project.getAccession());
            loadMetaDataByAssay(project, projectFilePath, assay);
        }

        logger.info("Project annotation has finished: {}", projectAccession);
    }

    /**
     * load metadata of a given assay
     *
     * @param project
     * @param projectFilePath
     * @param assay
     */
    private void loadMetaDataByAssay(Project project, String projectFilePath, Assay assay) {
        ProjectFileRepository projectFileRepository = archiveRepositoryBuilder.getProjectFileRepository();

        // find all the mzTab files
        List<ProjectFile> assayFiles = projectFileRepository.findAllByAssayId(assay.getId());

        File mzTabFile = findMzTabFile(projectFilePath, assayFiles);
        if (mzTabFile != null) {
            logger.info("Found mzTab file: {}", mzTabFile.getAbsolutePath());
            // parse mztab object
            try {
                loadMetaDataByMgfs(project, assay, mzTabFile, projectFilePath);
            } catch (IOException e) {
                String msg = "Failed to read mzTab file: " + mzTabFile.getAbsolutePath();
                logger.error(msg);
                throw new IllegalStateException(msg, e);
            }
        }
    }

    /**
     * Load all the metadata into data store
     *
     * @param project   project object
     * @param assay     assay object
     * @param mzTabFile mzTab file
     */
    private void loadMetaDataByMgfs(Project project, Assay assay, File mzTabFile, String projectFilePath) throws IOException {
        MzTabIndexer mzTabIndexer = null;

        try {
            mzTabIndexer = new MzTabIndexer(mzTabFile);
        } catch (Exception ex) {
            logger.error("Failed to load metadata for mzTab file: {}", mzTabFile.getAbsolutePath());
            //NOTE: this is to avoid mzTab parsing exception to cause the whole project load fail
        }

        if (mzTabIndexer != null) {
            AssayDetail assaySummary = SummaryFactory.summariseAssay(project, assay);
            clusterWriter.saveAssay(assaySummary);
            logger.info("Assay summary annotated. Assay database id: {}", assaySummary.getId());

            Set<String> msRunFileNames = mzTabIndexer.getMsRunFileNames();
            for (String msRunFileName : msRunFileNames) {
                String mgfFileName = FilenameUtils.removeExtension(msRunFileName) + Constants.PRIDE_MGF_SUFFIX;
                File mgfFile = new File(projectFilePath + File.separator + Constants.INTERNAL_DIRECTORY + File.separator + mgfFileName);
                if (mgfFile.exists()) {
                    logger.info("loading mgf file {}", mgfFile.getAbsolutePath());
                    loadMetaDataByMgf(assaySummary, mzTabIndexer, mgfFile);
                }
            }
        }

    }

    /**
     * Load all the spectra of a given MGF file along with the related PSMs
     *
     * @param assaySummary assay summary
     * @param mzTabIndexer mzTab object
     * @param mgfFile      MGF file to load
     * @throws IOException
     */
    private void loadMetaDataByMgf(AssayDetail assaySummary, MzTabIndexer mzTabIndexer, File mgfFile) throws IOException {
        Long assaySummaryId = assaySummary.getId();
        String assayAccession = assaySummary.getAccession();
        String projectAccession = assaySummary.getProjectAccession();
        int numberOfMsRuns = mzTabIndexer.getNumberOfMsRuns();

        // load identified spectra in batch
        LineNumberReader rdr = null;
        try {
            rdr = new LineNumberReader(new FileReader(mgfFile));
            Map<SpectrumDetail, List<PSMDetail>> spectrumSummaryBuffer = new HashMap<SpectrumDetail, List<PSMDetail>>();
            ISpectrum spectrum;
            while ((spectrum = ParserUtilities.readMGFScan(rdr)) != null) {
                if (spectrum.getPrecursorCharge() > 0 && spectrum.getPrecursorMz() > 0) {
                    Set<PSM> psms = getPSM(spectrum.getId(), mzTabIndexer);

                    if (psms != null && !psms.isEmpty()) {
                        if (spectrumSummaryBuffer.size() == 5000) {
                            storeSpectrumAndPSM(spectrumSummaryBuffer);
                            spectrumSummaryBuffer.clear();
                        }

                        SpectrumDetail spectrumSummary = SummaryFactory.summariseSpectrum(spectrum, assaySummaryId, true);
                        List<PSMDetail> psmSummaries = new ArrayList<PSMDetail>();
                        for (PSM psm : psms) {
                            PSMDetail psmSummary = SummaryFactory.summarisePSM(psm, projectAccession, assaySummaryId, assayAccession, numberOfMsRuns);
                            psmSummaries.add(psmSummary);
                        }
                        spectrumSummaryBuffer.put(spectrumSummary, psmSummaries);
                    }
                }
            }

            storeSpectrumAndPSM(spectrumSummaryBuffer);
            spectrumSummaryBuffer.clear();
        } finally {
            if (rdr != null)
                rdr.close();
        }
    }

    private void storeSpectrumAndPSM(Map<SpectrumDetail, List<PSMDetail>> spectrumSummaryBuffer) {
        clusterWriter.saveSpectra(new ArrayList<SpectrumDetail>(spectrumSummaryBuffer.keySet()));

        List<PSMDetail> psmSummaries = new ArrayList<PSMDetail>();
        for (Map.Entry<SpectrumDetail, List<PSMDetail>> spectrumSummaryListEntry : spectrumSummaryBuffer.entrySet()) {
            SpectrumDetail spectrum = spectrumSummaryListEntry.getKey();
            List<PSMDetail> psms = spectrumSummaryListEntry.getValue();

            for (PSMDetail psm : psms) {
                psm.setSpectrumId(spectrum.getId());
                if (!psmSummaries.contains(psm)) {
                    psmSummaries.add(psm);
                }
            }
        }

        clusterWriter.savePSMs(psmSummaries);
    }

    protected Set<PSM> getPSM(String spectrumId, MzTabIndexer mzTabIndexer) {
        String[] parts = spectrumId.split(";");
        if (parts.length < 3)
            throw new IllegalStateException("Wrongly formatted spectrum id: " + spectrumId);

        // get ms run
        MsRun msRun = mzTabIndexer.findMsRunUsingFileName(parts[1]);
        if (msRun == null) {
            String message = "Failed to find MS run for spectrum: " + spectrumId;
            logger.warn(message);
            return null;
        } else {
            // spectrum id
            String spectrumRef = msRun.getReference() + ":" + parts[2];
            return mzTabIndexer.findPSMUsingSpectrumId(spectrumRef);
        }
    }

    /**
     * Construct project file path
     *
     * @param rootPath root path to all the projects
     * @param project  project object
     * @return file path to project files
     */
    private String getFilePath(String rootPath, Project project) {
        if (project.isPublicProject()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(project.getPublicationDate());
            int month = calendar.get(Calendar.MONTH) + 1;

            return rootPath
                    + File.separator + calendar.get(Calendar.YEAR)
                    + File.separator + (month < 10 ? "0" : "") + month
                    + File.separator + project.getAccession();
        } else {
            return rootPath + File.separator + project.getAccession();
        }
    }

    /**
     * Find all the generated mzTab files
     *
     * @param projectFilePath
     * @param assayFiles
     * @return
     */
    private File findMzTabFile(String projectFilePath, List<ProjectFile> assayFiles) {
        String resultFileName = null;
        for (ProjectFile assayFile : assayFiles) {
            ProjectFileSource fileSource = assayFile.getFileSource();
            String assayFileFileName = assayFile.getFileName();
            ProjectFileType fileType = assayFile.getFileType();

            if (fileSource.equals(ProjectFileSource.GENERATED) && assayFileFileName.endsWith(Constants.PRIDE_MZTAB_SUFFIX_GZIPPED)) {
                String fileName = assayFileFileName.substring(0, assayFileFileName.length() - 3);
                File file = new File(projectFilePath + File.separator + Constants.INTERNAL_DIRECTORY + File.separator + fileName);
                if (file.exists()) {
                    return file;
                }
            } else if (fileSource.equals(ProjectFileSource.SUBMITTED) && fileType.equals(ProjectFileType.RESULT)) {
                resultFileName = assayFileFileName;
            }
        }

        // try to find file on file system
        if (resultFileName != null) {
            String resultFileNameWithoutExtension = resultFileName.endsWith(Constants.GZIP_FILE_EXTENSION) ?
                    FilenameUtils.removeExtension(resultFileName.substring(0, resultFileName.length() - Constants.GZIP_FILE_EXTENSION.length())) :
                    FilenameUtils.removeExtension(resultFileName);

            File file = new File(projectFilePath + File.separator + Constants.INTERNAL_DIRECTORY +
                    File.separator + resultFileNameWithoutExtension + Constants.PRIDE_MZTAB_SUFFIX);
            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    public static void main(String[] args) {
        String archiveRootFilePath = args[0];
        String projectAccession = args[1];

        // create connection to databases
        ArchiveRepositoryBuilder archiveRepositoryBuilder = new ArchiveRepositoryBuilder("prop/archive-database-oracle.properties");
        ClusterRepositoryBuilder clusterRepositoryBuilder = new ClusterRepositoryBuilder("prop/cluster-database-oracle.properties");
        IClusterWriteDao writer = new TransactionAwareClusterWriter(clusterRepositoryBuilder.getTransactionManager());

        // create project annotator
        ArchiveProjectAnnotator annotator = new ArchiveProjectAnnotator(archiveRepositoryBuilder, writer, archiveRootFilePath);

        // define transaction
        try {
            annotator.annotate(projectAccession);
        } catch (Exception ex) {
            // delete persisted records if there is an exception
            writer.deleteAssayByProjectAccession(projectAccession);

            String message = "Error loading project metadata into PRIDE cluster: " + projectAccession;
            throw new IllegalStateException(message, ex);
        }

    }

}
