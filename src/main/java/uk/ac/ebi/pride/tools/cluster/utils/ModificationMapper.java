package uk.ac.ebi.pride.tools.cluster.utils;

import uk.ac.ebi.pridemod.controller.impl.PRIDEModDataAccessController;
import uk.ac.ebi.pridemod.model.PTM;
import uk.ac.ebi.pridemod.model.Specificity;

import java.io.InputStream;
import java.util.List;

/**
 * Mapper for mapping modifications using David's mapping for PRIDE Archive
 *
 * @author Rui Wang
 * @version $Id$
 */
public class ModificationMapper {
    private final static ModificationMapper instance = new ModificationMapper();

    public static ModificationMapper getInstance() {
        return instance;
    }

    private final PRIDEModDataAccessController prideModDataAccessController;

    private ModificationMapper() {
        // pride
        InputStream prideModStream = ModificationMapper.class.getClassLoader().getResourceAsStream("mod/pride_mods.xml");
        prideModDataAccessController = new PRIDEModDataAccessController(prideModStream);
    }

    public InputStream getSource() {
        return prideModDataAccessController.getSource();
    }

    public List<PTM> getPTMListByPatternName(String namePattern) {
        return prideModDataAccessController.getPTMListByPatternName(namePattern);
    }

    public List<PTM> getPTMListByMonoDeltaMass(Double delta) {
        return prideModDataAccessController.getPTMListByMonoDeltaMass(delta);
    }

    public List<PTM> getPTMListByAvgDeltaMass(Double delta) {
        return prideModDataAccessController.getPTMListByAvgDeltaMass(delta);
    }

    public List<PTM> getPTMListBySpecificity(Specificity specificity) {
        return prideModDataAccessController.getPTMListBySpecificity(specificity);
    }

    public List<PTM> getPTMListByPatternDescription(String descriptionPattern) {
        return prideModDataAccessController.getPTMListByPatternDescription(descriptionPattern);
    }

    public List<PTM> getPTMListByEqualName(String name) {
        return prideModDataAccessController.getPTMListByEqualName(name);
    }

    public PTM getPTMbyAccession(String accession) {
        return prideModDataAccessController.getPTMbyAccession(accession);
    }

    public static void main(String[] args) {
        ModificationMapper modificationMapper = ModificationMapper.getInstance();
        PTM ptMbyAccession = modificationMapper.getPTMbyAccession("MOD:00781");
        System.out.println("test");
    }
}
