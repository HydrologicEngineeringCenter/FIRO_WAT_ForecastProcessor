import hec.stats.*;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComputableDataLocationTest {
    Computable maxAccumDuration = new MaxAccumDuration(3);
    Statistics[] stats = new Statistics[]{Statistics.MAX, Statistics.MEAN, Statistics.MIN};
    MultiComputable multiStatComputable = new MultiStatComputable(stats);
    SingleComputable twostep = new TwoStepComputable(new MaxComputable(),new MeanComputable(), true);

    ComputableDataLocation cdl = new ComputableDataLocation(new ModelAlternative(),"Popeye","Flow",maxAccumDuration);
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