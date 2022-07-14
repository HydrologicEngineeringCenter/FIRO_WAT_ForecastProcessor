import hec.ensemble.stats.Computable;
import hec.ensemble.stats.Serializer;
import hec2.model.DataLocation;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;

import java.util.List;

public class ComputableDataLocation extends DataLocation {
    private Computable computableThing;
    private boolean acrossTime = true;

    public Computable getComputableThing() {
        return computableThing;
    }
    public boolean isAcrossTime() {
        return acrossTime;
    }

    public ComputableDataLocation(ModelAlternative modelAlt, String name, String parameter, Computable computableThing, boolean computeAcrossTime) {
        super(modelAlt, name, parameter);
        this.computableThing = computableThing;
        this.acrossTime = computeAcrossTime;
    }

    public ComputableDataLocation() {
        super();
    }

    @Override
    public org.jdom.Element toXML(Element parent) {
        Element baseEl = super.toXML(parent);
        baseEl.addContent(Serializer.toXML(computableThing));
        return baseEl;
    }

    @Override
    public boolean fromXML(Element myElement) {
        super.fromXML(myElement);
        List<Object> childs = myElement.getChildren();
        for (Object child : childs) {
            Element childElement = (Element) child;
            if(childElement.getName().equals("ModelAlternative")){
                continue;
            }
            computableThing = Serializer.fromXML(childElement);
            return true;
        }
        return false;
    }
}

