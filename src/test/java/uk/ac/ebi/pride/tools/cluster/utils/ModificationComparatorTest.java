package uk.ac.ebi.pride.tools.cluster.utils;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab.model.Modification;
import uk.ac.ebi.pride.jmztab.model.PSM;
import uk.ac.ebi.pride.jmztab.model.SplitList;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ModificationComparatorTest {
    private MzTabIndexer mzTabIndexer;

    @Before
    public void setUp() throws Exception {
        URL resource = ModificationComparatorTest.class.getClassLoader().getResource("mztab/iTRAQ_SQI.mzTab");
        File mzTabFile = new File(resource.toURI());
        mzTabIndexer = new MzTabIndexer(mzTabFile);
    }

    @Test
    public void testCompare() throws Exception {
        Set<PSM> psms = mzTabIndexer.findPSMUsingSpectrumId("ms_run[1]:scan=1300");
        PSM psm = psms.iterator().next();

        SplitList<Modification> modifications = psm.getModifications();
        assertEquals("20-UNIMOD:214,2-UNIMOD:214", modifications.toString());

        ModificationComparator modificationComparator = ModificationComparator.getInstance();
        Collections.sort(modifications, modificationComparator);

        assertEquals("2-UNIMOD:214,20-UNIMOD:214", modifications.toString());
    }
}