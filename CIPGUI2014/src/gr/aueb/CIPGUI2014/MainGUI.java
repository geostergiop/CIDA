package gr.aueb.CIPGUI2014;

import java.awt.Event;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;


import org.eclipse.swt.widgets.Combo;

import com.tinkerpop.blueprints.impls.neo4j.Neo4jVertex;


public class MainGUI {
	private static Table cis;
	private static Table connections;
	private static Text inputSubsector;
	private static Text inputSubstation;
	private static Text inputLatitude;
	private static Text inputLongtitude;
	private static Combo dropdownSectors, inputIsInit;
	private static App run = null;
	private static Combo sourceCI;
	private static Combo destCI;
	private static Combo connectionType;
	private static Button btnAddConnection;
	private static Button btnClearAll;
	private static Button btnAddCi;
	
	private static String vertexExists = null;
	private static boolean edgeExists = false;
	private static boolean enableAllInput = false;
	private static Text edgeImpact;
	private static Text edgeLikelihood;

	public static void main(String[] args) {
		
		/*************************	GUI STUFF	*****************************/
		Display display = new Display();
        Shell cipGui = new Shell(display);
        cipGui.setText("CIP Graph Calculator v0.1");
        cipGui.setSize(556, 562);
        cipGui.setLayout(new GridLayout(1, false));
        
        Composite inputControls = new Composite(cipGui, SWT.NONE);
        GridData gd_inputControls = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_inputControls.heightHint = 227;
        gd_inputControls.widthHint = 530;
        inputControls.setLayoutData(gd_inputControls);
        
        Label label = new Label(inputControls, SWT.SEPARATOR | SWT.VERTICAL);
        label.setBounds(279, 0, 2, 196);
        
        Label label_1 = new Label(inputControls, SWT.SEPARATOR | SWT.HORIZONTAL);
        label_1.setBounds(0, 194, 530, 2);
        
        Label lblCiSector = new Label(inputControls, SWT.NONE);
        lblCiSector.setBounds(0, 56, 93, 15);
        lblCiSector.setText("Choose a Sector:");
        
        Label lblEnterASector = new Label(inputControls, SWT.NONE);
        lblEnterASector.setText("Enter a Subsector:");
        lblEnterASector.setBounds(0, 85, 93, 15);
        
        inputSubsector = new Text(inputControls, SWT.BORDER);
        inputSubsector.setEnabled(false);
        inputSubsector.setBounds(149, 82, 124, 21);
        
        inputSubstation = new Text(inputControls, SWT.BORDER);
        inputSubstation.setEnabled(false);
        inputSubstation.setBounds(149, 109, 124, 21);
        
        Label lblEnterSubstationName = new Label(inputControls, SWT.NONE);
        lblEnterSubstationName.setText("Enter Substation Name:");
        lblEnterSubstationName.setBounds(0, 112, 143, 15);
        
        inputLatitude = new Text(inputControls, SWT.BORDER);
        inputLatitude.setEnabled(false);
        inputLatitude.setBounds(149, 136, 124, 21);
        
        inputLongtitude = new Text(inputControls, SWT.BORDER);
        inputLongtitude.setEnabled(false);
        inputLongtitude.setBounds(149, 163, 124, 21);
        
        Label lblEnterLocationlatitude = new Label(inputControls, SWT.NONE);
        lblEnterLocationlatitude.setText("Enter location (latitude):");
        lblEnterLocationlatitude.setBounds(0, 139, 143, 15);
        
        Label lblEnterLocationlongtitude = new Label(inputControls, SWT.NONE);
        lblEnterLocationlongtitude.setText("Enter location (longtitude):");
        lblEnterLocationlongtitude.setBounds(0, 166, 143, 15);
        
        Composite tablesWithInsertedStuff = new Composite(cipGui, SWT.NONE);
        GridData gd_tablesWithInsertedStuff = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_tablesWithInsertedStuff.widthHint = 530;
        gd_tablesWithInsertedStuff.heightHint = 269;
        tablesWithInsertedStuff.setLayoutData(gd_tablesWithInsertedStuff);
        
        cis = new Table(tablesWithInsertedStuff, SWT.BORDER | SWT.FULL_SELECTION);
        cis.setBounds(0, 0, 256, 234);
        cis.setHeaderVisible(true);
        cis.setLinesVisible(true);
        
        connections = new Table(tablesWithInsertedStuff, SWT.BORDER | SWT.FULL_SELECTION);
        connections.setBounds(274, 0, 256, 234);
        connections.setHeaderVisible(true);
        connections.setLinesVisible(true);
        TableItem item = new TableItem(connections, SWT.NONE);
        item.setText(new String[] {"Column1 text","Column2 text"});
        connections.addListener(SWT.Selection, new Listener() {
        	@Override
        	public void handleEvent(org.eclipse.swt.widgets.Event e) {
        		if (e.detail == SWT.CHECK) {
        			System.out.println("You checked " + e.item);
        		} else {
        			System.out.println("You selected " + e.item);
        		}
        	}
        });
        
        dropdownSectors = new Combo(inputControls, SWT.NONE);
        dropdownSectors.setEnabled(false);
        dropdownSectors.setBounds(149, 53, 124, 23);
        dropdownSectors.add("Chemical");
        dropdownSectors.add("Commercial Facilities");
        dropdownSectors.add("Communications");
        dropdownSectors.add("Critical Manufacturing");
        dropdownSectors.add("Dams");
        dropdownSectors.add("Defense Industrial Base");
        dropdownSectors.add("Emergency Services");
        dropdownSectors.add("Energy");
        dropdownSectors.add("Financial Services");
        dropdownSectors.add("Food and Agriculture");
        dropdownSectors.add("Government Facilities");
        dropdownSectors.add("Healthcare and Public Health");
        dropdownSectors.add("Information Technology");
        dropdownSectors.add("Nuclear Reactors, Materials, and Waste");
        dropdownSectors.add("Transportation Systems");
        dropdownSectors.add("Water and Wastewater Systems");
        dropdownSectors.setText("Choose a Sector..");
        
        sourceCI = new Combo(inputControls, SWT.NONE);
        sourceCI.setEnabled(false);
        sourceCI.setBounds(420, 53, 110, 23);
        sourceCI.setText("Source C.I.");
        
        destCI = new Combo(inputControls, SWT.NONE);
        destCI.setEnabled(false);
        destCI.setBounds(420, 80, 110, 23);
        destCI.setText("Destination C.I.");
        
        Label lblChooseASource = new Label(inputControls, SWT.NONE);
        lblChooseASource.setText("Choose a Source C.I.:");
        lblChooseASource.setBounds(287, 56, 127, 15);
        
        Label label_2 = new Label(inputControls, SWT.NONE);
        label_2.setText("Choose a Source C.I.:");
        label_2.setBounds(287, 85, 127, 15);
        
        edgeImpact = new Text(inputControls, SWT.BORDER);
        edgeImpact.setEnabled(false);
        edgeImpact.setBounds(420, 109, 110, 21);
        
        edgeLikelihood = new Text(inputControls, SWT.BORDER);
        edgeLikelihood.setEnabled(false);
        edgeLikelihood.setBounds(420, 136, 110, 21);
        
        Label lblEnterTheLikelihood = new Label(inputControls, SWT.NONE);
        lblEnterTheLikelihood.setText("Impact (double) :");
        lblEnterTheLikelihood.setBounds(287, 115, 127, 15);
        
        Label lblLikelihooddouble = new Label(inputControls, SWT.NONE);
        lblLikelihooddouble.setText("Likelihood (double) :");
        lblLikelihooddouble.setBounds(287, 142, 127, 15);
        
        
        /********************************************
         * BUTTON: Add Connections between CIs		*
         ********************************************/       
        btnAddConnection = new Button(inputControls, SWT.NONE);
        btnAddConnection.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		// If graph not initialized, throw error.
        		if(run != null) {
        			int s = sourceCI.getSelectionIndex();
        			int d = destCI.getSelectionIndex();
        			int t = connectionType.getSelectionIndex();
        			// If user selection is empty, throw error.
        			if (s != -1 && d != -1 && t != -1) {
	        			String sCI = sourceCI.getItem(s);
	        			String dCI = destCI.getItem(d);
	        			String connType = connectionType.getItem(t);
	        			try {
		        			double impact = Double.parseDouble(edgeImpact.getText());
		        			double likelihood = Double.parseDouble(edgeLikelihood.getText());
		        			// Check Impact and Likelihood range values and throw error if incorrect
		        			if(impact<1.0 || impact > 5.0 || likelihood < 0.0 || likelihood > 1.0)
		        				throw new NumberFormatException();
		        			
		        			// Try to connect the two Vertices in the graph
		        			if(!run.addEdgeToCIs(sCI, dCI, connType, impact, likelihood)) {
		        				edgeExists = false;
		        				JOptionPane.showMessageDialog(null, "ERROR: Chosen C.I.s cannot be connected.", 
		        						"ERROR", JOptionPane.ERROR_MESSAGE);
		        			}
		        			else {
		        				edgeExists = true;
		        				JOptionPane.showMessageDialog(null, "Connection created.", 
		        						"NOTICE", JOptionPane.INFORMATION_MESSAGE);
		        			}
		        			
	        			}catch (NumberFormatException n) {
	        				JOptionPane.showMessageDialog(null, "ERROR: Coordinates must be double numbers.\n"
	        						+ "Impact range: 1.0 - 5.0\nLikelihood range: 0.0 - 1.0", 
	        						"ERROR", JOptionPane.ERROR_MESSAGE);
	        			}
        			}else
        				JOptionPane.showMessageDialog(null, "ERROR: Please choose a C.I.", 
        						"ERROR", JOptionPane.ERROR_MESSAGE);
        		}else
        			JOptionPane.showMessageDialog(null, "ERROR: You must initialize a graph first.", 
        					"ERROR", JOptionPane.ERROR_MESSAGE);
        	}
        });
        btnAddConnection.setEnabled(false);
        btnAddConnection.setBounds(420, 202, 110, 25);
        btnAddConnection.setText("ADD Connection");
        
                
        /********************************************
         * BUTTON: Create a new graph from scratch	*
         ********************************************/
        Button btnGenerateNewGraph = new Button(tablesWithInsertedStuff, SWT.NONE);
        btnGenerateNewGraph.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		boolean doit = true;
        		
        		if (run != null){
        			int reply = JOptionPane.showConfirmDialog(null, "A graph is already loaded. Discard a create a new one?", 
        					"Notification", JOptionPane.OK_CANCEL_OPTION);
        			if (reply == JOptionPane.NO_OPTION)
        				doit = false;
        			else {
        				run.resetGraphAndDatabase();
        				sourceCI.removeAll();
        				destCI.removeAll();
        				sourceCI.setEnabled(false);
            			destCI.setEnabled(false);
            			connectionType.setEnabled(false);
            			edgeImpact.setEnabled(false);
            			edgeLikelihood.setEnabled(false);
        			}
        		}
        		if (doit) {
	        		String path = JOptionPane.showInputDialog(null, "Creating a new graph. Enter directory name:", "Notification", JOptionPane.INFORMATION_MESSAGE);
	        		run = new App();
	    			//Create new graph item
	        		if (run.newGraph(path))
	        			enableAllInput = true;
        		}
        	}
        });
        btnGenerateNewGraph.setBounds(0, 244, 130, 25);
        btnGenerateNewGraph.setText("Generate New Graph");
        
        Button btnLoadGraph = new Button(tablesWithInsertedStuff, SWT.NONE);
        btnLoadGraph.setBounds(400, 244, 130, 25);
        btnLoadGraph.setText("Load Graph");
        
        
        /********************************
         * BUTTON: Load existing graph	*
         ********************************/
        Button btnLoadExistingGraph = new Button(tablesWithInsertedStuff, SWT.NONE);
        btnLoadExistingGraph.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		
        		String path = JOptionPane.showInputDialog(null, "Enter directory from which to load graph:", "Notification", JOptionPane.INFORMATION_MESSAGE);
        		run = new App();
    			//Create new graph item
        		if (run.loadGraph(path)) {
        			enableAllInput = true;
        			vertexExists = "";
        			edgeExists = true;
        			//clear lists and re-populate from existing graph
        			sourceCI.removeAll();
        			destCI.removeAll();
        			sourceCI.setEnabled(true);
        			destCI.setEnabled(true);
        			connectionType.setEnabled(true);
        			TreeMap<String, Neo4jVertex> CIs = run.getCIs();
        			for(Map.Entry<String,Neo4jVertex> entry1 : CIs.entrySet()) {
        				Neo4jVertex vertex = entry1.getValue();
        				sourceCI.add(vertex.getProperty("CI_ID")+"|"+vertex.getProperty("substation_id"));
        				destCI.add(vertex.getProperty("CI_ID")+"|"+vertex.getProperty("substation_id"));
        			}
        		}
        	}
        });
        btnLoadExistingGraph.setBounds(130, 244, 126, 25);
        btnLoadExistingGraph.setText("Load existing Graph");
        
        
        /********************************************************************
         * BUTTON: Run analysis of current graph ONLY if all steps are OK.	*
         ********************************************************************/
        Button btnRunGraphAnalysis = new Button(tablesWithInsertedStuff, SWT.NONE);
        btnRunGraphAnalysis.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		if (vertexExists != null && edgeExists) {
        			if (!run.startGraphAnalysis())
        				JOptionPane.showMessageDialog(null, "ERROR: Graph could not be analyzed", "ERROR", JOptionPane.ERROR_MESSAGE);
        		}else
        			JOptionPane.showMessageDialog(null, "You must first add C.I. nodes and Connections (edges)\n"
        					+ "before analysing the graph", "Data not inserted", JOptionPane.ERROR_MESSAGE);
        	}
        });
        btnRunGraphAnalysis.setBounds(274, 244, 124, 25);
        btnRunGraphAnalysis.setText("Run Graph Analysis");
        
        
        /****************************************
         * BUTTON: Add Vertex (C.I.) to graph	*
         ****************************************/
        btnAddCi = new Button(inputControls, SWT.NONE);
        btnAddCi.setEnabled(false);
        btnAddCi.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {      		
        		// Check input data are complete
        		if(checkInputIsOk() && run != null) {
        			String strIsInit = inputIsInit.getText();
        			boolean isInit = false;
        			if (strIsInit.equalsIgnoreCase("YES"))
        				isInit = true;
        			else
        				isInit = false;
        			// Add a new C.I. Vertex to graph with all input data provided by the user
        			vertexExists = run.addVertexToGraph(dropdownSectors.getItem(dropdownSectors.getSelectionIndex()),
        					inputSubsector.getText(), inputSubstation.getText(), 
        					inputLatitude.getText(), inputLongtitude.getText(),
        					isInit);
        		}
        		// Prompt user with popup-messages accordingly.
        		if (vertexExists == null || vertexExists.isEmpty())
        			JOptionPane.showMessageDialog(null, "ERROR: Invalid data inserted for Vertex.", "Node not inserted", JOptionPane.ERROR_MESSAGE);
        		else {
        			// Since a Node does exist in the program, enable the "Insert Connection" functionality
        			JOptionPane.showMessageDialog(null, "C.I. Node inserted to current graph", "Success", JOptionPane.INFORMATION_MESSAGE);
        			sourceCI.add(vertexExists);
        			destCI.add(vertexExists);
        			btnAddConnection.setEnabled(true);
        			sourceCI.setEnabled(true);
        			destCI.setEnabled(true);
        			edgeImpact.setEnabled(true);
        			edgeLikelihood.setEnabled(true);
        			connectionType.setEnabled(true);
        		}
        	}
        });
        btnAddCi.setBounds(0, 202, 110, 25);
        btnAddCi.setText("ADD C.I.");
        
        
        /************************************
         * BUTTON: Clear all input by user	*
         ************************************/
        btnClearAll = new Button(inputControls, SWT.NONE);
        btnClearAll.setEnabled(false);
        btnClearAll.setBounds(116, 202, 110, 25);
        btnClearAll.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        	}
        });
        btnClearAll.setText("CLEAR ALL");
        
        Label lblTypeOfConnection = new Label(inputControls, SWT.NONE);
        lblTypeOfConnection.setText("Type of connection :");
        lblTypeOfConnection.setBounds(287, 166, 127, 15);
        
        connectionType = new Combo(inputControls, SWT.NONE);
        connectionType.setEnabled(false);
        connectionType.setBounds(420, 163, 110, 23);
        connectionType.setText("Type..");
        connectionType.add("PHYSICAL");
        connectionType.add("SOCIAL");
        connectionType.add("INFORMATIONAL");
        
        Label lblCurrentCiCan = new Label(inputControls, SWT.NONE);
        lblCurrentCiCan.setText("C.I. initiates cascading effects:");
        lblCurrentCiCan.setBounds(0, 29, 165, 15);
        
        inputIsInit = new Combo(inputControls, SWT.NONE);
        inputIsInit.setEnabled(false);
        inputIsInit.setBounds(180, 24, 93, 23);
        inputIsInit.setText("Choose..");
        inputIsInit.add("YES");
        inputIsInit.add("NO");
        
        cipGui.pack();
        cipGui.open();
        /*************************	GUI STUFF	*****************************/
        
        // While application Window is active and working, keep listening
        while (!cipGui.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
            if (!cipGui.isDisposed() && enableAllInput) {
            	enableButtons(true);
            }else if(!cipGui.isDisposed() && !enableAllInput)
            	enableButtons(false);
        }
        // End program execution when widget is disposed.
        System.exit(0);
	}
	
	
	
	private static void enableButtons(boolean ok) {
		if(ok) {
			dropdownSectors.setEnabled(true);
			inputIsInit.setEnabled(true);
        	inputSubsector.setEnabled(true);
        	inputSubstation.setEnabled(true);
        	inputLatitude.setEnabled(true);
        	inputLongtitude.setEnabled(true);
        	btnAddCi.setEnabled(true);
		}else {
			dropdownSectors.setEnabled(false);
			inputIsInit.setEnabled(false);
        	inputSubsector.setEnabled(false);
        	inputSubstation.setEnabled(false);
        	inputLatitude.setEnabled(false);
        	inputLongtitude.setEnabled(false);
        	btnAddCi.setEnabled(false);
		}
	}
	
	
	
	/* 
	 * Checks to help acknowledge whether all input fields are filled
	 */
	private static boolean checkInputIsOk() {
		
		if ((dropdownSectors.getSelectionIndex() != -1) && (inputSubsector.getText() != null) && (inputSubstation.getText() != null) && 
				(inputLatitude.getText() != null) && (inputLongtitude.getText() != null) && (inputIsInit.getSelectionIndex() != -1))
			return true;
		else
			JOptionPane.showMessageDialog(null, "Please fill all fields before saving a Node.", "Input Error", JOptionPane.ERROR_MESSAGE);
		
		return false;
	}
}




class SWTDropdownSelectionListener extends SelectionAdapter {

	private ToolItem dropdown;

	private Menu menu;

	public SWTDropdownSelectionListener(ToolItem dropdown) {
		this.dropdown = dropdown;
		menu = new Menu(dropdown.getParent().getShell());
	}

	public void add(String item) {
		MenuItem menuItem = new MenuItem(menu, SWT.NONE);
		menuItem.setText(item);
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				MenuItem selected = (MenuItem) event.widget;
				dropdown.setText(selected.getText());
			}
		});
	}
	@Override
	public void widgetSelected(SelectionEvent event) {
		if (event.detail == SWT.ARROW) {
			ToolItem item = (ToolItem) event.widget;
			Rectangle rect = item.getBounds();
			Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
			menu.setLocation(pt.x, pt.y + rect.height);
			menu.setVisible(true);
		} else {
			System.out.println(dropdown.getText() + " Pressed");
		}
	}
}
