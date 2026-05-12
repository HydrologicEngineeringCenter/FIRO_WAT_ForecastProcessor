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
// import hec.SqliteDatabase;
import hec.dss.ensemble.DssDatabase;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.*;
import hec.heclib.dss.DSSPathname;
import hec.heclib.util.HecTime;
import hec.hecmath.DSS;
import hec.hecmath.DSSFile;
import hec.io.TimeSeriesContainer;
import hec.metrics.MetricCollection;
import hec.metrics.MetricCollectionTimeSeries;
import hec.model.OutputVariable;
import hec2.model.DataLocation;
import hec2.model.DssDataLocation;
import hec2.wat.model.ComputeOptions;
import hec2.plugin.selfcontained.SelfContainedPluginAlt;
import org.jdom.Document;
import org.jdom.Element;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author WatPowerUser
 */
public class FIRO_WFP_Alternative extends SelfContainedPluginAlt{
    //region Fields
    List<DataLocation> _inputDataLocations = new ArrayList<>();
    List<DataLocation> _outputDataLocations = new ArrayList<>();
    String _timeStep;
    private static final String DocumentRoot = "HEC.WAT.ForecastProcessor.FIRO_WFP_Alternative";
    private static final String AlternativeNameAttribute = "Name";
    private static final String AlternativeDescriptionAttribute = "Desc";
    private static final String OutputDataLocationParentElement = "OutputDataLocations";
    private static final String AlternativeFilenameAttribute = "AlternativeFilename";
    private static final String DatabaseName = "ensembles.db";
    private static final String DssDatabaseName = "ensembles.dss";
    private hec2.wat.model.ComputeOptions _computeOptions;
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
        if(_inputDataLocations.isEmpty()){
            defaultInputDataLocations();
        }
        return _inputDataLocations;
    }
    public List<DataLocation> getOutputDataLocations(){
        //construct input data locations.
        if(_outputDataLocations.isEmpty()){
            defaultOutputDataLocations();
        }
        return _outputDataLocations;
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
        try {
            DssDatabase inputDatabase = new DssDatabase(getInputDatabaseName());
            DSSFile outFile = DSS.open(getOutputDatabaseName());

            for (DataLocation inputDataLocation : _inputDataLocations) {
                DssDataLocation inputDataLocDSS = (DssDataLocation) inputDataLocation;
                String ensembleLoc = inputDataLocDSS.getLinkedToLocation().getName();
                String ensembleParam = inputDataLocDSS.getLinkedToLocation().getParameter();
                hec.RecordIdentifier timeSeriesIdentifier = new hec.RecordIdentifier(ensembleLoc, ensembleParam);
                EnsembleTimeSeries ensembleTimeSeries = inputDatabase.getEnsembleTimeSeries(timeSeriesIdentifier);
                for (DataLocation outDataLocation : _outputDataLocations) {
                    String className = outDataLocation.getClass().getName();
                    MetricCollectionTimeSeries mcts = computeMetrics(ensembleTimeSeries, outDataLocation, className);
                    outFile.write(condenseMetricsAcrossIssueDates(mcts, outDataLocation, _computeOptions.getFpart(), outFile));
                }
            }
            outFile.done();
        } catch (Exception e){
            // todo: do something better here!
            e.printStackTrace();
        }

        return true;
    }

    // copied from private static in hec.dss.ensembles.DssDatabase
    private static final DateTimeFormatter dssDateFormat = DateTimeFormatter.ofPattern("ddMMMyyyy HHmm");
    private static HecTime getHecTime(ZonedDateTime zdt) {
        String dateStr = "";
        dateStr = zdt.format(dssDateFormat);
        return new HecTime(dateStr);
    }
    private static ZonedDateTime getZonedDateTime(HecTime time) {
        int hour = time.hour();
        ZoneId timeZone = ZoneId.of("GMT");
        if (hour == 24) {
            String hecTimeStyle = time.toString(-13);
            String[] timeParse = hecTimeStyle.replaceAll("\\s*,\\s*", ",").split(",");
            String date = timeParse[0];
            String timeReset = "00:00:00";
            String formatDateTime = date + "T" + timeReset;
            LocalDateTime ldt = LocalDateTime.parse(formatDateTime);
            return ZonedDateTime.of(ldt, timeZone).plusDays(1L);
        } else {
            return ZonedDateTime.of(time.year(), time.month(), time.day(), time.hour(), time.minute(), time.second(), 0, timeZone);
        }
    }

    /**
     * This method consolidates a single value MetricCollectionTimeSeries into a single irregular timeseries
     * for use within a reservoir operations model.  This is intended to operate with
     * @param mcts
     * @param outputLocation
     * @param fPart
     * @return
     */
    private TimeSeriesContainer condenseMetricsAcrossIssueDates(MetricCollectionTimeSeries mcts, DataLocation outputLocation, String fPart, DSSFile dss) {
        TimeSeriesContainer outTSC = new TimeSeriesContainer();
        DSSPathname outPath = new DSSPathname(outputLocation.getDssPath());
        outPath.setBPart(mcts.getTimeSeriesIdentifier().location);
        String param = mcts.getTimeSeriesIdentifier().parameter;
        String metric = outputLocation.getName();
        outTSC.parameter = param;
        outTSC.subParameter = metric;
        outPath.setCPart(String.format("%s-%s", param, metric));
        outPath.setEPart("IR-YEAR");  // irregular because we don't know the interval of the forecasts anywhere here
        outPath.setFPart(fPart);
        int nItems = mcts.getIssueDates().size();
        int[] times = new int[nItems];
        double[] values = new double[nItems];
        int i = 0;

        // leaving this commented out for now, might be useful in the future.
        // HecTime endTimeStep = _computeOptions.getRunTimeWindow().getEndTime();  // this works, other methods don't.
        // ZonedDateTime simEndTime = getZonedDateTime(endTimeStep);
        for(ZonedDateTime t : mcts.getIssueDates()){
            times[i] = getHecTime(t).value();
            MetricCollection mc = mcts.getMetricCollection(t);
            values[i] =  mc.getValues()[0][0];
            i += 1;
            // check if we've gone past the end of the simulation time window, don't export these values
            // this deals with the weird tail produced by the MCTS compute?
            //if(t.isAfter(simEndTime)){
            //    nItems = i;
            //    break;
            //}
        }

        /* WAT forecast time-series method, this interpolates nicely to finer timesteps.
            FORMAT:  irregular inst-val data, with a point at the issue date and a point
            one minute before the next issue date.  This allows an interpolation routine
            to fill in values on any finer timestep than the issue date and get the
            'current forecast' for each timestep. This follows the approach used in the
            WAT Hydrologic Sampler for seasonal volume forecasts that are issued
            ~monthly, and behaves nicely in downstream models.
         */

        // if doing WAT method
        boolean WAT_METHOD = true;
        if(WAT_METHOD){
            int[] newTimes = new int[nItems*2];
            double[] newValues = new double[nItems*2];
            int interval = 0;
            int prevTime = times[0];
            for(int j=0; j < nItems; j++){
                newTimes[2*j] = times[j];
                newValues[2*j] = values[j];
                if(j > 0){ // add our end-of-period value
                    newTimes[2*j-1] = times[j]-1; // one minute before current time
                    newValues[2*j-1] = values[j-1]; // previous value
                }
                interval = Math.max(interval, times[j] - prevTime); // take largest forecast step
                prevTime = times[j];
            }
            // set the last value as one "interval" out from the last forecast issue date
            newTimes[nItems*2-1] = times[nItems-1]+interval-1;
            newValues[nItems*2-1] = values[nItems-1];
            // reassign if we're using these
            times = newTimes;
            values = newValues;
        }

        // finally, fill out TSC
        outTSC.values = values;
        outTSC.times = times;
        outTSC.numberValues = values.length;
        outTSC.startHecTime = new HecTime(times[0]);
        outTSC.endHecTime = new HecTime(times[times.length-1]);
        ZonedDateTime firstTime = mcts.getIssueDates().get(0);
        outTSC.timeZoneID = firstTime.getZone().getId();
        outTSC.fullName = outPath.toString();
        // inst-val for WAT forecast value method, this interpolates nicely to finer timesteps.
        outTSC.type = WAT_METHOD ? "INST-VAL" : "PER-AVER";
        outTSC.units = mcts.getMetricCollection(firstTime).getUnits();
        outTSC.fileName = dss.getFilename();

        return outTSC;
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

    private String getInputDatabaseName() {
        if(_computeOptions.getRunDirectory() == null) {
            return "src/test/resources/ensembles_by_event.dss";
        }

        Path runDir = FileSystems.getDefault().getPath(_computeOptions.getRunDirectory());
        // resolve to event-based DSS output for ensembles
        String ensembleFilenameTemplate = "syn-fcst-event_%06d.dss";
        int eventID = _computeOptions.getCurrentEventNumber();
        String ensembleFilename = String.format(ensembleFilenameTemplate, eventID);
        Path inputDatabaseName = runDir.getParent().getParent().resolve(ensembleFilename);
        return inputDatabaseName.toString();
    }

    private String getOutputDatabaseName() {
        if(_computeOptions.getRunDirectory() == null){
            return "src/test/resources/lifecycle_example.dss";
        }
        // resolve to lifecycle DSS file
        String simFileName = _computeOptions.getDssFilename();
        //Path runDir = FileSystems.getDefault().getPath(_computeOptions.getRunDirectory());
        //Path lifecycleFile = runDir.resolve("realization 1").resolve("lifecycle 1").resolve(simFileName);
        //Path outputDatabaseName = FileSystems.getDefault().getPath(_computeOptions.getRunDirectory()).resolve(lifecycleFile);
        return simFileName; //outputDatbaseName.toString();
    }

    private String getInputOutputDatabaseName() {
        //First Condition to make sure I can unit test this.
        if(_computeOptions.getRunDirectory() == null){
            return "src/test/resources/ensembles.db";
        }
        String runsDir = _computeOptions.getRunDirectory();
        String[] splitRunsDir = runsDir.split("\\\\");
        StringBuilder databasePath = new StringBuilder();
        for(int i = 0; i<splitRunsDir.length-1;i++ ){
            databasePath.append(splitRunsDir[i] + "\\");
        }
        return databasePath.toString() + DatabaseName;
    }

    @Override
    public boolean saveData(RmaFile file){
        if(file!=null){
            Element root = new Element(DocumentRoot);
            root.setAttribute(AlternativeNameAttribute,getName());
            root.setAttribute(AlternativeDescriptionAttribute,getDescription());
            root.setAttribute(AlternativeFilenameAttribute,file.getAbsolutePath());
            saveDataLocations(root, _inputDataLocations);
            saveOutputDataLocations(root, _outputDataLocations);
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
            _inputDataLocations.clear();
            _outputDataLocations.clear();
            loadDataLocations(ele, _inputDataLocations) ;
            loadOutputDataLocations(ele, _outputDataLocations );
            setModified(false);
            return true;
        }else{
            System.out.println("XML document was null.");
            return false;
        }
    }

    private String GetDssPathnameForDataLocation(String location, String statisticsLabel){
        return "" + '/' + location + '/' + statisticsLabel + '/' + "" + '/' + "" + '/' + "";
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

    //region Testing Data
    //These guys are just here for testing. We wouldn't really want default input and output data locations
    //Use one source of truth
    public void defaultInputDataLocations() {
        //create datalocations for each location of interest, so that it can be linked to output from other models.
        String pathname = GetDssPathnameForDataLocation("FORECAST", "FLOW");
        String outputDssFilename = _computeOptions.getDssFilename();
        String file = outputDssFilename == null ? "unknown.dss" : outputDssFilename;
        DssDataLocation Inflow = new DssDataLocation(file,pathname);
        _inputDataLocations.add(Inflow);

    }
    //populates output data locations when they are empty
    public void defaultOutputDataLocations() {
        float[] percentilesToCompute = new float[]{.95f,.90f,.75f,.50f,.25f,.10f,.05f};
        MultiComputable cumulativeComputable = new CumulativeComputable();
        float[] daysToCompute = new float[]{2f,3f,5f,7f};
        String outputDssFilename = _computeOptions.getDssFilename();
        String file = outputDssFilename == null ? "unknown.dss" : outputDssFilename;
        for( float days : daysToCompute){
            float[] daysArray = new float[]{days};
            Computable cumulative = new NDayMultiComputable(cumulativeComputable,daysArray);
            for( float percentile : percentilesToCompute){
                Computable percentileCompute = new PercentilesComputable(percentile);
                SingleComputable twoStep = new TwoStepComputable(cumulative,percentileCompute,false);
                String path = GetDssPathnameForDataLocation("",twoStep.StatisticsLabel());
                SingleComputableDataLocation dl = new SingleComputableDataLocation(path,file,twoStep);
                _outputDataLocations.add(dl);
            }
            Computable meanCompute = new MeanComputable();
            SingleComputable twoStep = new TwoStepComputable(cumulative,meanCompute,false);
            String path = GetDssPathnameForDataLocation("",twoStep.StatisticsLabel());
            SingleComputableDataLocation dl = new SingleComputableDataLocation(path,file,twoStep);
            _outputDataLocations.add(dl);
        }
    }
    //endregion
}
