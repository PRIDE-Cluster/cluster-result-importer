package uk.ac.ebi.pride.tools.cluster.model;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class PSMSummaryTest {

    @Test
    public void testEqualsMethod() throws Exception {

        PSMSummary psm1 = new PSMSummary();
        psm1.setSpectrumId(11111l);
        psm1.setSequence("AAAAAAA");
        psm1.setModifications("MOD1:MOD2");
        psm1.setArchivePSMId("PSM1");
        psm1.setSearchEngine("Engine1");

        PSMSummary psm2 = new PSMSummary();
        psm2.setSpectrumId(11111l);
        psm2.setSequence("AAAAAAA");
        psm2.setModifications("MOD1:MOD2");
        psm2.setArchivePSMId("PSM1");
        psm2.setSearchEngine("Engine2");


        assertEquals(psm1, psm2);
    }


    @Test
    public void testNotEqual() throws Exception {

        PSMSummary psm1 = new PSMSummary();
        psm1.setSpectrumId(11111l);
        psm1.setSequence("BBBBBB");
        psm1.setModifications("MOD1:MOD2");
        psm1.setArchivePSMId("PSM1");
        psm1.setSearchEngine("Engine1");

        PSMSummary psm2 = new PSMSummary();
        psm2.setSpectrumId(11111l);
        psm2.setSequence("AAAAAAA");
        psm2.setModifications("MOD1:MOD2");
        psm2.setArchivePSMId("PSM1");
        psm2.setSearchEngine("Engine2");


        assertNotSame(psm1, psm2);
    }

    @Test
    public void testLisContain() throws Exception {
        PSMSummary psm1 = new PSMSummary();
        psm1.setSpectrumId(11111l);
        psm1.setSequence("AAAAAAA");
        psm1.setModifications("MOD1:MOD2");
        psm1.setArchivePSMId("PSM1");
        psm1.setSearchEngine("Engine1");

        PSMSummary psm2 = new PSMSummary();
        psm2.setSpectrumId(11111l);
        psm2.setSequence("AAAAAAA");
        psm2.setModifications("MOD1:MOD2");
        psm2.setArchivePSMId("PSM1");
        psm2.setSearchEngine("Engine2");

        ArrayList<PSMSummary> psmSummaries = new ArrayList<PSMSummary>();
        psmSummaries.add(psm1);

        assertTrue(psmSummaries.contains(psm2));
    }


}
