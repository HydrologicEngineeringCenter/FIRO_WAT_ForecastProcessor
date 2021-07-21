import hec.data.Parameter;
import hec.model.OutputVariable;
import hec2.model.DataLocation;
import hec2.wat.model.tracking.OutputVariableImpl;
import org.jdom.Element;

public class FIRO_WFP_OutputVariable {
    //Fields
    private StatisticTypes _statisticType;
    private StatisticDirection _statisticDirection;
    private DataLocation _location;
    private final OutputVariable _outputVariable;

    //Getters
    public StatisticTypes get_statisticType() {
        return _statisticType;
    }

    public StatisticDirection get_statisticDirection() {
        return _statisticDirection;
    }

    public DataLocation getLocation() {
        return _location;
    }

    //Constructor

    public FIRO_WFP_OutputVariable(StatisticTypes _statisticType, StatisticDirection _statisticDirection, DataLocation _location) {
        this._statisticType = _statisticType;
        this._statisticDirection = _statisticDirection;
        this._location = _location;

        //initialize the output variable
        _outputVariable = new OutputVariableImpl();
        ((OutputVariableImpl)_outputVariable).setName(createName());
        ((OutputVariableImpl)_outputVariable).setDescription(createDescription());
        if(_location.getParameter().equals("Flow")){
            ((OutputVariableImpl)_outputVariable).setParamId(Parameter.PARAMID_FLOW);
        }else if(_location.getParameter().equals("FLOW-IN")){
            ((OutputVariableImpl)_outputVariable).setParamId(Parameter.PARAMID_FLOW);
        }else if(_location.getParameter().equals("Flow-Unreg")){
            ((OutputVariableImpl)_outputVariable).setParamId(Parameter.PARAMID_FLOW);
        }else if(_location.getParameter().equals("FLOW-OUT")){
            ((OutputVariableImpl)_outputVariable).setParamId(Parameter.PARAMID_FLOW);
        }else if(_location.getParameter().equals("ELEV")){
            ((OutputVariableImpl)_outputVariable).setParamId(Parameter.PARAMID_ELEV);
        }else{
            ((OutputVariableImpl)_outputVariable).setParamId(Parameter.UNDEF_PARAMETER_ID); }
    }

    private String createName(){
        return getLocation().getName() + " - " + getLocation().getParameter() + " - " + _statisticType.toString() + " - " + _statisticDirection.toString();
    }
    private String createDescription(){
        return "Self Describing";
    }
}
