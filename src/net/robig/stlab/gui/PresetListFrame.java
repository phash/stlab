package net.robig.stlab.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.Keymap;

import net.robig.logging.Logger;
import net.robig.stlab.StLab;
import net.robig.stlab.StLabConfig;
import net.robig.stlab.model.PresetList;
import net.robig.stlab.util.config.IntValue;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PresetListFrame extends JFrame {

	private static final Color FOREGROUND=new Color(187,154,77);
	
	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	private JScrollPane listScrollPane = null;
	private JTable presetList = null;
	private IDeviceController device=null;
	private DeviceFrame parent=null;
	PresetList list=null;
	Logger log = new Logger(this);
	IntValue width=null;
	IntValue height=null;
	IntValue x=null;
	IntValue y=null;
	boolean initialized=false;

	/**
	 * This is the default constructor
	 */
	public PresetListFrame(DeviceFrame mainWindow) {
		super();
		initializeGui();
		parent=mainWindow;
		device=mainWindow.getDeviceController();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initializeGui() {
		width=StLabConfig.getPresetListWindowWidth();
		height=StLabConfig.getPresetListWindowHeight();
		x=StLabConfig.getPresetListWindowX();
		y=StLabConfig.getPresetListWindowY();
//		this.setSize(width.getValue(),
//				height.getValue());
		this.setBounds(x.getValue(), y.getValue(), width.getValue(), height.getValue());
		this.setContentPane(getJContentPane());
		this.setTitle(StLab.applicationName+" Preset List");
		this.setName("Preset List");
		initListeners();
	}
	
	/**
	 * Fills the table initially with data from the device
	 */
	public void initializeData() {
		PresetList l=new PresetList();
		try {
			for(int i=0;i<device.getDeviceInfo().numPresets;i++){
				l.add(device.getPresetParameters(i));
			}
		}catch(Exception e){
			e.printStackTrace(log.getWarnPrintWriter());
		}
		setList(l);
		initialized=true;
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getListScrollPane(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes listScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getListScrollPane() {
		if (listScrollPane == null) {
			listScrollPane = new JScrollPane();
			listScrollPane.setViewportView(getPresetList());
		}
		return listScrollPane;
	}

	/**
	 * This method initializes presetList	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getPresetList() {
		if (presetList == null) {
			presetList = new JTable();
			presetList.setShowGrid(true);
			presetList.setBackground(new Color(44,45,48));
			presetList.setForeground(FOREGROUND);
			presetList.setSelectionBackground(Color.BLACK);
			presetList.setSelectionForeground(new Color(204,75,73));
			presetList.setGridColor(new Color(92,77,38));
			presetList.getTableHeader().setForeground(FOREGROUND);
			presetList.getTableHeader().setBackground(Color.BLACK);
			TableColumnModel colModel = presetList.getColumnModel();
	        for(int j = 0; j < colModel.getColumnCount(); j++)
	            colModel.getColumn(j).setCellRenderer(new RowRenderer());
	        presetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        presetList.setFocusable(true);
	        presetList.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		}
		return presetList;
	}

	public void setList(PresetList list){
		this.list=list;
		updateTable();
	}

	public void setSelectionIndex(int offset){
		if(!initialized) return;
		presetList.setRowSelectionInterval(offset, offset);
	}
	
	public int getSelectionIndex() {
		return presetList.getSelectedRow();
	}
	
	private void updateTable() {
		presetList.setModel(list);
		TableColumnModel colModel = presetList.getColumnModel();
        for(int j = 0; j < colModel.getColumnCount(); j++)
            colModel.getColumn(j).setCellRenderer(new RowRenderer());
	}
	
	class RowRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		public Component getTableCellRendererComponent(JTable table,
	                                                   Object value,
	                                                   boolean isSelected,
	                                                   boolean hasFocus,
	                                                   int row, int column) {
	        super.getTableCellRendererComponent(table, value, isSelected,
	                                            hasFocus, row, column);
	        setToolTipText(list.getCellInfo(row, column));
	        return this;
	    }
	}

	
	private void onChange() {
		int p=getSelectionIndex();
		log.info("Selected "+p);
		try {
			device.selectPreset(p);
		} catch (Exception e1) {
			e1.printStackTrace(log.getWarnPrintWriter());
		}
	}
	
	private void initListeners(){
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentMoved(ComponentEvent e) {
				x.setValue(getX());
				y.setValue(getY());
			}

			@Override
			public void componentResized(ComponentEvent e) {
				width.setValue(getWidth());
				height.setValue(getHeight());
			}
		});
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				parent.setPresetListVisible(false);
			}

			@Override
			public void windowClosing(WindowEvent e) {
				parent.setPresetListVisible(false);
			}
		});
		getPresetList().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				onChange();
			}
		});
		getPresetList().addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER){
					onChange();
				}else if(e.getKeyCode()==KeyEvent.VK_SPACE){
					parent.setPresetListVisible(false);
				}else if(e.getKeyCode()==KeyEvent.VK_TAB){
					log.debug("TAB pressed");
					parent.toFront();
					parent.requestFocus();
				}else if(e.getKeyCode()==0){
					log.warn("Unknown Keycode 0x00");
				}
				
			}
		});
		getPresetList().unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));
		//get(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER,0))
		InputMap inputMap = getPresetList().getInputMap(JComponent.WHEN_FOCUSED);
		KeyStroke enterKeyStroke=KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0);
		inputMap.remove(enterKeyStroke);
		inputMap.put(enterKeyStroke, "dummy");
		getPresetList().setInputMap(JComponent.WHEN_FOCUSED,inputMap);
		getPresetList().unregisterKeyboardAction(enterKeyStroke);
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
