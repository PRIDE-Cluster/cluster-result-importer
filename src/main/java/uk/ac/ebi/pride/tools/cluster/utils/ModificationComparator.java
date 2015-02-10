package uk.ac.ebi.pride.tools.cluster.utils;

import uk.ac.ebi.pride.jmztab.model.CVParam;
import uk.ac.ebi.pride.jmztab.model.Modification;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * Comparator for sorting modifications into ascending order
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ModificationComparator implements Comparator<Modification> {

    private static final ModificationComparator comparator = new ModificationComparator();

    public static ModificationComparator getInstance() {
        return comparator;
    }

    private ModificationComparator() {
    }

    @Override
    public int compare(Modification mod1, Modification mod2) {
        Map<Integer, CVParam> mod1PositionMap = mod1.getPositionMap();
        Map<Integer, CVParam> mod2PositionMap = mod2.getPositionMap();

        Set<Integer> mod1Positions = mod1PositionMap.keySet();
        Set<Integer> mod2Positions = mod2PositionMap.keySet();

        if (!mod1Positions.isEmpty() && !mod2Positions.isEmpty()) {
            Integer mod1Position = mod1Positions.iterator().next();
            Integer mod2Position = mod2Positions.iterator().next();

            return Integer.compare(mod1Position, mod2Position);
        }

        return -1;
    }
}
