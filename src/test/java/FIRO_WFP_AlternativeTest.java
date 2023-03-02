import HEC.WAT.ForecastProcessor.FIRO_WFP_Alternative;
import com.rma.io.RmaFile;
import hec.data.location.Alternative;
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
        _alt.getInputDataLocations();
        _alt.getOutputDataLocations();
        RmaFile rf = new RmaFile("src/test/resources/savedata.xml");
        assertTrue(_alt.saveData(rf));
    }

    @Test
    void loadDocument() throws JDOMException, IOException {
        FIRO_WFP_Alternative tmpAlt = new FIRO_WFP_Alternative();
        SAXBuilder sax = new SAXBuilder();
        Document doc = sax.build(new File("src/test/resources/savedata.xml"));
        assertTrue(tmpAlt.loadDocument(doc));
        int expectedInputs = 1;
        int expectedOutputs = 24;
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

    @Test
    void buildPathToDBFile(){
        //arrange
        String runsDir = "D:\\FIRO\\Evan's Test Data\\RTestTWM";
        int real = 1;
        int life = 1;
        int event = 1;
        String DatabaseName = "ensembles.db";
        String expected = "D:\\FIRO\\Evan's Test Data\\RTestTWM\\realization 1\\lifecycle 1\\event 1\\ensembles.db";
        //act
        String actual = FIRO_WFP_Alternative.buildPathToDBFile(runsDir,real,life,event,DatabaseName);
        //assert
        assertEquals(expected,actual);
    }

    @AfterEach
    void tearDown() {

    }

}