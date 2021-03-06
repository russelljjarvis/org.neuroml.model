package org.neuroml.model.test;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedHashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.neuroml.model.Annotation;

import org.neuroml.model.Cell;
import org.neuroml.model.ExpOneSynapse;
import org.neuroml.model.ExpTwoSynapse;
import org.neuroml.model.ExplicitInput;
import org.neuroml.model.IafCell;
import org.neuroml.model.Include;
import org.neuroml.model.Instance;
import org.neuroml.model.Location;
import org.neuroml.model.IzhikevichCell;
import org.neuroml.model.Member;
import org.neuroml.model.Morphology;
import org.neuroml.model.Network;
import org.neuroml.model.NeuroMLDocument;
import org.neuroml.model.Point3DWithDiam;
import org.neuroml.model.Population;
import org.neuroml.model.PopulationTypes;
import org.neuroml.model.PulseGenerator;
import org.neuroml.model.Segment;
import org.neuroml.model.SegmentGroup;
import org.neuroml.model.SegmentParent;
import org.neuroml.model.Standalone;
import org.neuroml.model.SynapticConnection;
import org.neuroml.model.util.NeuroML2Validator;
import org.neuroml.model.util.NeuroMLConverter;
import static org.neuroml.model.util.NeuroMLConverter.getAllStandaloneElements;
import org.neuroml.model.util.NeuroMLElements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NeuroML2Test {

    String wdir = System.getProperty("user.dir");
    String exampledirname = wdir + File.separator + "target/maven-shared-archive-resources/examples/";
    String tempdirname = wdir + File.separator + "target/maven-shared-archive-resources/examples/";

    //TODO: Move to Utility class
    static final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    static DocumentBuilder docBuilder = null;
    static Document doc = null;

    static {
        try {
            docBuilder = docFactory.newDocumentBuilder();
            doc = docBuilder.newDocument();
        } catch (ParserConfigurationException ex) {
            // continue
        }

    }

    @Test
    public void testCellSave() throws Exception {
        NeuroMLDocument nml2 = new NeuroMLDocument();
        nml2.setId("SomeCells");

        IzhikevichCell iz1 = new IzhikevichCell();
        iz1.setId("Izh0");
        iz1.setV0("-70mV");
        iz1.setThresh("30mV");
        iz1.setA("0.02");
        iz1.setB("0.2");
        iz1.setC("-50");
        iz1.setD("2");
        /*iz1.setIamp("2");
         iz1.setIdel("100 ms");
         iz1.setIdur("100 ms");*/
        nml2.getIzhikevichCell().add(iz1);

        ExpTwoSynapse e2syn = new ExpTwoSynapse();
        e2syn.setId("Syn0");
        e2syn.setTauRise("2ms");
        e2syn.setTauDecay("12ms");
        e2syn.setErev("-10mV");
        e2syn.setGbase("1nS");
        nml2.getExpTwoSynapse().add(e2syn);

        neuroml2ToXml(nml2, nml2.getId() + ".xml", true);

    }

    private NeuroMLDocument getValidDoc() throws Exception {
        NeuroMLDocument nml2 = new NeuroMLDocument();
        nml2.setId("SomeNML");
        Cell cell = new Cell();
        nml2.getCell().add(cell);
        cell.setId("aCell");
        Morphology morph = new Morphology();
        cell.setMorphology(morph);
        morph.setId("themorph");
        Segment soma = new Segment();
        soma.setId(0);
        soma.setName("Soma");
        morph.getSegment().add(soma);
        Point3DWithDiam prox = new Point3DWithDiam();
        prox.setX(0);
        prox.setY(0);
        prox.setZ(0);
        prox.setDiameter(10);
        soma.setProximal(prox);
        soma.setDistal(prox);

        Segment dend1 = new Segment();
        dend1.setId(1);
        dend1.setName("dend1");
        morph.getSegment().add(dend1);
        SegmentParent par = new SegmentParent();
        dend1.setParent(par);
        par.setSegment(soma.getId());
        Point3DWithDiam dist = new Point3DWithDiam();
        dist.setX(10);
        dist.setY(0);
        dist.setZ(0);
        dist.setDiameter(3);
        dend1.setDistal(dist);

        /*
         Segment dend2 = new Segment();
         dend2.setId("2");
         dend2.setName("dend2");
         morph.getSegment().add(dend2);*/
        SegmentGroup segGroupS = new SegmentGroup();
        morph.getSegmentGroup().add(segGroupS);
        segGroupS.setId("soma_group");
        Member membS = new Member();
        membS.setSegment(soma.getId());
        segGroupS.getMember().add(membS);

        SegmentGroup segGroupD = new SegmentGroup();
        morph.getSegmentGroup().add(segGroupD);
        segGroupD.setId("dendrite_group");
        Member membD = new Member();
        membD.setSegment(dend1.getId());
        segGroupD.getMember().add(membD);

        SegmentGroup segGroupDS = new SegmentGroup();
        morph.getSegmentGroup().add(segGroupDS);
        segGroupDS.setId("dendrite_soma_group");
        Include inc1 = new Include();
        inc1.setSegmentGroup("soma_group");
        segGroupDS.getInclude().add(inc1);
        Include inc2 = new Include();
        inc2.setSegmentGroup("dendrite_group");
        segGroupDS.getInclude().add(inc2);

        return nml2;
    }

    @Test
    public void testValidDoc() throws Exception {

        System.out.println("Testing a typical valid doc...");

        NeuroMLDocument nml2 = getValidDoc();

        neuroml2ToXml(nml2, "SomeValidDoc.xml", true);
    }

    @Test
    public void testInvalidDoc() throws Exception {

        System.out.println("Taking typical valid doc & breaking it...");

        // TEST_REPEATED_IDS
        NeuroMLDocument nml2 = getValidDoc();

        nml2.getCell().get(0).getMorphology().getSegment().get(1).setId(0);

        NeuroML2Validator nmlv = new NeuroML2Validator();
        nmlv.validateWithTests(nml2);
        assertTrue(nmlv.getValidity().contains(nmlv.TEST_REPEATED_IDS.description));

        // TEST_ONE_SEG_MISSING_PARENT
        nml2 = getValidDoc();

        nml2.getCell().get(0).getMorphology().getSegment().get(1).setParent(null);
        nmlv = new NeuroML2Validator();

        nmlv.validateWithTests(nml2);
        assertTrue(nmlv.getValidity().contains(nmlv.TEST_ONE_SEG_MISSING_PARENT.description));

        // TEST_MEMBER_SEGMENT_EXISTS
        nml2 = getValidDoc();

        nml2.getCell().get(0).getMorphology().getSegmentGroup().get(1).getMember().get(0).setSegment(3000);
        nmlv = new NeuroML2Validator();

        nmlv.validateWithTests(nml2);
        assertTrue(nmlv.getValidity().contains(nmlv.TEST_MEMBER_SEGMENT_EXISTS.description));

        // TEST_REPEATED_GROUPS
        nml2 = getValidDoc();

        nml2.getCell().get(0).getMorphology().getSegmentGroup().get(1).setId("soma_group");
        nmlv = new NeuroML2Validator();

        nmlv.validateWithTests(nml2);
        assertTrue(nmlv.getValidity().contains(nmlv.TEST_REPEATED_GROUPS.description));

        // TEST_INCLUDE_SEGMENT_GROUP_EXISTS
        nml2 = getValidDoc();
        nml2.getCell().get(0).getMorphology().getSegmentGroup().get(2).getInclude().get(0).setSegmentGroup("vvv");
        nmlv.validateWithTests(nml2);
        assertTrue(nmlv.getValidity().contains(nmlv.TEST_INCLUDE_SEGMENT_GROUP_EXISTS.description));
    }

    @Test
    public void testGetStandaloneAndAdd() throws Exception {
        NeuroMLDocument nml2 = new NeuroMLDocument();
        nml2.setId("CopiedNML2Doc");

        NeuroMLDocument nml2_old = getValidDoc();
        System.out.println("---- Creating: "+nml2.getId());

        LinkedHashMap<String, Standalone> els = getAllStandaloneElements(nml2_old);
        for (String el : els.keySet()) {
            Standalone stand = els.get(el);
            System.out.println("Adding " + stand + " to new doc...");
            NeuroMLConverter.addElementToDocument(nml2, stand);
        }
        
        String newFilename = nml2.getId() + ".xml";
        File f = neuroml2ToXml(nml2, newFilename, true);
        System.out.println("---- Created: "+f.getAbsolutePath());

    }

    /*
     TODO: move to utils class
     */
    public Element getPropertyElement(String tag, String value) {

        Element propElement = doc.createElement("property");

        propElement.setAttribute("tag", tag);
        propElement.setAttribute("value", value);

        return propElement;
    }

    @Test
    public void testNetworkSave() throws Exception {
        NeuroMLDocument nml2 = new NeuroMLDocument();
        nml2.setId("InstanceBasedNet");

        IafCell iaf = new IafCell();
        iaf.setId("iaf0");
        iaf.setLeakReversal("-60mV");
        iaf.setThresh("-55mV");
        iaf.setReset("-65mV");
        iaf.setC("1.0nF");
        iaf.setLeakConductance("0.05uS");
        nml2.getIafCell().add(iaf);

        Network net = new Network();
        net.setId("Net1");
        nml2.getNetwork().add(net);

        Population pop = new Population();
        pop.setId("pop1");
        pop.setType(PopulationTypes.POPULATION_LIST);
        
        int popSize = 5;
        pop.setSize(popSize);
        
        Annotation annot = new Annotation();
        pop.setAnnotation(annot);

        Element propElement = getPropertyElement("color", "0 1 0");
        annot.getAny().add(propElement);

        pop.setComponent(iaf.getId());

        net.getPopulation().add(pop);

        //<expOneSynapse id="syn1" gbase="5nS" erev="0mV" tauDecay="3ms" />
        ExpOneSynapse e1 = new ExpOneSynapse();
        e1.setId("syn1");
        e1.setGbase("5nS");
        e1.setErev("0mV");
        e1.setTauDecay("3ms");

        nml2.getExpOneSynapse().add(e1);

        float maxX = 100;
        float maxY = 100;
        float maxZ = 100;

        for (int i = 0; i < popSize; i++) {
            Instance instance = new Instance();
            Location loc = new Location();
            instance.setLocation(loc);
            loc.setX((float) Math.random() * maxX);
            loc.setY((float) Math.random() * maxY);
            loc.setZ((float) Math.random() * maxZ);
            pop.getInstance().add(instance);

            PulseGenerator pg = new PulseGenerator();
            pg.setId("pulseGen" + i);
            pg.setDelay("100ms");
            pg.setDuration("800ms");
            pg.setAmplitude((float) (0.5 * Math.random()) + "nA");
            nml2.getPulseGenerator().add(pg);

            ExplicitInput ei = new ExplicitInput();
            ei.setTarget(pop.getId() + "[" + i + "]");
            ei.setInput(pg.getId());
            net.getExplicitInput().add(ei);
        }

        float probConn = 0.5f;

        for (int pre = 0; pre < popSize; pre++) {

            for (int post = 0; post < popSize; post++) {
                if (pre != post) {
                    if (Math.random() < probConn) {
                        //<synapticConnection from="iafCells[0]" to="iafCells[1]" synapse="syn1"/>
                        SynapticConnection sc = new SynapticConnection();
                        sc.setFrom(pop.getId() + "[" + pre + "]");
                        sc.setTo(pop.getId() + "[" + post + "]");
                        sc.setSynapse(e1.getId());

                        net.getSynapticConnection().add(sc);
                    }
                }
            }

        }

        neuroml2ToXml(nml2, nml2.getId() + ".xml", true);

    }

    public void testVersions() throws IOException {
        System.out.println("Running a test on version usage, making all references to versions are: v" + NeuroMLElements.ORG_NEUROML_MODEL_VERSION + "...");

        String jnmlPom = readStringFromFile(new File("pom.xml"));

        assert (jnmlPom.contains("<version>" + NeuroMLElements.ORG_NEUROML_MODEL_VERSION + "</version>"));

    }

    private static String readStringFromFile(File f) throws IOException {
        String sdat = "null";
        if (f != null) {
            InputStream ins = new FileInputStream(f);

            InputStreamReader insr = new InputStreamReader(ins);
            BufferedReader fr = new BufferedReader(insr);

            StringBuilder sb = new StringBuilder();
            while (fr.ready()) {
                sb.append(fr.readLine());
                sb.append("\n");
            }
            fr.close();
            sdat = sb.toString();

        }
        return sdat;
    }

    private File neuroml2ToXml(NeuroMLDocument nml2, String name, boolean validate) throws Exception {
        File tempdir = new File(tempdirname);
        if (!tempdir.exists()) {
            tempdir.mkdir();
        }

        NeuroMLConverter conv = new NeuroMLConverter();
        String tempFilename = tempdirname + File.separator + name;
        File tempFile = conv.neuroml2ToXml(nml2, tempFilename);
        System.out.println("Saved to: " + tempFile.getAbsolutePath());
        if (!tempFile.exists()) {
            throw new Exception("Not successfully saved to: " + tempFilename);
        }

        if (validate) {
            validateFile(tempFile);
        }
        return tempFile;

    }


    
    @Test
    public void testLocalExamples() throws Exception {
        System.out.println("---  Testing local examples... ");
        File exdir = new File(exampledirname);
        NeuroMLConverter neuromlConverter = new NeuroMLConverter();
        for (File f : exdir.listFiles()) {
            if (f.getName().endsWith(".nml")) {
                validateFile(f);
                String url = f.getAbsolutePath();

                System.out.println("      Trying to load: " + url);
                URL newURL = f.toURI().toURL();

                NeuroMLDocument neuroml = neuromlConverter.urlToNeuroML(newURL);
                //neuroml.
                System.out.println("      Success: " + neuroml.getId());

            }
        }

    }

    private void validateFile(File f) throws Exception {

        System.out.println("---  Validating: " + f);
        NeuroML2Validator nmlv = new NeuroML2Validator();
        nmlv.validateWithTests(f);
        System.out.println("       " + nmlv.getValidity());
        System.out.println("       " + nmlv.getWarnings());
        assertTrue("---  Validating: " + f, nmlv.isValid());
        assertTrue("---  Validating: " + f, !nmlv.hasWarnings());
    }

}
