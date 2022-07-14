import hec.ensemble.stats.Computable;
import hec.ensemble.stats.MultiComputable;
import hec.ensemble.stats.Serializer;
import hec2.model.DataLocation;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;

import java.util.List;

public class MultiComputableDataLocation extends DataLocation {
    private MultiComputable computableThing;
    private boolean acrossTime = true;

    public MultiComputable getComputableThing() {
        return computableThing;
    }
    public boolean isAcrossTime() {
        return acrossTime;
    }

    public MultiComputableDataLocation(ModelAlternative modelAlt, String name, String parameter, MultiComputable computableThing, boolean computeAcrossTime) {
        super(modelAlt, name, parameter);
        this.computableThing = computableThing;
        this.acrossTime = computeAcrossTime;
    }
    public MultiComputableDataLocation() {
        super();
    }
    @Override
    public Element toXML(Element parent){
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
