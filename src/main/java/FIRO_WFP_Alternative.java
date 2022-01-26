/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.rma.io.RmaFile;
import hec.SqliteDatabase;
import hec.ensemble.EnsembleTimeSeries;
import hec.metrics.MetricCollectionTimeSeries;
import hec.model.OutputVariable;
import hec.model.RunTimeWindow;
import hec.stats.MultiStatComputable;
import hec.stats.Statistics;
import hec2.model.DataLocation;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import org.jdom.Document;
import org.jdom.Element;
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
    private List<OutputVariable> _outputVariables;
    public FIRO_WFP_Alternative(){
        super();
    }
    public FIRO_WFP_Alternative(String name){
        this();
        setName(name);
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
        String dssName;
        dssName = _computeOptions.getDssFilename();
        String databaseName = dssName.substring(0,dssName.length() - 3) + "db";
        hec2.wat.model.ComputeOptions wco;
        if(_computeOptions instanceof hec2.wat.model.ComputeOptions) {
            wco = (hec2.wat.model.ComputeOptions) _computeOptions;
            RunTimeWindow rtw = wco.getEventList().get(wco.getCurrentEventNumber());
            return computeForTimeWindow(rtw);
        } else{
            return false;
        }
    }

    private boolean computeForTimeWindow(RunTimeWindow rtw){
        String dssName;
        dssName = _computeOptions.getDssFilename();
        String databaseName = dssName.substring(0,dssName.length() - 3) + "db";

        try {
            SqliteDatabase database = new SqliteDatabase(databaseName, SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_NO_UPDATE);
            for(int k=0; k<_inputDataLocations.size(); k++){
                hec.RecordIdentifier timeSeriesIdentifier = new hec.RecordIdentifier(_inputDataLocations.get(k).getName(), _inputDataLocations.get(k).getParameter());
                EnsembleTimeSeries ensembleTimeSeries = database.getEnsembleTimeSeries(timeSeriesIdentifier);
                for (DataLocation outDataLocation:_outputDataLocations)
                {
                    MetricOutputDataLocation metricOutDataLocation = (MetricOutputDataLocation) outDataLocation;
                    if (outDataLocation.getName().equals(_inputDataLocations.get(k).getName())){
                        MultiStatComputable msc = new MultiStatComputable(metricOutDataLocation.getStats());
                        MetricCollectionTimeSeries mcts = ensembleTimeSeries.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(msc);
                        database.write(mcts);
                    }
                }
            }
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
            Element root = new Element(DocumentRoot);
            root.setAttribute("AlternativeNameAttribute",getName());
            root.setAttribute("AlternativeDescriptionAttribute",getDescription());
            root.setAttribute("AlternativeFilenameAttribute",file.getAbsolutePath());
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
    @Override
    protected boolean loadDocument(org.jdom.Document dcmnt) {
        if(dcmnt!=null){
            org.jdom.Element ele = dcmnt.getRootElement();
            if(ele==null){
                System.out.println("No root element on the provided XML document.");
                return false;
            }
            if(ele.getName().equals(DocumentRoot)){
                setName(ele.getAttributeValue("AlternativeNameAttribute"));
                setDescription(ele.getAttributeValue("AlternativeDescriptionAttribute"));
                String val = ele.getAttributeValue("AlternativeFilenameAttribute");
                RmaFile file = new RmaFile(val);
                setFile(file);
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
    public List<OutputVariable> getOutputVariables(){
        return _outputVariables;
    }
    public boolean hasOutputVariables(){
        if (_outputVariables == null || _outputVariables.size() == 0){
            return false;
        }
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
    public List<DataLocation> getOutputDataLocations(){
        //construct input data locations.
        if(_outputDataLocations== null || _outputDataLocations.isEmpty()){
            _outputDataLocations = defaultOutputDataLocations();
        }
        return _outputDataLocations;
    }

//These guys are just here for testing. We wouldn't really want default input and output data locations
    public List<DataLocation> defaultInputDataLocations() {
        List<DataLocation> dlList = new ArrayList<>();
        //create datalocations for each location of interest, so that it can be linked to output from other models.
        DataLocation KanatockEnsemble = new DataLocation(this.getModelAlt(),"Kanatook","Ensemble");
        dlList.add(KanatockEnsemble);
        return dlList;
    }
    public List<DataLocation> defaultOutputDataLocations() {
        List<DataLocation> dlList = new ArrayList<>();
        //create datalocations for each location of interest, so that it can be linked to output from other models.
        Statistics[] metricArray = new Statistics[1];
        metricArray[0] =Statistics.MAX;
        DataLocation KanatockEnsemble = new MetricOutputDataLocation(this.getModelAlt(),"Kanatook","Ensemble",metricArray);
        dlList.add(KanatockEnsemble);

        return dlList;
    }


}
