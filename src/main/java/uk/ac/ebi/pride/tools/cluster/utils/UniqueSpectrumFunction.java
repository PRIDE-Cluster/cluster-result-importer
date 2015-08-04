package uk.ac.ebi.pride.tools.cluster.utils;

import uk.ac.ebi.pride.spectracluster.clusteringfilereader.objects.ISpectrumReference;
import uk.ac.ebi.pride.spectracluster.util.function.IFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Function to remove duplicated spectra within a collection
 *
 * @author Rui Wang
 * @version $Id$
 */
public class UniqueSpectrumFunction implements IFunction<Collection<ISpectrumReference>, Collection<ISpectrumReference>> {


    @Override
    public Collection<ISpectrumReference> apply(Collection<ISpectrumReference> spectra) {
        Collection<ISpectrumReference> uniqueSpectra = new ArrayList<ISpectrumReference>();
        Set<String> idTracker = new HashSet<String>();

        for (ISpectrumReference spectrumReference : spectra) {
            String spectrumId = spectrumReference.getSpectrumId();

            if (!idTracker.contains(spectrumId)) {
                idTracker.add(spectrumId);
                uniqueSpectra.add(spectrumReference);
            }
        }

        return uniqueSpectra;
    }
}
