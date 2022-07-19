import hec.gui.SelectorPanelEditor;
import hec2.plugin.model.ModelAlternative;

import java.awt.*;


@SuppressWarnings("serial")
public class WfpAltEditor extends SelectorPanelEditor
{
	private WfpAltPanel  _cfpAltPanel;

	public WfpAltEditor(Dialog parent, boolean modal)
	{
		super(parent, modal);
		createControls();
		pack();
		setSize(550,250);
		setLocationRelativeTo(getParent());
	}

	public WfpAltEditor(Frame parent, boolean modal)
	{
		super(parent, modal);
		createControls();
		pack();
		setSize(550,550);
		setLocationRelativeTo(getParent());
	}

	private void createControls()
	{
		setTitle(FIRO_WFP_PluginI18n.getI18n(
				WfpMessages.PLUGIN_EDITOR_TITLE).getText());
		_cfpAltPanel = new WfpAltPanel();
		addPanel(_cfpAltPanel);
	}

	/**
	 * @param modelAlt
	 */
	public void setModelAlternative(ModelAlternative modelAlt)
	{
		_cfpAltPanel.setModelAlternative(modelAlt);
	}
}
