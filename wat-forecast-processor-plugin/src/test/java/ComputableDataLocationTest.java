import HEC.WAT.ForecastProcessor.DataLocations.ComputableDataLocation;
import hec.ensemble.stats.*;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

class ComputableDataLocationTest {
    Computable maxAccumDuration = new MaxAccumDuration(3);
    String dssPath = "SomeDSSPAth";
    String dssFile = "SomeDSSFilePath";
    ComputableDataLocation cdl = new ComputableDataLocation(dssPath,dssFile,maxAccumDuration,true);
    Element parent = new Element("OutputDataLocations");

    @Test
    void toXML() {
        cdl.toXML(parent);
    }
    @Test
    void fromXML() {
       Element cdlElement = cdl.toXML(parent);
       ComputableDataLocation newCdl = new ComputableDataLocation();
       newCdl.fromXML(cdlElement);
       System.out.println("done");
}}