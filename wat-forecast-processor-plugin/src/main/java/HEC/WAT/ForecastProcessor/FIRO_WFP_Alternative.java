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
import hec.RecordIdentifier;
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
import hec2.model.DataLocationType;
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
public class FIRO_WFP_Alternative extends SelfContainedPluginAlt {
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
    private ComputeOptions _computeOptions;
    private List<OutputVariable> _outputVariables;

    //endregion
    //region Constructors
    public FIRO_WFP_Alternative() {
        super();
    }

    public FIRO_WFP_Alternative(String name) {
        this();
        setName(name);
    }

    //endregion
    //region Getters and Setters
    @Override
    public int getModelCount() {
        return 1;
    }

    public List<DataLocation> getInputDataLocations() {
        //construct input data locations.
        //if (_inputDataLocations.isEmpty()) {
        //    defaultInputDataLocations();
        //}
        return _inputDataLocations;
    }

    public List<DataLocation> getOutputDataLocations() {
        //construct input data locations.
        //if (_outputDataLocations.isEmpty()) {
        //    defaultOutputDataLocations();
        //}
        // update data locations
        // cast outputs to DssDataLocations only.

        for (DataLocation odl : _outputDataLocations) {
            DssDataLocation dssDataLoc = (DssDataLocation) odl;
            if (_computeOptions != null) {
                dssDataLoc.set_dssFile(getSimulationFileName());
                DSSPathname updatedPath = new DSSPathname(dssDataLoc.getDssPath());
                updatedPath.setFPart(getFpart());
                odl.setDssPath(updatedPath.toString());
                dssDataLoc.setModelAlternative(this.getModelAlt());
                dssDataLoc.setType(DataLocationType.TIME_SERIES);
            }
        }
        return _outputDataLocations;
    }

    public List<OutputVariable> getOutputVariables() {
        return _outputVariables;
    }

    @Override
    public String getLogFile() {
        return null;
    }

    public void setComputeOptions(ComputeOptions opts) {
        _computeOptions = opts;
    }

    public boolean hasOutputVariables() {
        if (_outputVariables == null || _outputVariables.size() == 0) {
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

    boolean computeOutputVariables(List<OutputVariable> list) {
        return true;
    }

    @Override
    public boolean cancelCompute() {
        return false;
    }

    //endregion
    @Override
    public boolean compute() {
        try {
            DSSFile outFile = DSS.open(getOutputDatabaseName());
            for (DataLocation inputDataLocation : _inputDataLocations) {
                String ensembleLoc = inputDataLocation.getLinkedToLocation().getName();
                String ensembleParam = inputDataLocation.getLinkedToLocation().getParameter();
                String inputDatabaseName = getInputDatabaseName(inputDataLocation);
                // open input database and read input ensembles
                DssDatabase inputDatabase = new DssDatabase(inputDatabaseName);
                RecordIdentifier timeSeriesIdentifier = new RecordIdentifier(ensembleLoc, ensembleParam);
                EnsembleTimeSeries ensembleTimeSeries = inputDatabase.getEnsembleTimeSeries(timeSeriesIdentifier);
                // compute for each metric
                for (DataLocation outDataLocation : _outputDataLocations) {
                    String className = outDataLocation.getClass().getName();
                    MetricCollectionTimeSeries mcts = computeMetrics(ensembleTimeSeries, outDataLocation, className);
                    outFile.write(condenseMetricsAcrossIssueDates(mcts, outDataLocation, getFpart(), outFile));
                }
            }
            outFile.done();
        } catch (Exception e) {
            // super.addComputeErrorMessage("Unable to compute for this alternative.");
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
     *
     * @param mcts
     * @param outputLocation
     * @param fPart
     * @return
     */
    private TimeSeriesContainer condenseMetricsAcrossIssueDates(MetricCollectionTimeSeries mcts, DataLocation outputLocation, String fPart, DSSFile dss) {
        TimeSeriesContainer outTSC = new TimeSeriesContainer();
        DSSPathname outPath = new DSSPathname(outputLocation.getDssPath());
        outPath.setBPart(outputLocation.getName()); //mcts.getTimeSeriesIdentifier().location);
        String param = outputLocation.getParameter(); //mcts.getTimeSeriesIdentifier().parameter;
        //String metric = outputLocation.getName();
        //outTSC.parameter = param;
        //outTSC.subParameter = metric;
        //outPath.setCPart(String.format("%s-%s", param, metric));
        outPath.setCPart(param);
        outPath.setEPart("IR-YEAR");  // irregular because we don't know the interval of the forecasts anywhere here
        outPath.setFPart(fPart);
        int nItems = mcts.getIssueDates().size();
        int[] times = new int[nItems];
        double[] values = new double[nItems];
        int i = 0;

        // leaving this commented out for now, might be useful in the future.
        // HecTime endTimeStep = _computeOptions.getRunTimeWindow().getEndTime();  // this works, other methods don't.
        // ZonedDateTime simEndTime = getZonedDateTime(endTimeStep);
        for (ZonedDateTime t : mcts.getIssueDates()) {
            times[i] = getHecTime(t).value();
            MetricCollection mc = mcts.getMetricCollection(t);
            values[i] = mc.getValues()[0][0];
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
        if (WAT_METHOD) {
            int[] newTimes = new int[nItems * 2];
            double[] newValues = new double[nItems * 2];
            int interval = 0;
            int prevTime = times[0];
            for (int j = 0; j < nItems; j++) {
                newTimes[2 * j] = times[j];
                newValues[2 * j] = values[j];
                if (j > 0) { // add our end-of-period value
                    newTimes[2 * j - 1] = times[j] - 1; // one minute before current time
                    newValues[2 * j - 1] = values[j - 1]; // previous value
                }
                interval = Math.max(interval, times[j] - prevTime); // take largest forecast step
                prevTime = times[j];
            }
            // set the last value as one "interval" out from the last forecast issue date
            newTimes[nItems * 2 - 1] = times[nItems - 1] + interval - 1;
            newValues[nItems * 2 - 1] = values[nItems - 1];
            // reassign if we're using these
            times = newTimes;
            values = newValues;
        }

        // finally, fill out TSC
        outTSC.values = values;
        outTSC.times = times;
        outTSC.numberValues = values.length;
        outTSC.startHecTime = new HecTime(times[0]);
        outTSC.endHecTime = new HecTime(times[times.length - 1]);
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
        switch (classname) {
            case "HEC.WAT.ForecastProcessor.DataLocations.MultiComputableDataLocation":
                MultiComputableDataLocation mcdl = ((MultiComputableDataLocation) outDataLocation);
                MultiComputable msc = mcdl.getComputableThing();
                if (mcdl.isAcrossTime()) {
                    mcts = ensembleTimeSeries.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(msc);
                } else {
                    mcts = ensembleTimeSeries.iterateTracesOfEnsemblesWithMultiComputable(msc);
                }
                break;

            case "HEC.WAT.ForecastProcessor.DataLocations.SingleComputableDataLocation":
                SingleComputableDataLocation sdl = ((SingleComputableDataLocation) outDataLocation);
                SingleComputable sc = sdl.getComputableThing();
                mcts = ensembleTimeSeries.computeSingleValueSummary(sc);
                break;

            case "HEC.WAT.ForecastProcessor.DataLocations.ComputableDataLocation":
                ComputableDataLocation cdl = ((ComputableDataLocation) outDataLocation);
                Computable c = cdl.getComputableThing();
                if (cdl.isAcrossTime()) {
                    mcts = ensembleTimeSeries.iterateAcrossTimestepsOfEnsemblesWithSingleComputable(c);
                } else {
                    mcts = ensembleTimeSeries.iterateAcrossEnsembleTracesWithSingleComputable(c);
                }
                break;
            default:
                throw new Exception("DataLocation of class " + classname + " not supported at this time.");
        }
        return mcts;
    }

    private String getInputDatabaseName(DataLocation idl){
        if(_computeOptions.isFrmCompute()){
            return getInputDatabaseNameFRA();
        } else {
            String linkedDssFilename = ((DssDataLocation) idl.getLinkedToLocation()).get_dssFile();
            return getProject().getAbsolutePath(linkedDssFilename);
        }
    }

    private String getInputDatabaseNameFRA() {
        if (_computeOptions.getRunDirectory() == null) {
            return "src/test/resources/ensembles_by_event.dss";
        }
        if (_computeOptions.isFrmCompute()){
            // if it is an fra compute, otherwise, get data location DSS
            Path runDir = FileSystems.getDefault().getPath(_computeOptions.getRunDirectory());
            // resolve to event-based DSS output for ensembles
            String ensembleFilenameTemplate = "syn-fcst-event_%06d.dss";
            int eventID = _computeOptions.getCurrentEventNumber();
            String ensembleFilename = String.format(ensembleFilenameTemplate, eventID);
            Path inputDatabaseName = runDir.getParent().getParent().resolve(ensembleFilename);
            return inputDatabaseName.toString();
        } else {
            // else, compute is not FRM compute
            return "" ;
        }
    }

    // the following methods are used to catch null _computeOptions when initializing the plugin.
    private String getSimulationName() {
        if(_computeOptions == null) return "No Simulation";
        else return _computeOptions.getSimulationName();
    }
    private String getFpart() {
        if(_computeOptions == null) return super.getModelAlt().getFpart();
        else return _computeOptions.getFpart();
    }
    private String getSimulationFileName() {
        if(_computeOptions == null) return "wat_simulation.dss";
        else return _computeOptions.getDssFilename();
    }

    private String getOutputDatabaseName() {
        if (_computeOptions.getRunDirectory() == null) {
            return "src/test/resources/lifecycle_example.dss";
        }
        // resolve to lifecycle DSS file
        String simFileName = getSimulationFileName();
        //Path runDir = FileSystems.getDefault().getPath(_computeOptions.getRunDirectory());
        //Path lifecycleFile = runDir.resolve("realization 1").resolve("lifecycle 1").resolve(simFileName);
        //Path outputDatabaseName = FileSystems.getDefault().getPath(_computeOptions.getRunDirectory()).resolve(lifecycleFile);
        return simFileName; //outputDatbaseName.toString();
    }

    @Override
    public boolean saveData(RmaFile file) {
        if (file != null) {
            Element root = new Element(DocumentRoot);
            root.setAttribute(AlternativeNameAttribute, getName());
            root.setAttribute(AlternativeDescriptionAttribute, getDescription());
            root.setAttribute(AlternativeFilenameAttribute, file.getAbsolutePath());
            saveDataLocations(root, _inputDataLocations);
            saveOutputDataLocations(root, _outputDataLocations);
            Document doc = new Document(root);
            return writeXMLFile(doc, file);
        }
        return false;
    }

    @Override
    protected void loadOutputDataLocations(Element root, List<DataLocation> outputDataLocations) {
        Element OutputDataLocationsEle = root.getChild(OutputDataLocationParentElement);
        for (Object child : OutputDataLocationsEle.getChildren()) {
            Element outputEle = (Element) child;
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
    public boolean loadDocument(Document dcmnt) {
        if (dcmnt != null) {
            Element ele = dcmnt.getRootElement();
            if (ele == null) {
                System.out.println("No root element on the provided XML document.");
                return false;
            }
            if (ele.getName().equals(DocumentRoot)) {
                setName(ele.getAttributeValue(AlternativeNameAttribute));
                setDescription(ele.getAttributeValue(AlternativeDescriptionAttribute));
                String val = ele.getAttributeValue(AlternativeFilenameAttribute);
                RmaFile file = new RmaFile(val);
                setFile(file);
            } else {
                System.out.println("XML document root was named " + ele.getName() + " but we expected " + DocumentRoot);
                return false;
            }
            _inputDataLocations.clear();
            _outputDataLocations.clear();
            loadDataLocations(ele, _inputDataLocations);
            loadOutputDataLocations(ele, _outputDataLocations);
            setModified(false);
            return true;
        } else {
            System.out.println("XML document was null.");
            return false;
        }
    }

    private String getDssPathnameForDataLocation(String location, String parameter) {
        String aPart = "";
        String bPart = location;
        String cPart = parameter;
        String dPart = "";
        String ePart = this.getTimeStep();
        String fPart = _computeOptions == null ? this.getModelAlt().getFpart() : _computeOptions.getFpart();
        // TODO: use DssPathname class!
        return "/" + aPart + "/" + bPart + '/' + cPart + '/' + dPart + '/' + ePart + '/' + fPart + "/";
    }

    private String getDssPathnameForDataLocation(String location, String parameter, String subparameter) {
        return getDssPathnameForDataLocation(location, parameter + "-" + subparameter);
    }

    /**
     * Adds the input data location specified and updates the output data locations.
     *
     * @param locs input DataLocations
     */
    public void addInputDataLocations(List<DataLocation> locs) {
        addInputDataLocations(locs, true);
    }

    /**
     *
     * @param locs                      input DataLocations
     * @param updateOutputDataLocations should the default output data locations be created for each input data location?
     */
    public void addInputDataLocations(List<DataLocation> locs, boolean updateOutputDataLocations) {
        setInputDataLocations(locs);
        if (updateOutputDataLocations) {
            ArrayList<DataLocation> odls = new ArrayList<>();
            for (DataLocation idl : locs) {
                odls.addAll(createDefaultOutputMetrics(idl));
            }
            setOutputDataLocations(odls);
        }
    }

    /**
     * Creates a set of default metrics from the input data location given.
     *
     * @param inputDataLocation
     * @return
     */
    private List<DataLocation> createDefaultOutputMetrics(DataLocation inputDataLocation) {
        String locationName = inputDataLocation.getName();
        String param = inputDataLocation.getParameter();
        String outputDssFilename = getSimulationFileName();
        String file = outputDssFilename == null ? "unknown.dss" : outputDssFilename;

        float[] percentilesToCompute = new float[]{.95f, .90f, .75f, .50f, .25f, .10f, .05f};
        float[] daysToCompute = new float[]{1f, 2f, 3f, 4f, 5f};

        ArrayList<DataLocation> odls = new ArrayList<>();
        for (float days : daysToCompute) {
            for (float percentile : percentilesToCompute) {
                Computable percentileCompute = new PercentilesComputable(percentile);
                odls.add(createCumulativeOutputDataLocation(percentileCompute, inputDataLocation, days));
            }
            Computable meanCompute = new MeanComputable();
            odls.add(createCumulativeOutputDataLocation(meanCompute, inputDataLocation, days));
        }
        return odls;
    }

    /**
     * We do this twice in the above method, this standardizes it; DRY.
     * @param metric
     * @param inputDataLocation
     * @param durationDays
     * @return
     */
    private DataLocation createCumulativeOutputDataLocation(Computable metric, DataLocation inputDataLocation, float durationDays){
        MultiComputable cumulativeComputable = new CumulativeComputable();
        float[] daysArray = new float[]{durationDays};
        Computable cumulative = new NDayMultiComputable(cumulativeComputable, daysArray);
        SingleComputable twoStep = new TwoStepComputable(cumulative, metric, false);
        String path = getDssPathnameForDataLocation(inputDataLocation.getName(), inputDataLocation.getParameter(), twoStep.StatisticsLabel());
        String dssFile = getSimulationFileName();
        SingleComputableDataLocation dl = new SingleComputableDataLocation(path, dssFile, twoStep);
        dl.setName(inputDataLocation.getName());
        dl.setParameter(inputDataLocation.getParameter() + "-" + twoStep.StatisticsLabel());
        dl.setAlternativeName(this.getName());
        return dl;
    }

    public void setInputDataLocations(List<DataLocation> locs) {
        _inputDataLocations.clear();
        _inputDataLocations.addAll(locs);
    }

    public void setOutputDataLocations(List<DataLocation> locs) {
        _outputDataLocations.clear();
        _outputDataLocations.addAll(locs);
    }

    public void setTimeStep(String timeStep) {
        _timeStep = timeStep;
    }

    public String getTimeStep() {
        return _timeStep;
    }

}
