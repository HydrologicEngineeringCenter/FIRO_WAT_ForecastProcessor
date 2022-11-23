package HEC.WAT.ForecastProcessor.UI;

import HEC.WAT.ForecastProcessor.FIRO_WFP_PluginI18n;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class WfpMessages {
    public static final String Bundle_Name = FIRO_WFP_PluginI18n.BUNDLE_NAME;
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(Bundle_Name);
    public static final String Plugin_Name = "CfpPlugin.Name";
    public static final String Plugin_Description = "CfpPlugin.Description";
    public static final String Plugin_Short_Name = "CfpPlugin.ShortName";
	public static final String PLUGIN_EDITOR_TITLE = "CfpPlugin.Editor.Title";
	public static final String DATALOCATION_PANEL_HEADER_NAME = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.Header.Name";
	public static final String DATALOCATION_PANEL_HEADER_PARAMETER = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.Header.Parameter";
	public static final String DATALOCATION_PANEL_HEADER_TYPE = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.Header.DataLocationType";
	public static final String DATALOCATION_PANEL_HEADER_USER_DEF_FIELD = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.Header.UserDefField";
	public static final String DATALOCATION_PANEL_HEADER_SET_FIELD = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.Header.SetField";
	public static final String DATALOCATION_PANEL_BUTTON_ADD = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.Button.Add";
	public static final String DATALOCATION_PANEL_BUTTON_DELETE = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.Button.Delete";
	public static final String DATALOCATION_PANEL_DELETE_LOC_MSG = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.Msg.DeleteLocation";
	public static final String DATALOCATION_PANEL_DELETE_LOC_MSG_TITLE = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.Msg.DeleteLocation.Title";
	public static final String DATALOCATION_PANEL_DEFAULT_PARAM = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.DefaultParameter";
	public static final String DATALOCATION_PANEL_DEFAULT_NAME = "HEC.WAT.ForecastProcessor.UI.DataLocationPanel.DefaultName";
	public static final String EDITOR_PANEL_TAB_NAME = "Editor.Panel.Tab.Name";
	public static final String EDITOR_PANEL_DATA_LOC_TITLE = "Editor.Panel.Title.DataLocations";
	public static final String EDITOR_PANEL_TIME_STEP_LABEL = "Editor.Panel.Label.TimeStep";
	public static final String EDITOR_PANEL_TIME_STEP_DEFAULT = "Editor.Panel.TimeStep.Default";
	public static final String EDITOR_PANEL_INPUT_TITLE = "Editor.Panel.Title.Input";
	public static final String EDITOR_PANEL_OUTPUT_TITLE = "Editor.Panel.Title.Output";

    private WfpMessages() {super();}
    public static String getString(String key) {
        try{
            return RESOURCE_BUNDLE.getString(key);
        }
        catch(MissingResourceException e) {
            return "!" + key + "!";
        }
    }
}
