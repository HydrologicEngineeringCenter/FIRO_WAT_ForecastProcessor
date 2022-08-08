import HEC.WAT.ForecastProcessor.FIRO_WFP_Alternative;
import com.rma.io.RmaFile;
import hec2.model.DataLocation;
import hec2.wat.model.ComputeOptions;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FIRO_WFP_AlternativeTest {
    FIRO_WFP_Alternative _alt;

    @BeforeEach
    void setUp() {
        RmaFile rf = new RmaFile("src/test/resources/savedata.test");
        _alt = new FIRO_WFP_Alternative("myName");
        _alt.setDescription("myDescription");
        _alt.setFile(rf);
        hec2.wat.model.ComputeOptions opts = new ComputeOptions();
        opts.setDssFilename("src/test/resources/ensembles.dss");
        _alt.setComputeOptions(opts);
    }

    @Test
    void saveData() {
//        SAve data does not work because the override toXML on the unique Data locations is not working. They end up being serialized like a typical data location, leaving out data.
//        saving has been disabled until a solution is found. There is no UI element to modify the alternative yet anyway. Once that exists, the rest will need to follow.
        _alt.getInputDataLocations();
        _alt.getOutputDataLocations();
        RmaFile rf = new RmaFile("src/test/resources/savedata.xml");
        assertTrue(_alt.saveData(rf));
    }

    @Test
    void loadDocument() throws JDOMException, IOException {
        FIRO_WFP_Alternative tmpAlt = new FIRO_WFP_Alternative();
        SAXBuilder sax = new SAXBuilder();
        // XML is a local file
        Document doc = sax.build(new File("src/test/resources/savedata.xml"));
        assertTrue(tmpAlt.loadDocument(doc));
        int expectedInputs = 1;
        int expectedOutputs = 8;
        int actualInputs = tmpAlt.getInputDataLocations().size();
        int actualOutputs = tmpAlt.getOutputDataLocations().size();
        assertEquals(expectedInputs,actualInputs);
        assertEquals(expectedOutputs,actualOutputs);
    }

    @Test
    void compute() throws JDOMException, IOException {
        SAXBuilder sax = new SAXBuilder();
        Document doc = sax.build(new File("src/test/resources/PradoMetricsBackup.xml"));
        _alt.loadDocument(doc);
        assertTrue(_alt.compute());
    }

    @AfterEach
    void tearDown() {

    }

}