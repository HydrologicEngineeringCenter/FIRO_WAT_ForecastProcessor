/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.rma.io.RmaFile;
/*import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;*/
import hec.data.Parameter;
/*import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.TimeSeriesIdentifier;*/
import hec.model.OutputVariable;
import hec2.model.DataLocation;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import hec2.wat.client.WatFrame;
import hec2.wat.model.tracking.OutputVariableImpl;
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
    List<DataLocation> _inputDataLocations;
    List<DataLocation> _outputDataLocations ;
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

            if(_inputDataLocations ==null){
                _inputDataLocations = new ArrayList<>();
            }
            _inputDataLocations.clear();
            loadDataLocations(ele, _inputDataLocations);

            if(_outputDataLocations ==null){
                _outputDataLocations = new ArrayList<>();
            }
            _outputDataLocations.clear();
            loadOutputDataLocations(ele, _outputDataLocations);

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
        return false;
/*        String dssName = _computeOptions.getDssFilename();
        String databaseName = dssName.substring(0,dssName.length() - 3) + "db";
        try {
            TimeSeriesDatabase database = new JdbcTimeSeriesDatabase(databaseName, JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_NO_UPDATE);
            TimeSeriesIdentifier timeSeriesIdentifier = new TimeSeriesIdentifier("Coyote.fake_forecast", "flow");
            EnsembleTimeSeries ensembleTimeSeries = database.getEnsembleTimeSeries(timeSeriesIdentifier);
            List<ZonedDateTime> issueDates = ensembleTimeSeries.getIssueDates();
            Ensemble ensemble = database.getEnsemble(timeSeriesIdentifier,issueDates.get(0));
            WatFrame fr = hec2.wat.WAT.getWatFrame();
            fr.addMessage("We got the ensemble data!");

            hec.stats.Computable test = new MeanComputable();
            float[] output = ensemble.iterateForTracesAcrossTime(test);
            fr.addMessage("This is the mean across time for the first trace: " + output[0] );

            hec.stats.Computable test2 = new MeanComputable();
            float[] output2 = ensemble.iterateForTimeAcrossTraces(test2);
            fr.addMessage("This is mean across traces for the first timestep: " + output2[0]);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    */
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
            if(_inputDataLocations!=null) {
                saveDataLocations(root, _inputDataLocations);
            }
            if(_outputDataLocations!=null) {
                saveOutputDataLocations(root, _outputDataLocations);
            }
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

    public List<DataLocation> getInputDataLocations(){
        //construct input data locations.
        if(_inputDataLocations==null ||_inputDataLocations.isEmpty()){
            _inputDataLocations = defaultInputDataLocations();
        }
            return _inputDataLocations;
    }

    public List<DataLocation> defaultInputDataLocations() {
        List<DataLocation> dlList = new ArrayList<>();
        //create datalocations for each location of interest, so that it can be linked to output from other models.

        DataLocation KanatockEnsemble = new DataLocation(this.getModelAlt(),"Kanatook","Ensemble");
        dlList.add(KanatockEnsemble);


        return dlList;
    }

    public List<DataLocation> getOutputDataLocations(){
        //construct input data locations.
        if(_outputDataLocations== null || _outputDataLocations.isEmpty()){
            _outputDataLocations = defaultOutputDataLocations();
        }
        return _outputDataLocations;
    }

    public List<DataLocation> defaultOutputDataLocations() {
        List<DataLocation> dlList = new ArrayList<>();
        //create datalocations for each location of interest, so that it can be linked to output from other models.

        DataLocation KanatockEnsemble = new DataLocation(this.getModelAlt(),"Kanatook","MaxTS");
        dlList.add(KanatockEnsemble);

        return dlList;
    }


}
