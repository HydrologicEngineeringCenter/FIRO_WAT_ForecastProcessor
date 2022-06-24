import hec.stats.Computable;
import hec.stats.Serializer;
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
            String elementName = childElement.getName();
            Class<?> c;
            try {
                c = Class.forName(elementName);
                Class<?>[] interfaces = c.getInterfaces();
                for (Class<?> inters : interfaces) {
                    if (inters.getName().equals("hec.stats.Computable")) {
                        computableThing = Serializer.fromXML(childElement);
                    }
                }
            } catch (Exception ex) {
                System.out.println("this child wasn't a computable");
            }
        }
        return true;
    }
}

