import com.rma.script.ScriptEditor;
import com.rma.swing.RmaFileChooserField;
import hec.gui.AbstractEditorPanel;
import hec.heclib.dss.HecTimeSeries;
import hec.lang.NamedType;
import hec2.model.DataLocation;
import hec2.plugin.model.ModelAlternative;
import rma.swing.RmaInsets;
import rma.swing.RmaJComboBox;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;


@SuppressWarnings("serial")
public class WfpAltPanel extends AbstractEditorPanel
{
	private static final Logger LOGGER = Logger.getLogger(FIRO_WFP_Alternative.class.getName());
	private static final String MAX_SELECT_ATTEMPTS = "WAT.Scripting.MaxSelectAttempts";
	private static final int DEFAULT_MAX_SELECT_ATTEMPTS = 10;


	private RmaFileChooserField _programSpecFld;
	private JButton _editScriptButton;
	private String _tabName;

	private JPanel _dataLocPanel;

	private FIRO_WFP_Alternative _alt;
	private DataLocationPanel _inputPanel;
	private DataLocationPanel _outputPanel;
	private ModelAlternative _modelAlt;
	private RmaJComboBox<String> _timeStepCombo;
	private Vector<String> _timeSteps;

	private ScriptEditor _editor;

	public WfpAltPanel()
	{
		super(new GridBagLayout());
		buildControls();
		addListeners();
	}

	/**
	 * 
	 */
	private void buildControls()
	{
		_tabName = FIRO_WFP_PluginI18n.getI18n(WfpMessages.EDITOR_PANEL_TAB_NAME).getText();

		
		buildBottomTabs();
	}
	
	/**
	 * 
	 */
	private void buildBottomTabs()
	{
		JTabbedPane tabHolder = new JTabbedPane();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx     = 0;
		gbc.gridy     = 1;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx   = 1.0;
		gbc.weighty   = 1.0;
		gbc.anchor    = GridBagConstraints.NORTHWEST;
		gbc.fill      = GridBagConstraints.BOTH;
		gbc.insets    = RmaInsets.INSETS5505;
		add(tabHolder, gbc);

        _dataLocPanel = buildDataPanel();
        String dataTitle = FIRO_WFP_PluginI18n.getI18n(WfpMessages.EDITOR_PANEL_DATA_LOC_TITLE).getText(); //"Data Locations"));
        tabHolder.addTab(dataTitle, _dataLocPanel);


    }


	private JPanel buildDataPanel() {
        JPanel dataLocPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx     = 0;
        gbc.gridy     = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx   = 1.0;
        gbc.weighty   = 1.0;
        gbc.anchor    = GridBagConstraints.NORTHWEST;
        gbc.fill      = GridBagConstraints.BOTH;
        gbc.insets    = RmaInsets.INSETS5505;

        _inputPanel = new DataLocationPanel(this);
        _outputPanel = new DataLocationPanel(this);

        JLabel label = FIRO_WFP_PluginI18n.getI18n(WfpMessages.EDITOR_PANEL_TIME_STEP_LABEL).createJLabel();
        gbc.gridx     = GridBagConstraints.RELATIVE;
        gbc.gridy     = GridBagConstraints.RELATIVE;
        gbc.gridwidth = 1;
        gbc.weightx   = 0.0;
        gbc.weighty   = 0.0;
        gbc.anchor    = GridBagConstraints.WEST;
        gbc.fill      = GridBagConstraints.NONE;
        gbc.insets    = RmaInsets.INSETS5505;
        dataLocPanel.add(label, gbc);

        _timeSteps = HecTimeSeries.getListOfEParts();
        _timeStepCombo = new RmaJComboBox<>(_timeSteps);
        _timeStepCombo.setSelectedItem(FIRO_WFP_PluginI18n.getI18n(WfpMessages.EDITOR_PANEL_TIME_STEP_DEFAULT).getText());
        _timeStepCombo.setModifiable(true);
        label.setLabelFor(_timeStepCombo);
        gbc.gridx     = GridBagConstraints.RELATIVE;
        gbc.gridy     = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx   = 1.0;
        gbc.weighty   = 0.0;
        gbc.anchor    = GridBagConstraints.WEST;
        gbc.fill      = GridBagConstraints.NONE;
        gbc.insets    = RmaInsets.INSETS5505;
        dataLocPanel.add(_timeStepCombo, gbc);

        JTabbedPane tabbedPane = new JTabbedPane();
        gbc.gridx     = GridBagConstraints.RELATIVE;
        gbc.gridy     = GridBagConstraints.RELATIVE;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx   = 1.0;
        gbc.weighty   = 1.0;
        gbc.anchor    = GridBagConstraints.NORTHWEST;
        gbc.fill      = GridBagConstraints.BOTH;
        gbc.insets    = RmaInsets.INSETS5505;
        dataLocPanel.add(tabbedPane, gbc);

        String title = FIRO_WFP_PluginI18n.getI18n(WfpMessages.EDITOR_PANEL_INPUT_TITLE).getText();
        //"Input"
        tabbedPane.addTab(title, _inputPanel);
        title = FIRO_WFP_PluginI18n.getI18n(WfpMessages.EDITOR_PANEL_OUTPUT_TITLE).getText();
        //"Output"
        tabbedPane.addTab(title, _outputPanel);
        return dataLocPanel;
    }

    /**
	 * 
	 */
	private void addListeners()
	{
	}
	
	
	

	
	
	
	
	
	
	/* (non-Javadoc)
	 * @see hec.gui.AbstractEditorPanel#getTabname()
	 */
	@Override
	public String getTabname()
	{
		return _tabName;
	}

	/* (non-Javadoc)
	 * @see hec.gui.AbstractEditorPanel#fillPanel(hec.lang.NamedType)
	 */
	@Override
	public void fillPanel(NamedType dobj)
	{
		if ( !(dobj instanceof FIRO_WFP_Alternative) )
		{
			return;
		}
		_alt = (FIRO_WFP_Alternative) dobj;
		setModelAlternative(_alt.getModelAlt());
		
		String timeStep = _alt.getTimeStep();
		if ( timeStep != null )
		{
			_timeStepCombo.setSelectedItem(timeStep);
		}
		else
		{
			_timeStepCombo.setSelectedIndex(-1);
		}
		
		List<DataLocation> inputDls = _alt.getInputDataLocations();
		_inputPanel.setDataLocations(inputDls);
		List<DataLocation> outputDls = _alt.getOutputDataLocations();
		_outputPanel.setDataLocations(outputDls);

		
	}

	
	
	/* (non-Javadoc)
	 * @see hec.gui.AbstractEditorPanel#savePanel(hec.lang.NamedType)
	 */
	@Override
	public boolean savePanel(NamedType dobj)
	{
		FIRO_WFP_Alternative alt;
		if ( dobj instanceof FIRO_WFP_Alternative )
		{
			alt = (FIRO_WFP_Alternative) dobj;
		}
		else
		{
			alt = _alt;
		}
		if ( alt == null )
		{
			return true;
		}
		List<DataLocation> locs = _inputPanel.getDataLocations();
		alt.setInputDataLocations(locs);
		locs = _outputPanel.getDataLocations();
		alt.setOutputDataLocations(locs);


		alt.setTimeStep((String)_timeStepCombo.getSelectedItem());
		alt.setModified(true);
		alt.saveData();
		
		return true;
	}

	/**
	 * @param modelAlt
	 */
	public void setModelAlternative(ModelAlternative modelAlt)
	{
		_inputPanel.setModelAlternative(modelAlt);
		_outputPanel.setModelAlternative(modelAlt);
		_modelAlt = modelAlt;
	}

	/**
	 * @return
	 */
	public String getTimeStep()
	{
		return (String) _timeStepCombo.getSelectedItem();
	}
	
	

	public List<DataLocation> getOutputDataLocations(){
		List<DataLocation> retval = Collections.emptyList();

		List<DataLocation> panelLocs = _outputPanel.getDataLocations();
		if(panelLocs != null && !panelLocs.isEmpty()){
			retval = Collections.unmodifiableList(panelLocs);
		}

		return retval;
	}
}
