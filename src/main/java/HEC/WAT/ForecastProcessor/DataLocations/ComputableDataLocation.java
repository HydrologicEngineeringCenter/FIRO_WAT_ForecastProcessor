package HEC.WAT.ForecastProcessor.DataLocations;

import hec.ensemble.stats.Computable;
import hec.ensemble.stats.Serializer;
import hec2.model.DataLocation;
import hec2.model.DssDataLocation;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;

public class ComputableDataLocation extends DssDataLocation {
    private Computable computableThing;
    private boolean acrossTime = true;

    public Computable getComputableThing() {
        return computableThing;
    }
    public boolean isAcrossTime() {
        return acrossTime;
    }

    public ComputableDataLocation(String dssPath,String dssFile, Computable computableThing, boolean computeAcrossTime) {
        super(dssFile,dssPath);
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
            if(childElement.getName().equals("ModelAlternative")){
                continue;
            }
            computableThing = Serializer.fromXML(childElement);
            return true;
        }
        return false;
    }
}

