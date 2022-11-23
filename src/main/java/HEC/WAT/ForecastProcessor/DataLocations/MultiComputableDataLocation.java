package HEC.WAT.ForecastProcessor.DataLocations;

import hec.ensemble.stats.Computable;
import hec.ensemble.stats.MultiComputable;
import hec.ensemble.stats.Serializer;
import hec2.model.DataLocation;
import hec2.model.DssDataLocation;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;

public class MultiComputableDataLocation extends DssDataLocation {
    private MultiComputable computableThing;
    private boolean acrossTime = true;

    public MultiComputable getComputableThing() {
        return computableThing;
    }
    public boolean isAcrossTime() {
        return acrossTime;
    }

    public MultiComputableDataLocation(String dssPath,String dssFile, MultiComputable computableThing, boolean computeAcrossTime) {
        super(dssFile,dssPath);
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
    public Element toXML(Element parent, Path root){
        Element baseEl = super.toXML(parent, root);
        baseEl.addContent(Serializer.toXML(computableThing));
        return baseEl;
    }
    @Override
    public boolean fromXML(Element myElement) {
        super.fromXML(myElement);
        List<Object> childs = myElement.getChildren();
        for (Object child : childs) {
            Element childElement = (Element) child;
            if(childElement.getName().equals("hec.ensemble.stats")){
                computableThing = Serializer.fromXML(childElement);
                return true;
            }
        }
        return false;
    }
}
