package uk.ac.ebi.pride.tools.cluster.repo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Rui Wang
 * @version $Id$
 */
public class DBSequenceTest {

    @Test
    public void testSequenceCalculation() throws Exception {
        double increment = 1000.0;

        assertEquals(2.0, Math.ceil(1234/increment), 0.00001);
        assertEquals(2.0, Math.ceil(1566/increment), 0.00001);
        assertEquals(2.0, Math.ceil(1001/increment), 0.00001);
        assertEquals(1.0, Math.ceil(599/increment), 0.00001);

    }
}
