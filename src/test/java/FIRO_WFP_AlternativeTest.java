import com.rma.io.RmaFile;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rma.util.RMAFile;

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

    }

    @Test
    void saveData() {
        _alt.getInputDataLocations();
        _alt.getOutputDataLocations();
        RmaFile rf = new RmaFile("src/test/resources/savedata.test");
        _alt.saveData(rf);
    }

    @Test
    void loadDocument() throws JDOMException, IOException {
        FIRO_WFP_Alternative tmpAlt = new FIRO_WFP_Alternative();
        SAXBuilder sax = new SAXBuilder();
        // XML is a local file
        Document doc = sax.build(new File("src/test/resources/savedata.test"));
        tmpAlt.loadDocument(doc);
    }


    @AfterEach
    void tearDown() {

    }

}