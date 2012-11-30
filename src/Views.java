import java.awt.*;
import javax.swing.*; 
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.image.BufferedImage;

/**
 * Create the main GUI of our application
 * **/
public class Views implements MouseListener, TableModelListener, MouseMotionListener{

	/**
	 * Map declarations
	 * **/
	public static HashMap<String,MapStop> stopMap = new HashMap<String,MapStop>(70);
	public static final String IMAGE_PATH = "mbta.bmp";
	public static BufferedImage map;
	public static JLabel imageLabel;

	public static LinkedList<TrainLine> trainLines = new LinkedList<TrainLine>();
	private static LinkedList<Stop> stops;

	/**
	 * Table declarations
	 * **/
	public static JTable table;
	public static DefaultTableModel tableModel;
	// Column names for the list of trains
	public static String[] trainColumns = {"ID", "Line", "Location", "Destination"};
	// Column names for the list of stops
	public static String[] stopColumns = {"Line", "Name", "Stop ID"};
	public static boolean showTrains = true;
	public enum viewState {
		VIEWING_TRAINS,
		VIEWING_STOPS,
		VIEWING_ROUTE
	}

	int draggedAtX, draggedAtY;

	// Views constructor
	public Views(LinkedList<TrainLine> lines, LinkedList<Stop> s) {
		stops = s;
		trainLines = lines;
		createWindow(trainLines);
		pushHash();
		update();
	}

	// Sets the lines
	public void setLines(LinkedList<TrainLine> lines) {
		// Set the trainLines to the given list of lines
		trainLines = lines;
		// Update table and map
		update();
	}

	//creates the Window
	//NF
	public void createWindow(LinkedList<TrainLine> lines) {
		//Create and set up the window.
		JFrame frame = new JFrame("MBTA Trip Planner"); 
		frame.setBackground(new Color(100,100,100));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 

		//define all labels
		imageLabel = new JLabel();
		imageLabel.addMouseListener(this);
		imageLabel.addMouseMotionListener(this);
		JLabel depTime = new JLabel("Departure Time");
		JLabel arrTime = new JLabel("Arrival Time");

		SpinnerListModel hours1 = new SpinnerListModel(getTime(1,13));
		SpinnerListModel mins1 = new SpinnerListModel(getTime(1,60));
		SpinnerListModel hours2 = new SpinnerListModel(getTime(1,13));
		SpinnerListModel mins2 = new SpinnerListModel(getTime(1,60));
		JSpinner pickArrHour = new JSpinner(hours1);
		JSpinner pickDepHour = new JSpinner(hours2);
		JSpinner pickArrMin = new JSpinner(mins1);
		JSpinner pickDepMin = new JSpinner(mins2);


		/**
		 * The Buttons
		 * NF and AG
		 * **/
		// List Trains button
		JButton listTrains = new JButton("List Trains");
		listTrains.setBackground(new Color(230,230,230));
		listTrains.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTrains = true;
				update();
			}
		});

		// List Stops button
		JButton listStops = new JButton("List Stops");
		listStops.setBackground(new Color(230,230,230));
		listStops.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showTrains = false;
				update();
			}
		});

		// Test System button
		JButton testSystem = new JButton("Test System");
		testSystem.setBackground(new Color(230,230,230));

		// Add Stop button
		JButton addStop = new JButton("Add Stop");
		addStop.setBackground(new Color(230,230,230));

		// Calculate Route button
		JButton calcRoute = new JButton("Calculate Route");
		calcRoute.setBackground(new Color(230,230,230));



		String[] stops = {"stop a", "stop b", "stop c", "stop d", "stop e", "stop f"};

		JComboBox stopInfo = new JComboBox();
		JComboBox selectStop = new JComboBox();
		for(int i=0;i<stops.length;i++){
			stopInfo.addItem(stops[i]);
			selectStop.addItem(stops[i]);
		}

		JRadioButton earliestDep = new JRadioButton("Earliest Departures");
		earliestDep.setBackground(new Color(230,230,230));
		//birdButton.setSelected(true);

		JRadioButton fewestTrans = new JRadioButton("Fewest Transfers");
		fewestTrans.setBackground(new Color(220,220,220));
		//catButton.setActionCommand(catString);

		JRadioButton earliestArr = new JRadioButton("Earliest Arrival");
		earliestArr.setBackground(new Color(210,210,210));
		//dogButton.setActionCommand(dogString);

		ButtonGroup group = new ButtonGroup();
		group.add(earliestDep);
		group.add(fewestTrans);
		group.add(earliestArr);

		JRadioButton am1 = new JRadioButton("PM");
		fewestTrans.setBackground(new Color(220,220,220));

		JRadioButton pm1 = new JRadioButton("AM");
		earliestArr.setBackground(new Color(210,210,210));

		JRadioButton am2 = new JRadioButton("PM");
		fewestTrans.setBackground(new Color(220,220,220));

		JRadioButton pm2 = new JRadioButton("AM");
		earliestArr.setBackground(new Color(210,210,210));

		ButtonGroup ampm1 = new ButtonGroup();
		ampm1.add(am1);
		ampm1.add(pm1);

		ButtonGroup ampm2 = new ButtonGroup();
		ampm2.add(am2);
		ampm2.add(pm2);


		//define all checkboxes
		JCheckBox orderedList = new JCheckBox("Ordered List");
		orderedList.setBackground(new Color(230,230,230));

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		//Display the window
		//divide the window into 3 columns
		frame.setLayout(new GridLayout(1,3,5,0));
		//set window as visible
		frame.setVisible(true); 

		//create left, middle, right internal jframes
		JInternalFrame left = newFrame();
		JInternalFrame right = newFrame();
		JInternalFrame middle = newFrame();

		//set internal jframe layouts
		left.setLayout(gridbag);
		middle.setLayout(gridbag);
		//right.setLayout(new GridLayout(3,1,5,5));
		right.setLayout(gridbag);

		//////////////////////////////////////
		//set up layout for rightmost jframe
		JInternalFrame right1 = newFrame(2,new Color(230,230,230));
		right1.setMinimumSize(new Dimension(300,40));
		right1.setLayout(new FlowLayout());
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		c.weightx = 1.0;
		c.weighty = 0.0;
		gridbag.setConstraints(right1, c);
		right.add(right1);

		JInternalFrame right2 = newFrame(0, new Color(200,200,200));
		right2.setMinimumSize(new Dimension(300,40));
		right2.setLayout(new FlowLayout());
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		c.weightx = 0;
		c.weighty = 0;
		gridbag.setConstraints(right2, c);
		right.add(right2);

		JInternalFrame right3 = newFrame();
		right3.setMinimumSize(new Dimension(300,40));
		//right3.setLayout(new FlowLayout());
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right3, c);
		right.add(right3);

		JInternalFrame right4 = newFrame(0,new Color(200,200,200));
		right4.setMinimumSize(new Dimension(300,40));
		right4.setLayout(new FlowLayout());
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right4, c);
		right.add(right4);

		JInternalFrame right5 = newFrame(0,new Color(200,200,200));
		right5.setMinimumSize(new Dimension(300,40));
		right5.setLayout(new GridLayout(3,1,5,5));
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right5, c);
		right.add(right5);

		JInternalFrame right6 = newFrame();
		//right6.setMinimumSize(new Dimension(300,40));
		right6.setLayout(new GridLayout(1,2,5,5));
		c.weighty = 1;
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		gridbag.setConstraints(right6, c);
		right.add(right6);

		JInternalFrame lastLeft = newFrame(0,new Color(220,220,220));
		lastLeft.setLayout(new GridLayout(3,1,5,5));
		JInternalFrame lastRight = newFrame(0,new Color(220,220,220));
		lastRight.setLayout(new GridLayout(3,1,5,5));
		right6.add(lastLeft);
		right6.add(lastRight);
		JInternalFrame depFrame = newFrame(0,new Color(220,220,220));
		depFrame.setLayout(new GridLayout(1,2,5,5));
		JInternalFrame arrFrame = newFrame(0,new Color(220,220,220));
		arrFrame.setLayout(new GridLayout(1,2,5,5));
		JInternalFrame ampmFrame1 = newFrame(0,new Color(220,220,220));
		ampmFrame1.setLayout(new GridLayout(1,2,5,5));
		JInternalFrame ampmFrame2 = newFrame(0,new Color(220,220,220));
		ampmFrame2.setLayout(new GridLayout(1,2,5,5));
		///////////////////////////////////////

		//////////////////////////////////////
		//set up layout for LEFT jframe
		JInternalFrame topLeft = newFrame(2,new Color(230,230,230));
		topLeft.setLayout(new FlowLayout());
		topLeft.setMinimumSize(new Dimension(300,40));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		c.weighty = 0;
		gridbag.setConstraints(topLeft, c);
		left.add(topLeft);

		JInternalFrame middleLeft = newFrame();
		//middleLeft.setBackground(new Color(255,255,255));
		middleLeft.setLayout(new FlowLayout());
		//middleLeft.setMinimumSize(new Dimension(100,100));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weighty = 1;
		gridbag.setConstraints(middleLeft, c);
		left.add(middleLeft);

		JInternalFrame bottomLeft = newFrame(2,new Color(230,230,230));
		bottomLeft.setLayout(new FlowLayout());
		bottomLeft.setMinimumSize(new Dimension(300,40));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weighty = 0;
		gridbag.setConstraints(bottomLeft, c);
		left.add(bottomLeft);
		///////////////////////////////////////

		//////////////////////////////////////
		//set up layout for LEFT jframe
		JInternalFrame topMiddle = newFrame(2,new Color(230,230,230));
		//topMiddle.setLayout(new GridLayout(1,3,5,5));
		topMiddle.setLayout(new FlowLayout());
		topMiddle.setMinimumSize(new Dimension(300,40));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weightx = 1;
		c.weighty = 0;
		gridbag.setConstraints(topMiddle, c);
		middle.add(topMiddle);
		topMiddle.add(listTrains);
		topMiddle.add(listStops);
		topMiddle.add(stopInfo);

		JInternalFrame bottomMiddle = newFrame();
		//middleLeft.setBackground(new Color(255,255,255));
		//bottomMiddle.setLayout(new FlowLayout());
		bottomMiddle.setMinimumSize(new Dimension(300,40));
		//middleLeft.setMinimumSize(new Dimension(100,100));
		c.gridwidth = GridBagConstraints.REMAINDER; //end row
		//c2.gridheight = GridBagConstraints.RELATIVE;
		c.weighty = 1;
		gridbag.setConstraints(bottomMiddle, c);
		middle.add(bottomMiddle);
		///////////////////////////////////////

		//add to left jframe
		topLeft.add(testSystem);
		createMap();
		middleLeft.add(imageLabel);
		//bottomLeft.add(drawTrains);

		//add to middle jframe		 
		createTable(bottomMiddle, lines);
		stopsTable(right3);		 
		right1.add(orderedList);

		right2.add(addStop);
		right2.add(selectStop);
		right4.add(calcRoute);
		right5.add(earliestDep);
		right5.add(fewestTrans);
		right5.add(earliestArr);

		lastLeft.add(depTime);
		lastLeft.add(depFrame);
		depFrame.add(pickDepHour);
		depFrame.add(pickDepMin);
		lastLeft.add(ampmFrame1);
		ampmFrame1.add(am1);
		ampmFrame1.add(pm1);
		lastRight.add(arrTime);
		lastRight.add(arrFrame);
		arrFrame.add(pickArrHour);
		arrFrame.add(pickArrMin);
		lastRight.add(ampmFrame2);
		ampmFrame2.add(am2);
		ampmFrame2.add(pm2);

		//add internal jframes in order to fill the grid layout
		frame.add(left);
		frame.add(middle);
		frame.add(right);

		//pack the frames neatly		
		frame.pack();
		frame.setSize(1200,600);
	}

	/**
	 * Loads map
	 * @author NF
	 * **/
	public static void createMap() {
		try 
		{
			// Read from a file
			File FileToRead = new File(IMAGE_PATH);
			//Recognize file as image
			map = ImageIO.read(FileToRead);
			//Image pic = Picture.getScaledInstance(width, height, type);
			ImageIcon icon = new ImageIcon(map);
			//Show the image inside the label
			imageLabel.setIcon(icon);
		} 
		catch (Exception e) 
		{
			//Display a message if something goes wrong
			JOptionPane.showMessageDialog( null, e.toString() );
		}
	}

	//returns a new internal jframe without a toolbar
	// NF
	private static JInternalFrame newFrame(int borderWidth, Color c){
		JInternalFrame frame = new JInternalFrame("",false,false,false,false);
		frame.setBorder(BorderFactory.createLineBorder(new Color(150,150,150), borderWidth));
		frame.setBackground(c);
		javax.swing.plaf.InternalFrameUI ifu= frame.getUI(); 
		((javax.swing.plaf.basic.BasicInternalFrameUI)ifu).setNorthPane(null);
		frame.setVisible(true);
		frame.moveToFront();
		return frame;
	}
	private static JInternalFrame newFrame(){
		return newFrame(2,new Color(230,230,230));
	}
	//takes an internal jframe and create a table in it
	private void createTable(JInternalFrame container, LinkedList<TrainLine> lines){

		tableModel = new DefaultTableModel();
		tableModel.addTableModelListener(this);
		table = new JTable(tableModel);
		table.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		table.setShowVerticalLines(true);
		table.setSize(700,700);
		update();
		JScrollPane scrollPane = new JScrollPane(table); 
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//table.setFillsViewportHeight(true);

		container.add(table.getTableHeader(), BorderLayout.PAGE_START);
		container.add(scrollPane);
		//container.add(table);
	}

	/**
	 * Updates table and map based on lines and trains
	 * @author AG, CM and NF
	 **/
	public static void update() {
		// Table data
		Object[][] data;

		Graphics g = map.getGraphics();

		// If the trains should be shown
		if (showTrains) {
			// Number of trains
			int numTrains = 0;
			// Get the total number of trains
			for (TrainLine l : trainLines) {
				numTrains += l.getTrains().size();
			}

			// initialize length of data array
			data = new Object[numTrains][];
		}
		
		// If the stops should be shown
		else {
			// Set data array to size of the stops list
			data = new Object[stops.size()][];
			// Iterate through stops
			for (Stop s : stops) {
				// Create row to hold stop info
				Object[] row = { s.Line, s.stop_name, s.stopID };
				// Add row to data array
				data[stops.indexOf(s)] = row;
			}
		}

		// Counter for rows in data array
		int counter = 0;
		// iterate through lines
		for (TrainLine line : trainLines) {
			String lineName = line.getLine();
			// Get list of trains for each line
			LinkedList<Train> trains = line.getTrains();
			for (int t = 0; t < trains.size(); t++) {
				String posString;
				posString = trains.get(t).getTrainPredictions().get(0).getName();
				Object[] row = { trains.get(t).getTrainID(), lineName, 
						posString, trains.get(t).getTrainDestination() };

				// Draw trains on map
				if(stopMap.get(posString) != null){
					drawNode(stopMap.get(posString).x, stopMap.get(posString).y, g);
				}
				
				// Add trains to data array for table
				if (showTrains) {
					data[counter] = row;
					counter++;
				}
			}
		}

		// Set columns to trains or stops
		if (tableModel != null) {
			tableModel.setDataVector(data, (showTrains ? trainColumns : stopColumns));
		}

		// Dispose of graphics object
		g.dispose();
		// Invalidate the imageLabel
		imageLabel.repaint();
	}
	//linear interpolation between 2 floats and time
	public static Float LinearInterpolate(Float y1,Float y2,Float mu)
	{
		return(y1*(1-mu)+y2*mu);
	}


	//makes the stops table (to be refactored)
	//NF
	private static void stopsTable(JInternalFrame container){
		String[] columnNames = {"Stops"};

		String[][] data = {{"State Street"}, {"Gov't Center"}, {"Park Street"}, {"Harvard Ave"},
				{"State Street"}, {"Gov't Center"}, {"Park Street"}, {"Harvard Ave"},
				{"State Street"}, {"Gov't Center"}, {"Park Street"}, {"Harvard Ave"},
				{"State Street"}, {"Gov't Center"}, {"Park Street"}, {"Harvard Ave"}};
		JTable table = new JTable(data, columnNames);	
		table.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
		table.setShowVerticalLines(true);
		table.setSize(700,700);
		JScrollPane scrollPane = new JScrollPane(table); 
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		//table.setFillsViewportHeight(true);

		container.add(table.getTableHeader(), BorderLayout.PAGE_START);
		container.add(scrollPane);
		//container.add(table);
	}

	//scales the map on mouse click; scales in for left click, scales out for right click
	//NF + AG
	public void mouseClicked(MouseEvent e) {
		Graphics g = map.getGraphics();
		int button = e.getButton();		
		if (button == MouseEvent.BUTTON1) {
			/*
		    String name = JOptionPane.showInputDialog(null,
					  "What is your name?",
					  "Enter your name",
					  JOptionPane.QUESTION_MESSAGE);
			MapStop stop1 = new MapStop(e.getX(),e.getY());
			//stopMap.put(name, stop1);
			//drawNode(stopMap.get("Downtown Crossing").x,stopMap.get("Downtown Crossing").y,g);
			//System.out.println("stopMap.put('"+name+"',new MapStop("+e.getX()+","+e.getY()+"));");
			 */

			g.dispose();
			imageLabel.repaint();
			/*
			System.out.println("button 1");
			scaleX *= 1.5;
			scaleY *= 1.5;
			createMap(scaleX, scaleY, SCALE_TYPE);
			 */
		}

		/*
		else if (button == MouseEvent.BUTTON3) {
			System.out.println("button 3");
			scaleX /= 1.5;
			scaleY /= 1.5;
			createMap(scaleX, scaleY, SCALE_TYPE);
		}
		 */

	}
	public static void drawNode(int x, int y, Graphics g)
	{       g.setColor(Color.red);
	g.fillOval(x-9, y-9, 18, 18);
	}
	//sets the location of the map using the mouse coordinates
	//NF
	public void mouseDragged(MouseEvent e) {		
		imageLabel.setLocation(e.getX() - draggedAtX + imageLabel.getX(),
				e.getY() - draggedAtY + imageLabel.getY());		
	}

	public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	//obtains the current mouse coordinates upon mousePress
	//NF
	public void mousePressed(MouseEvent e) {

		if (e.getSource() == imageLabel) {
			draggedAtX = e.getX();
			draggedAtY = e.getY();
		} 

	}

	public static void pushHash(){
		stopMap.put("Oak Grove",new MapStop(799,77));
		stopMap.put("Malden",new MapStop(798,143));
		stopMap.put("Wellington",new MapStop(799,214));
		stopMap.put("Sullivan Square",new MapStop(799,298));
		stopMap.put("Community College",new MapStop(801,377));
		stopMap.put("North Station",new MapStop(813,471));
		stopMap.put("Chinatown",new MapStop(724,837));
		stopMap.put("NE Medical Center",new MapStop(684,872));
		stopMap.put("Back Bay",new MapStop(652,907));
		stopMap.put("Mass Ave",new MapStop(616,944));
		stopMap.put("Ruggles",new MapStop(579,979));
		stopMap.put("Roxbury Crossing",new MapStop(544,1011));
		stopMap.put("Jackson Square",new MapStop(506,1049));
		stopMap.put("Stony Brook",new MapStop(472,1085));
		stopMap.put("Green Street",new MapStop(433,1123));
		stopMap.put("Forest Hills",new MapStop(400,1158));
		stopMap.put("Downtown Crossing",new MapStop(798,761));
		stopMap.put("State",new MapStop(870,681));
		stopMap.put("Alewife",new MapStop(165,321));
		stopMap.put("Davis",new MapStop(253,321));
		stopMap.put("Porter",new MapStop(374,343));
		stopMap.put("Harvard",new MapStop(453,417));
		stopMap.put("Central",new MapStop(521,487));
		stopMap.put("Kendall/MIT",new MapStop(600,565));
		stopMap.put("Charles/MGH",new MapStop(672,638));
		stopMap.put("Park St",new MapStop(728,691));
		stopMap.put("South Station",new MapStop(882,846));
		stopMap.put("Broadway",new MapStop(901,942));
		stopMap.put("Andrew",new MapStop(899,1021));
		stopMap.put("JFK/UMass",new MapStop(901,1104));
		stopMap.put("North Quincy",new MapStop(1075,1422));
		stopMap.put("Wollaston",new MapStop(1147,1496));
		stopMap.put("Quincy Center",new MapStop(1222,1569));
		stopMap.put("Quincy Adams",new MapStop(1292,1639));
		stopMap.put("Braintree",new MapStop(1314,1794));
		stopMap.put("Bowdoin",new MapStop(746,565));
		stopMap.put("Government Center",new MapStop(798,619));
		stopMap.put("Aquarium",new MapStop(936,621));
		stopMap.put("Maverick",new MapStop(1052,505));
		stopMap.put("Airport",new MapStop(1111,448));
		stopMap.put("Wood Island",new MapStop(1161,399));
		stopMap.put("Orient Heights",new MapStop(1214,345));
		stopMap.put("Suffolk Downs",new MapStop(1267,292));
		stopMap.put("Beachmont",new MapStop(1320,238));
		stopMap.put("Revere Beach",new MapStop(1371,186));
		stopMap.put("Wonderland",new MapStop(1435,124));

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

		// TODO Auto-generated method stub

	}

	@Override
	public void tableChanged(TableModelEvent e) {
	}

	//returns an array of strings from min to max
	//NF
	public String[] getTime(int min, int max){
		String[] time = new String[max];
		for(int i =0; i<time.length;i++){
			time[i] = String.valueOf(i);
		}
		return time;
	}
} 