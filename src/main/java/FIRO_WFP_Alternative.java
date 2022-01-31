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
    //region Fields
    private String _pluginVersion;
    List<DataLocation> _inputDataLocations;
    List<MetricOutputDataLocation> _outputDataLocations ;
    private static final String DocumentRoot = "FIRO_WFP_Alternative";
    private static final String OutputVariableElement = "OutputVariables";
    private static final String AlternativeNameAttribute = "Name";
    private static final String AlternativeDescriptionAttribute = "Desc";
    private ComputeOptions _computeOptions;
    private List<OutputVariable> _outputVariables;
    //endregion
    //region Constructors
    public FIRO_WFP_Alternative(){
        super();
    }
    public FIRO_WFP_Alternative(String name){
        this();
        setName(name);
    }
    //endregion
    //region Getters and Setters
    @Override
    public int getModelCount() {
        return 1;
    }
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
        List<DataLocation> outputAsDataLoc = new ArrayList<DataLocation>();
        for(DataLocation dl : _outputDataLocations){
            outputAsDataLoc.add(dl);
        }
        return outputAsDataLoc;
    }
    public List<OutputVariable> getOutputVariables(){
        return _outputVariables;
    }
    @Override
    public String getLogFile() {
        return null;
    }
    public void setComputeOptions(ComputeOptions opts){
        _computeOptions = opts;
    }
    public boolean hasOutputVariables(){
        if (_outputVariables == null || _outputVariables.size() == 0){
            return false;
        }
        return true;
    }
    //endregion
    //region Ignored Boilerplate
    @Override
    public boolean isComputable() {
        return true;
    }
    boolean computeOutputVariables(List<OutputVariable> list) { return true; }
    @Override
    public boolean cancelCompute() {
        return false;
    }
    //endregion
    @Override
    public boolean compute() {
        String dssName;
        dssName = _computeOptions.getDssFilename();
        String databaseName = dssName.substring(0,dssName.length() - 3) + "db";
        try {
            SqliteDatabase database = new SqliteDatabase(databaseName, SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_NO_UPDATE);
            for (DataLocation inputDataLocation : _inputDataLocations) {
                hec.RecordIdentifier timeSeriesIdentifier = new hec.RecordIdentifier(inputDataLocation.getName(), inputDataLocation.getParameter());
                EnsembleTimeSeries ensembleTimeSeries = database.getEnsembleTimeSeries(timeSeriesIdentifier);
                for (MetricOutputDataLocation outDataLocation : _outputDataLocations) {
                    if (outDataLocation.getName().equals(inputDataLocation.getName())) {
                        MultiStatComputable msc = new MultiStatComputable(outDataLocation.getStats());
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
                saveMetricOutputDataLocations(root, _outputDataLocations);
            }
            Document doc = new Document(root);
            return writeXMLFile(doc,file);
        }
        return false;
    }

     void loadMetricOutputDataLocations(Element root, List<MetricOutputDataLocation> outputDataLocations) {
         for (Object child : root.getChildren()) {
             Element ele = (Element)child;
             if(ele.getName().equals("OutputDataLocations")){
                 for ( Object Grandchild: ele.getChildren() ){
                     MetricOutputDataLocation outDataLocation = new MetricOutputDataLocation();
                     Element outputEle = (Element)Grandchild;
                     outDataLocation.fromXML(outputEle);
                     outputDataLocations.add(outDataLocation);
                 }
             }
         }
    }

    void saveMetricOutputDataLocations(Element root, List<MetricOutputDataLocation> outputDataLocations) {
        root.addContent(new Element("OutputDataLocations"));
        for( MetricOutputDataLocation outputDataLocation : outputDataLocations){
            outputDataLocation.toXML(root.getChild("OutputDataLocations"));
        }
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
            loadMetricOutputDataLocations(ele, _outputDataLocations);

            setModified(false);
            return true;
        }else{
            System.out.println("XML document was null.");
            return false;
        }
    }

//These guys are just here for testing. We wouldn't really want default input and output data locations
    public List<DataLocation> defaultInputDataLocations() {
        List<DataLocation> dlList = new ArrayList<>();
        //create datalocations for each location of interest, so that it can be linked to output from other models.
        DataLocation CoyoteEnsemble = new DataLocation(this.getModelAlt(),"Coyote.fake_forecast","flow");
        dlList.add(CoyoteEnsemble);
        return dlList;
    }
    public List<MetricOutputDataLocation> defaultOutputDataLocations() {
        List<MetricOutputDataLocation> dlList = new ArrayList<>();
        //create datalocations for each location of interest, so that it can be linked to output from other models.
        Statistics[] metricArray = new Statistics[1];
        metricArray[0] =Statistics.MAX;
        MetricOutputDataLocation CoyoteEnsemble = new MetricOutputDataLocation(this.getModelAlt(),"Coyote.fake_forecast","flow",metricArray);
        dlList.add(CoyoteEnsemble);

        return dlList;
    }


}
