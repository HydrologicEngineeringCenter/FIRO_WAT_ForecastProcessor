/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.rma.io.RmaFile;
import com.rma.model.Computable;
import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;
import hec.data.Parameter;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.TimeSeriesIdentifier;
import hec.model.OutputVariable;
import hec.stats.MaxAvgDuration;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import hec2.wat.client.WatFrame;
import hec2.wat.model.tracking.OutputVariableImpl;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author WatPowerUser
 */
public class FIRO_WFP_Alternative extends SelfContainedPluginAlt{
    private String _pluginVersion;
    private static final String DocumentRoot = "FIRO_WFP_Alternative";
    private static final String OutputVariableElement = "OutputVariables";
    private static final String AlternativeNameAttribute = "Name";
    private static final String AlternativeDescriptionAttribute = "Desc";
    private ComputeOptions _computeOptions;
    public FIRO_WFP_Alternative(){
        super();
    }
    public FIRO_WFP_Alternative(String name){

        this();
        setName(name);
    }
    @Override
    protected boolean loadDocument(org.jdom.Document dcmnt) {
        if(dcmnt!=null){
            org.jdom.Element ele = dcmnt.getRootElement();
            if(ele==null){
                System.out.println("No root element on the provided XML document.");
                return false;
            }
            if(ele.getName().equals(DocumentRoot)){
                setName(ele.getAttributeValue(AlternativeNameAttribute));
                setDescription(ele.getAttributeValue(AlternativeDescriptionAttribute));
            }else{
                System.out.println("XML document root was imporoperly named.");
                return false;
            }
            setModified(false);
            return true;
        }else{
            System.out.println("XML document was null.");
            return false;
        }
    }
    public void setComputeOptions(ComputeOptions opts){
        _computeOptions = opts;
    }
    @Override
    public boolean isComputable() {
        return true;
    }
    @Override
    public boolean compute() {
        String dssName = _computeOptions.getDssFilename();
        String databaseName = dssName.substring(0,dssName.length() - 3) + "db";
        try {
            TimeSeriesDatabase database = new JdbcTimeSeriesDatabase(databaseName, JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_NO_UPDATE);
            TimeSeriesIdentifier timeSeriesIdentifier = new TimeSeriesIdentifier("Coyote.fake_forecast", "flow");
            EnsembleTimeSeries ensembleTimeSeries = database.getEnsembleTimeSeries(timeSeriesIdentifier);
            List<ZonedDateTime> issueDates = ensembleTimeSeries.getIssueDates();
            Ensemble ensemble = database.getEnsemble(timeSeriesIdentifier,issueDates.get(0));
            WatFrame fr = hec2.wat.WAT.getWatFrame();
            fr.addMessage("We got the ensemble data!");

            /*MaxAvgDuration test = new MaxAvgDuration(5);
            float[] output = ensemble.iterateForTracesAcrossTime(test);
            WatFrame fr = hec2.wat.WAT.getWatFrame();
            fr.addMessage("This is an output from the Across Time Calculation" + output[0] );

            MaxAvgDuration test2 = new MaxAvgDuration((2));
            float[] output2 = ensemble.iterateForTimeAcrossTraces(test2);
            fr.addMessage("This is an output from the Across Traces Calculation " + output2[0]);*/


        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
    @Override
    public boolean cancelCompute() {
        return false;
    }
    @Override
    public String getLogFile() {
        return null;
    }
    @Override
    public int getModelCount() {
        return 1;
    }
    @Override
    public boolean saveData(RmaFile file){
        if(file!=null){
            //used to be sElement
            Element root = new Element(DocumentRoot);
            root.setAttribute(AlternativeNameAttribute,getName());
            root.setAttribute(AlternativeDescriptionAttribute,getDescription());
            Document doc = new Document(root);
            return writeXMLFile(doc,file);
        }
        return false;
    }
    public List<OutputVariable> getOutputVariables(){
        OutputVariableImpl oimpl = new OutputVariableImpl();
        oimpl.setName("RAS Compute Failures");
        oimpl.setDescription("Finds RAS Compute Failures");
        oimpl.setParamId(Parameter.PARAMID_COUNT);
        List<OutputVariable> ret = new ArrayList<>();
            ret.add(oimpl);
        return ret;
    }
    public boolean hasOutputVariables(){
        return true;
    }
    boolean computeOutputVariables(List<OutputVariable> list) { return true; }

}
