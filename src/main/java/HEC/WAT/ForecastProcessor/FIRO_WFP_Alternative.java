package HEC.WAT.ForecastProcessor;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import HEC.WAT.ForecastProcessor.DataLocations.ComputableDataLocation;
import HEC.WAT.ForecastProcessor.DataLocations.MultiComputableDataLocation;
import HEC.WAT.ForecastProcessor.DataLocations.SingleComputableDataLocation;
import com.rma.io.RmaFile;
import hec.SqliteDatabase;
import hec.dss.ensemble.DssDatabase;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.*;
import hec.metrics.MetricCollectionTimeSeries;
import hec.model.OutputVariable;
import hec2.model.DataLocation;
import hec2.plugin.model.ComputeOptions;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import org.jdom.Document;
import org.jdom.Element;

import java.io.File;
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
    List<DataLocation> _outputDataLocations ;
    String _timeStep;
    private static final String DocumentRoot = "HEC.WAT.ForecastProcessor.FIRO_WFP_Alternative";
    private static final String OutputVariableElement = "OutputVariables";
    private static final String AlternativeNameAttribute = "Name";
    private static final String AlternativeDescriptionAttribute = "Desc";
    private static final String OutputDataLocationParentElement = "OutputDataLocations";
    private static final String AlternativeFilenameAttribute = "AlternativeFilename";
    private static final String OutputDataLocationsChildElement = "OutputDataLocation";
    private static final String DatabaseName = "ensembles.db";
    private static final String DssDatabaseName = "ensembles.dss";
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
        String databaseName = getInputOutputDatabaseName();
        try {
            SqliteDatabase database = new SqliteDatabase(databaseName, SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);
            DssDatabase dssDatabase = new DssDatabase(getOutputDssDatabaseName());
            for (DataLocation inputDataLocation : _inputDataLocations) {
                hec.RecordIdentifier timeSeriesIdentifier = new hec.RecordIdentifier(inputDataLocation.getName(), inputDataLocation.getParameter());
                EnsembleTimeSeries ensembleTimeSeries = database.getEnsembleTimeSeries(timeSeriesIdentifier);
                for (DataLocation outDataLocation : _outputDataLocations) {
                        String className = outDataLocation.getClass().getName();
                        MetricCollectionTimeSeries mcts = computeMetrics(ensembleTimeSeries,  outDataLocation, className);
                        database.write(mcts);
                        dssDatabase.write(mcts);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private MetricCollectionTimeSeries computeMetrics(EnsembleTimeSeries ensembleTimeSeries, DataLocation outDataLocation, String classname) throws Exception {
        MetricCollectionTimeSeries mcts = null;
        switch (classname){
            case "HEC.WAT.ForecastProcessor.DataLocations.MultiComputableDataLocation":
                MultiComputableDataLocation mcdl = ((MultiComputableDataLocation)outDataLocation);
                MultiComputable msc = mcdl.getComputableThing();
                if(mcdl.isAcrossTime()){
                     mcts = ensembleTimeSeries.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(msc);
                }
                else{
                     mcts = ensembleTimeSeries.iterateTracesOfEnsemblesWithMultiComputable(msc);
                }
                break;

            case "HEC.WAT.ForecastProcessor.DataLocations.SingleComputableDataLocation":
                SingleComputableDataLocation sdl = ((SingleComputableDataLocation)outDataLocation);
                SingleComputable sc = sdl.getComputableThing();
                mcts = ensembleTimeSeries.computeSingleValueSummary(sc);
                break;

            case "HEC.WAT.ForecastProcessor.DataLocations.ComputableDataLocation":
                ComputableDataLocation cdl = ((ComputableDataLocation)outDataLocation);
                Computable c = cdl.getComputableThing();
                if(cdl.isAcrossTime()){
                    mcts = ensembleTimeSeries.iterateAcrossTimestepsOfEnsemblesWithSingleComputable(c);
                }
                else{
                    mcts = ensembleTimeSeries.iterateAcrossEnsembleTracesWithSingleComputable(c);
                }
                break;
            default:
                throw new Exception("wtf man.");
        }
        return mcts;
    }

    private String getInputOutputDatabaseName() {
        //First Condition to make sure I can unit test this.
        if(_computeOptions.getRunDirectory() == null){
            return "src/test/resources/ensembles.db";
        }
        String runsDir;
        runsDir = _computeOptions.getRunDirectory();
        String databaseFullPath = runsDir.replace("FIRO_WFP"+ File.separator,DatabaseName);
        return databaseFullPath;
    }
    private String getOutputDssDatabaseName() {
        //First Condition to make sure I can unit test this.
        if(_computeOptions.getRunDirectory() == null){
            return "src/test/resources/ensembles.dss";
        }
        String runsDir;
        runsDir = _computeOptions.getRunDirectory();
        String databaseFullPath = runsDir+ DssDatabaseName;
        return databaseFullPath;
    }

    @Override
    public boolean saveData(RmaFile file){
        if(file!=null){
            Element root = new Element(DocumentRoot);
            root.setAttribute(AlternativeNameAttribute,getName());
            root.setAttribute(AlternativeDescriptionAttribute,getDescription());
            root.setAttribute(AlternativeFilenameAttribute,file.getAbsolutePath());
            if(_inputDataLocations!=null) {
                saveDataLocations(root, _inputDataLocations);}
            if(_outputDataLocations!=null) {
                saveOutputDataLocations(root, _outputDataLocations);}
            Document doc = new Document(root);
            return writeXMLFile(doc,file);
        }
        return false;
    }

    @Override
    protected void loadOutputDataLocations(Element root, List<DataLocation> outputDataLocations) {
        Element OutputDataLocationsEle = root.getChild(OutputDataLocationParentElement);
        for ( Object child: OutputDataLocationsEle.getChildren() ){
            Element outputEle = (Element)child;
            //Get the data location type
            String dataLocationtype = outputEle.getAttributeValue("Class");
            switch (dataLocationtype) {
                case "HEC.WAT.ForecastProcessor.DataLocations.ComputableDataLocation":
                    DataLocation cdl = new ComputableDataLocation();
                    cdl.fromXML(outputEle);
                    outputDataLocations.add(cdl);
                    break;
                case "HEC.WAT.ForecastProcessor.DataLocations.MultiComputableDataLocation":
                    DataLocation mcl = new MultiComputableDataLocation();
                    mcl.fromXML(outputEle);
                    outputDataLocations.add(mcl);
                    break;
                case "HEC.WAT.ForecastProcessor.DataLocations.SingleComputableDataLocation":
                    DataLocation scdl = new SingleComputableDataLocation();
                    scdl.fromXML(outputEle);
                    outputDataLocations.add(scdl);
                    break;
                case "hec2.model.DataLocation":
                    DataLocation dl = new DataLocation();
                    dl.fromXML(outputEle);
                    outputDataLocations.add(dl);
                default:
                    //None of these.
                    break;
                }
        }
    }

    @Override
    public boolean loadDocument(org.jdom.Document dcmnt) {
        if(dcmnt!=null){
            org.jdom.Element ele = dcmnt.getRootElement();
            if(ele==null){
                System.out.println("No root element on the provided XML document.");
                return false;
            }
            if(ele.getName().equals(DocumentRoot)){
                setName(ele.getAttributeValue(AlternativeNameAttribute));
                setDescription(ele.getAttributeValue(AlternativeDescriptionAttribute));
                String val = ele.getAttributeValue(AlternativeFilenameAttribute);
                RmaFile file = new RmaFile(val);
                setFile(file);
            }else{
                System.out.println("XML document root was named " + ele.getName() + " but we expected " + DocumentRoot);
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

//These guys are just here for testing. We wouldn't really want default input and output data locations
    public List<DataLocation> defaultInputDataLocations() {
        List<DataLocation> dlList = new ArrayList<>();
        //create datalocations for each location of interest, so that it can be linked to output from other models.
        DataLocation Inflow = new DataLocation(this.getModelAlt(),"ADOC","FLOW");
        dlList.add(Inflow);
        return dlList;
    }
    public List<DataLocation> defaultOutputDataLocations() {
        List<DataLocation> dlList = new ArrayList<>();
        float[] percentilesToCompute = new float[]{.95f,.90f,.75f,.50f,.25f,.10f,.05f};
        MultiComputable cumulativeComputable = new CumulativeComputable();
        int[] daysToCompute = new int[]{1,2,3};
        for( int days : daysToCompute){
            Computable cumulative = new NDayMultiComputable(cumulativeComputable,days);
            for( float percentile : percentilesToCompute){
                Computable percentileCompute = new PercentilesComputable(percentile);
                SingleComputable twoStep = new TwoStepComputable(cumulative,percentileCompute,false);
                SingleComputableDataLocation dl = new SingleComputableDataLocation(this.getModelAlt(),twoStep.StatisticsLabel(), "VOLUME",twoStep);
                dlList.add(dl);
            }
            Computable meanCompute = new MeanComputable();
            SingleComputable twoStep = new TwoStepComputable(cumulative,meanCompute,false);
            SingleComputableDataLocation dl = new SingleComputableDataLocation(this.getModelAlt(), twoStep.StatisticsLabel(), "VOLUME",twoStep);
            dlList.add(dl);
            return dlList;
        }
        return dlList;
    }

    public void setInputDataLocations(List<DataLocation> locs) {
        _inputDataLocations.clear();
        _inputDataLocations.addAll(locs);
    }

    public void setOutputDataLocations(List<DataLocation> locs) {
        _outputDataLocations.clear();
        _outputDataLocations.addAll(locs);
    }

    public void setTimeStep(String timeStep)
    {
            _timeStep = timeStep;
    }

    public String getTimeStep()
    {
        return _timeStep;
    }

}
