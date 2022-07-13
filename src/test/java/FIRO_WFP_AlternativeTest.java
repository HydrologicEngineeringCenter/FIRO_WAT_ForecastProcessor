import com.rma.io.RmaFile;
import hec.ensemble.stats.SingleComputable;
import hec.ensemble.stats.TwoStepComputable;
import hec2.wat.model.ComputeOptions;
import org.jdom.Document;
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
        int actualInputs = tmpAlt._inputDataLocations.size();
        int actualOutputs = tmpAlt._outputDataLocations.size();
        assertEquals(expectedInputs,actualInputs);
        assertEquals(expectedOutputs,actualOutputs);
    }

    @Test
    void compute() throws JDOMException, IOException {
        SAXBuilder sax = new SAXBuilder();
        Document doc = sax.build(new File("src/test/resources/savedata.xml"));
        _alt.loadDocument(doc);
        assertTrue(_alt.compute());
    }

    @AfterEach
    void tearDown() {

    }

}