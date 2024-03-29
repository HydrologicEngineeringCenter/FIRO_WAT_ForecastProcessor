import HEC.WAT.ForecastProcessor.DataLocations.SingleComputableDataLocation;
import hec.ensemble.stats.*;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

class SingleComputableDataLocationTest {
    SingleComputable twostep = new TwoStepComputable(new MaxComputable(),new MeanComputable(), true);
    String dssPath = "SomeDSSPAth";
    String dssFile = "SomeDSSFilePath";
    SingleComputableDataLocation cdl = new SingleComputableDataLocation(dssFile,dssPath,twostep);
    Element parent = new Element("OutputDataLocations");
    @Test
    void toXML() {
        cdl.toXML(parent);
    }
    @Test
    void fromXML() {
       Element cdlElement = cdl.toXML(parent);
       SingleComputableDataLocation newCdl = new SingleComputableDataLocation();
       newCdl.fromXML(cdlElement);
       System.out.println("done");
}}