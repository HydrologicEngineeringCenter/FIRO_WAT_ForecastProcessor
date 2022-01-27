import hec.stats.Statistics;
import hec2.model.DataLocation;
import hec2.plugin.model.ModelAlternative;
import org.jdom.Element;

public class MetricOutputDataLocation extends DataLocation {
    private Statistics[] _metricList;
    public MetricOutputDataLocation(ModelAlternative modelAlt, String name, String parameter, Statistics[] metricList){
        super(modelAlt,name,parameter);
        _metricList = metricList;
    }
    @Override
    public org.jdom.Element toXML(Element parent){
        Element baseEl = super.toXML(parent);
        baseEl.setAttribute("Metrics", metricStatisticsToString());
        return baseEl;
    }
    @Override
    public boolean fromXML(Element myElement){
        String stringMetrics = myElement.getAttributeValue("Metrics");
        if(stringMetrics != null){
            MetricStringsToStatistics(stringMetrics);
        }
        super.fromXML(myElement);
        return true;
    }
    private String metricStatisticsToString(){
        String s = "";
        for (int i=0;i<_metricList.length;i++) {
            s += _metricList[i] + ",";
        }
        s = s.substring(0,s.length()-1);
        return s;
    }
    private void MetricStringsToStatistics(String stringStats){
        String[] splitString = stringStats.split(",");
        Statistics[] _metricList = new Statistics[splitString.length];
        for (int i = 0; i< splitString.length; i++){
            _metricList[i] = Statistics.valueOf(splitString[i]);
        }
    }
    public Statistics[] getStats() {
        return _metricList;
    }
}
