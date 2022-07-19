/*
 * Copyright 2018  Hydrologic Engineering Center (HEC).
 * United States Army Corps of Engineers
 * All Rights Reserved.  HEC PROPRIETARY/CONFIDENTIAL.
 * Source may not be released without written approval
 * from HEC
 */


import hec.heclib.dss.DSSPathname;
import hec2.editors.DataLocationUserDefinedFieldEditor;
import hec2.model.DataLocation;
import hec2.model.DataLocationFactory;
import hec2.model.DataLocationType;
import hec2.model.UserDefinedDataLocationFieldHelper;
import hec2.plugin.PathnameUtilities;
import hec2.plugin.model.ModelAlternative;
import rma.swing.RmaInsets;
import rma.swing.RmaJPanel;
import rma.swing.RmaJTable;
import rma.swing.table.AbstractRmaTableModel;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author Mark Ackerman
 *
 */
@SuppressWarnings("serial")
public class DataLocationPanel extends RmaJPanel
{
	private RmaJTable _dataLocTable;
	private JButton _addBtn;
	private JButton _deleteBtn;
	private DataLocationTableModel _tableModel;
	private ModelAlternative _modelAlt;
	private WfpAltPanel _parentPanel;
	private JButton _button;

	public DataLocationPanel(WfpAltPanel cfpAltPanel)
	{
		super(new GridBagLayout());
		_parentPanel = cfpAltPanel;
		buildControls();
		addListeners();
	}

	/**
	 * 
	 */
	protected void buildControls()
	{
		String hdr1 = FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_HEADER_NAME).getText();
		String hdr2 = FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_HEADER_PARAMETER).getText();
		String hdr3 = FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_HEADER_TYPE).getText();
		String hdr4 = FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_HEADER_USER_DEF_FIELD).getText();
		String[] headers = new String[]
		{ hdr1, hdr2, hdr3, hdr4 };
				//"Linkage Name", "Parameter" };
		_dataLocTable = new RmaJTable(this, headers)
		{
			@Override
			public String getToolTipText(MouseEvent e)
			{
				return getTableToolTipText(e.getPoint());
			}
		};

		_tableModel = new DataLocationTableModel(headers);
		_dataLocTable.setModel(_tableModel);
		_dataLocTable.removePopuMenuFillOptions();
		_dataLocTable.removePopupMenuSumOptions();
		_dataLocTable.setRowHeight(_dataLocTable.getRowHeight()+5);
		_dataLocTable.setComboBoxEditor(2, DataLocationType.values());
		_dataLocTable.setComboBoxEditor(2, DataLocationType.values());
		_button = _dataLocTable.setButtonCellEditor(3);
		_button.setText(
				FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_HEADER_SET_FIELD).getText());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx     = GridBagConstraints.RELATIVE;
		gbc.gridy     = GridBagConstraints.RELATIVE;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx   = 1.0;
		gbc.weighty   = 1.0;
		gbc.anchor    = GridBagConstraints.NORTHWEST;
		gbc.fill      = GridBagConstraints.BOTH;
		gbc.insets    = RmaInsets.INSETS5505;
		add(_dataLocTable.getScrollPane(), gbc);
		
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		gbc.gridx     = GridBagConstraints.RELATIVE;
		gbc.gridy     = GridBagConstraints.RELATIVE;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx   = 1.0;
		gbc.weighty   = 0.0;
		gbc.anchor    = GridBagConstraints.NORTHWEST;
		gbc.fill      = GridBagConstraints.HORIZONTAL;
		gbc.insets    = RmaInsets.INSETS5555;
		add(buttonPanel, gbc);
		
		_addBtn = FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_BUTTON_ADD).createJButton();
				//new JButton("Add");
		gbc.gridx     = GridBagConstraints.RELATIVE;
		gbc.gridy     = GridBagConstraints.RELATIVE;
		gbc.gridwidth = 1;
		gbc.weightx   = 0.0;
		gbc.weighty   = 0.0;
		gbc.anchor    = GridBagConstraints.CENTER;
		gbc.fill      = GridBagConstraints.NONE;
		gbc.insets    = RmaInsets.INSETS5505;
		buttonPanel.add(_addBtn, gbc);
		
		_deleteBtn = FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_BUTTON_DELETE).createJButton();
				//new JButton("Delete");
		gbc.gridx     = GridBagConstraints.RELATIVE;
		gbc.gridy     = GridBagConstraints.RELATIVE;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx   = 0.0;
		gbc.weighty   = 0.0;
		gbc.anchor    = GridBagConstraints.CENTER;
		gbc.fill      = GridBagConstraints.NONE;
		gbc.insets    = RmaInsets.INSETS5505;
		buttonPanel.add(_deleteBtn, gbc);	
		
		tableRowSelected();
	}

	/**
	 * @param point
	 * @return
	 */
	protected String getTableToolTipText(Point point)
	{
		int row = _dataLocTable.rowAtPoint(point);
		if ( row < 0 )
		{
			return null;
		}
		DataLocation dl = _tableModel.getDataLocationAt(row);
		if ( dl == null )
		{
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("<html>");
		
		DataLocation linkedDl = dl.getLinkedToLocation();
		builder.append("<b>DSSPath:</b>");
		builder.append(dl.getDssPath());
		if ( linkedDl != null )
		{
			builder.append("<hr>");
			builder.append("<B>Linked To:</b>");
			builder.append(linkedDl.getName());
			builder.append("<br>");
			builder.append("<B>Pathname:</b>");
			builder.append(linkedDl.getDssPath());
		}
		builder.append("<html>");
		return builder.toString();
	}

	/**
	 * 
	 */
	private void addListeners()
	{
		_dataLocTable.getSelectionModel().addListSelectionListener(e->tableRowSelected());
		_addBtn.addActionListener(e->addLocationAction());
		_deleteBtn.addActionListener(e->deleteLocationAction());
		_button.addActionListener(e -> lauchUserDefinedCellEditor());
	}

	/**
	 * @return
	 */
	private void lauchUserDefinedCellEditor()
	{

		if (canLaunchUserDefinedDialog(_dataLocTable.getSelectedRows()))
		{
			DataLocationUserDefinedFieldEditor editor = new DataLocationUserDefinedFieldEditor(
					SwingUtilities.windowForComponent(this),
					new UserDefinedDataLocationFieldHelper(
							_tableModel.getDataLocationAt(_dataLocTable.getSelectedRows()[0])));
			editor.setVisible(true);
			setModified(true);
		}

	}

	private boolean canLaunchUserDefinedDialog(int[] rows)
	{
		if (rows == null || rows.length == 0)
		{
			return false;
		}
		else
		{
			DataLocation selectedDl = _tableModel.getDataLocationAt(rows[0]);
			return selectedDl != null && selectedDl.getUserSpecifiedFieldDefinitions().size() > 0;
		}
	}

	/**
	 * @return
	 */
	private void deleteLocationAction()
	{
		int[] rows = _dataLocTable.getSelectedRows();
		if ( rows == null || rows.length == 0 )
		{
			return;
		}
		String msg = FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_DELETE_LOC_MSG).getText();
				//"Do you want to delete the selected DataLocations?";
		String title =FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_DELETE_LOC_MSG_TITLE).getText();
				//"Confirm Deletion";
		int opt = JOptionPane.showConfirmDialog(this, msg, title, JOptionPane.YES_NO_OPTION);
		if ( opt != JOptionPane.YES_OPTION)
		{
			return;
		}
		for (int i = rows.length-1;i >= 0; i-- )
		{
			int r = rows[i];
			_dataLocTable.deleteRow(r);
		}
		setModified(true);
	}

	/**
	 * @return
	 */
	private void addLocationAction()
	{
		_dataLocTable.appendRow();
	}

	/**
	 * @return
	 */
	private void tableRowSelected()
	{
		int[] rows = _dataLocTable.getSelectedRows();
		_deleteBtn.setEnabled ( rows != null && rows.length > 0 );
	}

	/**
	 * @param inputDls
	 */
	public void setDataLocations(List<DataLocation> dataLocs)
	{
		_tableModel.setDataLocations(dataLocs);
	}
	
	public List<DataLocation> getDataLocations()
	{
		_dataLocTable.commitEdit(true);
		return _tableModel.getDataLocations();
	}
	/**
	 * @param modelAlt
	 */
	public void setModelAlternative(ModelAlternative modelAlt)
	{
		_modelAlt = modelAlt;
	}	
	
	public class DataLocationTableModel extends AbstractRmaTableModel
	{
		private List<DataLocation>_dataLocs = new ArrayList<>();
		private DSSPathname _pathname = new DSSPathname();
		/**
		 * @param headers
		 */
		public DataLocationTableModel(String[] headers)
		{
			super();
			m_columnNames = headers;
		}
	
		/**
		 * @param row
		 */
		public DataLocation getDataLocationAt(int row)
		{
			DataLocation dl = _dataLocs.get(row);
			if ( dl != null )
			{
				updateDlPathname(dl,_parentPanel.getTimeStep());
			} 
			return dl;
		}

		/**
		 * @return
		 */
		public List<DataLocation> getDataLocations()
		{
			List<DataLocation>dataLocs = new ArrayList<>();
			DataLocation dl;
			for(int i = 0;i < _dataLocs.size(); i++ )
			{
				dl =_dataLocs.get(i); 
				if ( dl.getName() == null || dl.getName().isEmpty() 
					|| dl.getParameter() == null || dl.getParameter().isEmpty())
				{
					continue;
				}
				updateDlPathname(dl, _parentPanel.getTimeStep());

				dataLocs.add((DataLocation) dl.clone());
			}
			return dataLocs;
		}

		/**
		 * @param dl
		 */
		private void updateDlPathname(DataLocation dl, String epart)
		{
			_pathname.setPathname(dl.getDssPath());
			_pathname.setBPart(dl.getName());
			_pathname.setCPart(dl.getParameter());
			if (epart != null )
			{
				_pathname.setEPart(epart);
			}
			String modelPart = PathnameUtilities.getWatFPartModelPart(_modelAlt);
			_pathname.setFPart("alt:ap:"+modelPart);
			dl.setDssPath(_pathname.getPathname());
			
		}

		public void setDataLocations(List<DataLocation>dataLocs)
		{
			_dataLocs.clear();
			if ( dataLocs != null )
			{
				for (int i = 0;i < dataLocs.size(); i++ )
				{
					_dataLocs.add((DataLocation) dataLocs.get(i).clone());
				}
			}
			fireTableDataChanged();
		}
		@Override
		public int getRowCount()
		{
			return _dataLocs.size();
		}
		@Override
		public void setValueAt(Object obj, int row, int col)
		{
			if ( row < 0 || row >= _dataLocs.size())
			{
				return;
			}
			if ( col < 0 || col >= getColumnCount() )
			{
				return;
			}
			if ( obj == null || obj.toString().isEmpty())
			{
				return;
			}
			DataLocation dl = _dataLocs.get(row);
			String str = obj.toString().trim();
			switch ( col )
			{
			case 0: // name
				boolean canSetName = true;
				for (DataLocation dlTest : _dataLocs)
				{
					if (!dl.getName().equals(dlTest.getName()) && dlTest.getName().equals(str))
					{
						JOptionPane.showMessageDialog(
								DataLocationPanel.this,
								"Duplicate name detected. Cannot set name to " + str,
								"Duplicate Name Error",
								JOptionPane.ERROR_MESSAGE);
						canSetName = false;
					}
				}
				if (canSetName)
				{
					dl.setName(str);
				}
					break;
				case 1: // param
					dl.setParameter(str);
					break;
				case 2: // data location type
					updateDataLocation(row, dl, str);
					fireTableCellUpdated(row, col + 1);
					break;
			}
			fireTableCellUpdated(row, col);
		}
		
		private void updateDataLocation(int row, DataLocation dl, String str)
		{
			DataLocationType type = DataLocationType.getDataLocationTypeForName(str);

			if (type != null && !type.equals(dl.getType()))
			{
				DataLocation newDl = DataLocationFactory.getDatalocationForType(type);
				newDl.setType(type);
				newDl.setModelAlternative(dl.getModelAlternative());
				newDl.setName(dl.getName());
				newDl.setParameter(dl.getParameter());
				_dataLocs.set(row, newDl);
			}
		}

		@Override
		public Object getValueAt(int row, int col)
		{
			if ( row < 0 || row >= _dataLocs.size())
			{
				return null;
			}
			if ( col < 0 || col >= getColumnCount() )
			{
				return null;
			}
			
			DataLocation dl = _dataLocs.get(row);
			switch ( col )
			{
				case 0: // name 
					return dl.getName();
				case 1: // param
					return dl.getParameter();
				case 2:
					return dl.getType();
				case 3:
					return FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_HEADER_SET_FIELD)
							.getText();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see rma.swing.table.AbstractRmaTableModel#addRow(java.util.Vector)
		 */
		@Override
		public void addRow(Vector newRow)
		{
			String name = getNextDlName();
			String param = FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_DEFAULT_PARAM).getText();
			DataLocation dl = new DataLocation(_modelAlt,name, param);
					//"flow");
			_dataLocs.add(dl);
		}

		/**
		 * @return
		 */
		private String getNextDlName()
		{
			int num = _dataLocs.size();
			if ( num == 0 )
			{
				num++;
			}
			String defName = FIRO_WFP_PluginI18n.getI18n(WfpMessages.DATALOCATION_PANEL_DEFAULT_NAME).getText();
			String name = defName+num;
			DataLocation dl;
			boolean notUnique = false;
			do
			{
				notUnique = false;
				for (int i = 0;i < _dataLocs.size(); i++ )
				{
					dl = _dataLocs.get(i);
					if ( name.equals(dl.getName()))
					{
						num++;
						name = defName+num;
						notUnique = true;
						break;
					}
				}
			}
			while(notUnique);
			
			return name;
		}

		/* (non-Javadoc)
		 * @see rma.swing.table.AbstractRmaTableModel#clearAll()
		 */
		@Override
		public void clearAll()
		{
			_dataLocs.clear();
			fireTableDataChanged();
		}

		/* (non-Javadoc)
		 * @see rma.swing.table.AbstractRmaTableModel#deleteRow(int)
		 */
		@Override
		public void deleteRow(int rowIndex)
		{
			if ( rowIndex < 0 || rowIndex >= _dataLocs.size())
			{
				return;
			}
			_dataLocs.remove(rowIndex);
			fireTableRowsDeleted(rowIndex, rowIndex);
		}
		
		@Override
		public boolean isCellEditable(int row, int col)
		{
			return !(col == 3 && !canLaunchUserDefinedDialog(new int[]
			{ row }));
		}
		
	}

	private class DataLocationTypeComboBoxEditor extends DefaultCellEditor
	{
		public DataLocationTypeComboBoxEditor()
		{
			super(new DataLocationTypeCombo());
		}

		@Override
		public
				Component
				getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
		{

			Component c = super.getTableCellEditorComponent(
					table,
					value,
					isSelected,
					row,
					column);

			if (c instanceof DataLocationTypeCombo && value instanceof DataLocationType)
			{
				// Select the current value
				((DataLocationTypeCombo) c).setSelectedItem(value);
			}

			return c;

		}

	}

	private class DataLocationTypeCombo extends JComboBox<DataLocationType>
	{
		public DataLocationTypeCombo()
		{
			super(new DefaultComboBoxModel<>(DataLocationFactory.getSupportedDataLocationTypes()));
		}
	}

	private class DataLocationTypeComboBoxRenderer extends DataLocationTypeCombo implements TableCellRenderer
	{

		public DataLocationTypeComboBoxRenderer()
		{
			super();
		}

		@Override
		public Component getTableCellRendererComponent(
				JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row,
				int column)
		{

			if (value != null)
			{
				// Select the current value
				setSelectedItem(value);
			}

			return this;
		}

	}

}
