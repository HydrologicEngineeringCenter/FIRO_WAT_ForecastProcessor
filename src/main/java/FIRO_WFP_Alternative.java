/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.rma.io.RmaFile;
import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;
import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;
import hec.data.Parameter;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.TimeSeriesIdentifier;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.TimeSeriesIdentifier;
import hec.model.OutputVariable;
import hec.model.RunTimeWindow;
import hec.stats.MaxAvgDuration;
import hec.stats.MeanComputable;
import hec.stats.MedianComputable;
import hec.stats.MinComputable;
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
    private List<OutputVariable> _outputVariables;
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

    private boolean computeForTimeWindow(RunTimeWindow rtw){
        String dssName;
        dssName = _computeOptions.getDssFilename();
        String databaseName = dssName.substring(0,dssName.length() - 3) + "db";

        try {
            TimeSeriesDatabase database = new JdbcTimeSeriesDatabase(databaseName, JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_NO_UPDATE);
            //  loop on input data locations
            // determine location and parameter to create timeseries identifier from an input data location
            for(int k=0; k<_inputDataLocations.size(); k++){
                TimeSeriesIdentifier timeSeriesIdentifier = new TimeSeriesIdentifier(_inputDataLocations.get(k).getName(), _inputDataLocations.get(k).getParameter());
                EnsembleTimeSeries ensembleTimeSeries = database.getEnsembleTimeSeries(timeSeriesIdentifier);
                // Use the timewindow  rtw to iterate over issuance dates
                //we now need to use the timewindow of the event and map to issuance dates in the database
                int timeStepsInWindow = rtw.getNumSteps();
                String rtwStartTime = rtw.getStartTimeString();
                List<ZonedDateTime> issueDates = ensembleTimeSeries.getIssueDates();
                int startingIndex = issueDates.indexOf(rtwStartTime); //This won't work. Not the right type. Need to figure out how to convert

                //create an ensemble for each issue date. For loop
                for(int i = startingIndex; i <= timeStepsInWindow; i++){
                    Ensemble ensemble = database.getEnsemble(timeSeriesIdentifier,issueDates.get(i));
                    WatFrame fr = hec2.wat.WAT.getWatFrame();
                    fr.addMessage("We got the ensemble data!");

                    for(int j = 0; j<_outputDataLocations.size(); j++){
                        //from output data locations determine computes we need to perform for each output data location at this location
                        //check for location, compute type, store data
                        boolean isCorrectInputLocation = _inputDataLocations.get(k).equals(_outputDataLocations.get(j));

                        if (_outputDataLocations.get(j).getComputeType().toString().equals(EnsembleComputeTypes.Mean.toString()) && isCorrectInputLocation){
                            hec.stats.Computable stat = new MeanComputable();
                            float[] output = ensemble.iterateForTimeAcrossTraces(stat);
                            fr.addMessage("This is the mean across time for the first trace: " + output[0] );
                        }
                        if (_outputDataLocations.get(j).getComputeType().toString().equals(EnsembleComputeTypes.Median.toString()) && isCorrectInputLocation){
                            hec.stats.Computable test2 = new MedianComputable();
                            float[] output2 = ensemble.iterateForTimeAcrossTraces(test2);
                            fr.addMessage("This is mean across traces for the first timestep: " + output2[0]);

                        }
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        return _outputVariables;
    }
    public boolean hasOutputVariables(){
        if (_outputVariables != null){
            if(_outputVariables.size() == 0){
                return false;
            }else{
                return true;
            }
        }else{
            return false;
        }
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

        DataLocation KanatockEnsemble = new DataLocation(this.getModelAlt(),"Kanatook",EnsembleComputeTypes.Max.toString());
        dlList.add(KanatockEnsemble);

        return dlList;
    }


}
