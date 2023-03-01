import HEC.WAT.ForecastProcessor.DataLocations.ComputableDataLocation;
import HEC.WAT.ForecastProcessor.DataLocations.MultiComputableDataLocation;
import hec.ensemble.stats.*;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

class MultiComputableDataLocationTest {
    Statistics[] stats = new Statistics[]{Statistics.MAX, Statistics.AVERAGE, Statistics.MIN};
    MultiComputable multiStatComputable = new MultiStatComputable(stats);
    String dssPath = "SomeDSSPAth";
    String dssFile = "SomeDSSFilePath";

    MultiComputableDataLocation cdl = new MultiComputableDataLocation(dssPath,dssFile,multiStatComputable,false);
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