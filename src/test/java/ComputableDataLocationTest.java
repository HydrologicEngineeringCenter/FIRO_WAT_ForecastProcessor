import hec.ensemble.stats.*;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ComputableDataLocationTest {
    Computable maxAccumDuration = new MaxAccumDuration(3);
    ComputableDataLocation cdl = new ComputableDataLocation(new ModelAlternative(),"Popeye","Flow",maxAccumDuration,false);
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