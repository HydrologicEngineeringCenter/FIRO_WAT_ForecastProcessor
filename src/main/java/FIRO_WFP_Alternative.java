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
import hec.stats.*;
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
    List<DataLocation> _outputDataLocations ;
    private static final String DocumentRoot = "FIRO_WFP_Alternative";
    private static final String OutputVariableElement = "OutputVariables";
    private static final String AlternativeNameAttribute = "Name";
    private static final String AlternativeDescriptionAttribute = "Desc";
    private static final String OutputDataLocationParentElement = "OutputDataLocations";
    private static final String OutputDataLocationsChildElement = "OutputDataLocation";
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
        String databaseName = getOutputDatabaseName();
        try {
            SqliteDatabase database = new SqliteDatabase(databaseName, SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_NO_UPDATE);
            for (DataLocation inputDataLocation : _inputDataLocations) {
                hec.RecordIdentifier timeSeriesIdentifier = new hec.RecordIdentifier(inputDataLocation.getName(), inputDataLocation.getParameter());
                EnsembleTimeSeries ensembleTimeSeries = database.getEnsembleTimeSeries(timeSeriesIdentifier);
                for (DataLocation outDataLocation : _outputDataLocations) {
                    if (outDataLocation.getName().equals(inputDataLocation.getName())) {
                        String className = outDataLocation.getClass().getName();
                        MetricCollectionTimeSeries mcts = computeMetrics(ensembleTimeSeries,  outDataLocation, className);
                        database.write(mcts);
                    }
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
            case "MultiComputableDataLocation":
                MultiComputableDataLocation mcdl = ((MultiComputableDataLocation)outDataLocation);
                MultiComputable msc = mcdl.getComputableThing();
                if(mcdl.isAcrossTime()){
                     mcts = ensembleTimeSeries.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(msc);
                }
                else{
                     mcts = ensembleTimeSeries.iterateTracesOfEnsemblesWithMultiComputable(msc);
                }
                break;

            case "SingleComputableDataLocation":
                SingleComputableDataLocation sdl = ((SingleComputableDataLocation)outDataLocation);
                SingleComputable sc = sdl.getComputableThing();
                mcts = ensembleTimeSeries.computeSingleValueSummary(sc);
                break;

            case "ComputableDataLocation":
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

    private String getOutputDatabaseName() {
        String dssName;
        dssName = _computeOptions.getDssFilename();
        return dssName.substring(0,dssName.length() - 3) + "db";
    }

    @Override
    public boolean saveData(RmaFile file){
        if(file!=null){
            Element root = new Element(DocumentRoot);
            root.setAttribute("AlternativeNameAttribute",getName());
            root.setAttribute("AlternativeDescriptionAttribute",getDescription());
            root.setAttribute("AlternativeFilenameAttribute",file.getAbsolutePath());
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
                case "ComputableDataLocation":
                    DataLocation cdl = new ComputableDataLocation();
                    cdl.fromXML(outputEle);
                    outputDataLocations.add(cdl);
                    break;
                case "MultiComputableDataLocation":
                    DataLocation mcl = new MultiComputableDataLocation();
                    mcl.fromXML(outputEle);
                    outputDataLocations.add(mcl);
                    break;
                case "SingleComputableDataLocation":
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
            loadDataLocations(ele, _outputDataLocations);

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
    public List<DataLocation> defaultOutputDataLocations() {
        List<DataLocation> dlList = new ArrayList<>();
        //create datalocations for each location of interest, so that it can be linked to output from other models.
        Computable mc = new MaxComputable();
        MultiComputable msc = new MultiStatComputable(new Statistics[]{Statistics.MIN,Statistics.MAX,Statistics.AVERAGE});
        SingleComputable sc = new TwoStepComputable(new MaxComputable(), new MeanComputable(), true);
        DataLocation ComputableEnsemble = new ComputableDataLocation(this.getModelAlt(),"Compute","flow",mc);
        DataLocation MultiComputableEnsemble = new MultiComputableDataLocation(this.getModelAlt(),"MultiCompute","flow",msc);
        DataLocation SingleComputableEnsemble = new SingleComputableDataLocation(this.getModelAlt(),"SingleCompute","flow",sc);
        dlList.add(ComputableEnsemble);
        dlList.add(MultiComputableEnsemble);
        dlList.add(SingleComputableEnsemble);

        return dlList;
    }


}
