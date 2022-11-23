package HEC.WAT.ForecastProcessor.DataLocations;

import hec.ensemble.stats.Serializer;
import hec.ensemble.stats.SingleComputable;
import hec2.model.DataLocation;
import hec2.model.DssDataLocation;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;

public class SingleComputableDataLocation extends DssDataLocation {
    private SingleComputable computableThing;
    public SingleComputable getComputableThing() {
        return computableThing;
    }

    public SingleComputableDataLocation(String dssPath,String dssFile, SingleComputable computableThing) {
        super(dssFile,dssPath);
        this.computableThing = computableThing;
    }
    public SingleComputableDataLocation() {
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
            if(childElement.getName().equals("ModelAlternative")){
                continue;
            }
            try{
                computableThing = Serializer.fromXML(childElement);
                return true;
            }
            catch (Exception ex){
                System.out.println(ex);
            }

        }
        return false;
    }

    @Override
    public boolean fromXML(Element myElement, Path root) {
        super.fromXML(myElement,root);
        List<Object> childs = myElement.getChildren();
        for (Object child : childs) {
            Element childElement = (Element) child;
            if(childElement.getName().equals("ModelAlternative")){
                continue;
            }
            try{
                computableThing = Serializer.fromXML(childElement);
                return true;
            }
            catch (Exception ex){
                System.out.println(ex);
            }

        }
        return false;
    }

}
