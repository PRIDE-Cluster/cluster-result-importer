package uk.ac.ebi.pride.tools.cluster.annotator;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MzTabIndexerTest {
    @Test
    public void testFileNameIsCorrectlyParsed() throws Exception {
        String file = "file:C:\\Users\\Owner\\Desktop\\test2\\cw15-HILIC-polyMAC-3 bioreps combine-20130122 mzIdent export 14-Feb-2014 10-53-57-AM\\BioSample 1\\mascot_daemon_merge_F008921.mzid_mascot_daemon_merge_F008921.MGF";

        String fileName = FilenameUtils.getName(file);

        assertEquals("mascot_daemon_merge_F008921.mzid_mascot_daemon_merge_F008921.MGF", fileName);

    }
}