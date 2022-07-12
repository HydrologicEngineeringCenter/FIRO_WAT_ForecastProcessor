import hec.ensemble.stats.Computable;
import hec.ensemble.stats.Serializer;
import hec.ensemble.stats.SingleComputable;
import hec2.model.DataLocation;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;

import java.util.List;

public class SingleComputableDataLocation extends DataLocation {
    private SingleComputable computableThing;
    public SingleComputable getComputableThing() {
        return computableThing;
    }

    public SingleComputableDataLocation(ModelAlternative modelAlt, String name, String parameter, SingleComputable computableThing) {
        super(modelAlt, name, parameter);
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
    public boolean fromXML(Element myElement) {
        super.fromXML(myElement);
        List<Object> childs = myElement.getChildren();
        for (Object child : childs) {
            Element childElement = (Element) child;
            String elementName = childElement.getName();
            Class<?> c;
            try {
                c = Class.forName(elementName);
                Class<?>[] interfaces = c.getInterfaces();
                for (Class<?> inters : interfaces) {
                    if (inters.getName().equals("hec.ensemble.stats.SingleComputable")) {
                        computableThing = Serializer.fromXML(childElement);
                    }
                }
            } catch (Exception ex) {
                System.out.println("this child wasn't a SingleComputable");
            }
        }
        return true;
    }

}
