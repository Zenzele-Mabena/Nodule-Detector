package LND.model;


import java.awt.image.RenderedImage;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.util.Base64;

import javax.imageio.ImageIO;



//import LND.File.ReportGen;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;




/**
 *This class is responsible for connecting to the server and giving the user functionality that is facilitated by the server
 * @author ZB MABENA 219026791
 * @version PX
 *@see LNDClient
 */
public class LNDClientPane extends BorderPane {
	
	
	private Socket connection;
	
	
	///
	private int possibleNodules;
	
	////Streams 
	private InputStream is;
	private OutputStream os;
	
	private BufferedReader br;

	private BufferedOutputStream bos;
	private DataOutputStream dos;
	
	///GUI 
	private Button btnSelectFile;
	private Button btnConnect;
	private Button btnDilate;
	private Button btnErode;
	private Button btnHighlight;
	private Button btnDiagnose;
	
	private Label lblDiagnosis;
	private Label lblPossibleNodule;
	private Text txtPossibleNodule;
	private Label lblNumberOfNodules;
	private Text txtNumberOfNodules;
	
	private RadioButton critical;
	private RadioButton alarming;
	private RadioButton insignificant;
	
	
	private Label lblReport;
	private Label lblName;
	private TextField txtName;
	private Label lblID;
	private TextField txtID;
	private Label lblDate;
	private TextField txtDate;
	private Button btnReport;
	
	private ImageView imvCTScan;
	private Image image;
	private TextArea taLog;
	
	/////Path
	private Path current;
	

	///URLS
	
	private String dilationURL = "/api/Dilation";
	private String erosionURL = "/api/Erosion";
	private String orbURL = "/api/ORB";
	private String orbFeaturesURL = "/api/ORBFeatures";
	
	////This counter helps with auto-connection after the non persistent connection is terminated by server for first time 
	private int connectionCounter =0;
	
	
	
	
	
	/**
	 * This is a constructor that takes in the stage on which the Client pane is displayed in 
	 * @param stage This stage will be passed to the file Chooser as a root  when the user wants to select a CT scan
	 */

	public LNDClientPane(Stage stage)
	{
		
		///private function for initializing the uI
		initPane();
		
		
		btnConnect.setOnAction(event -> {
		try {
			
			///making connection to server and binding all relevant streams
			this.connection = new Socket("localhost",5000);
			is = connection.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			
			os = connection.getOutputStream();
			bos = new BufferedOutputStream(os);
			dos = new DataOutputStream(bos);
			
			
			if(connectionCounter < 2) {
			this.taLog.appendText("Connection with API established.\n");
			}
			
			///increment counter to alert my other functions that they can first call connect button before making a request
			this.connectionCounter++;
			
		} catch (IOException ioe) {
			
			ioe.printStackTrace();
		}
		});
		
		
		this.btnSelectFile.setOnAction(event -> {
			
			try {
			FileChooser chooser = new FileChooser();
			File imageFile =  chooser.showOpenDialog(stage);
			
			
			if(imageFile.exists())
			{
			FileInputStream fis = new FileInputStream(imageFile);
			
			///setting current path so that i can read in the image again when i now want to alter the picture 
			this.current = imageFile.toPath();
			this.image = new Image(fis);
			this.imvCTScan.setImage(image);
			}
			}catch(FileNotFoundException fnfe)
			{
				fnfe.printStackTrace();
			}
			
			
		});
		
		
		this.btnDilate.setOnAction((event) -> {
			
		      String encodedFile = "";
		      try {
		    	  if(connectionCounter >0)
		    	  {
		    	  this.btnConnect.fire();
		    	  }
		  		///checking if a file has been chosen yet
		    	  
		    	  
		
					if(this.image != null)
					{
						
						//"data","CTScan1.jpg"
						
						System.out.println(current);
						File imageFile = new File(current.toString());
						if(imageFile.exists()) {
						FileInputStream fis = new FileInputStream(imageFile);
						
						byte[] bytes = new byte[(int)imageFile.length()];
						fis.read(bytes);
						encodedFile = new String(Base64.getEncoder().encodeToString(bytes));
						byte[] bytesToSend = encodedFile.getBytes();
					    fis.close();
						
						////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						///HTTP request 1
						dos.write(("POST "+dilationURL+ " HTTP/1.1\r\n").getBytes());
						dos.write(("Content-Type: "+"application/text\r\n").getBytes());
						dos.write(("Content-Length: " + encodedFile.length()+"\r\n").getBytes());
						dos.write(("\r\n").getBytes());	
						dos.write(bytesToSend);
						dos.flush();
						dos.write(("\r\n").getBytes());
						
						this.taLog.appendText("First request sent \n");
						
						String response = "";
						String line;
						while(!((line = br.readLine()).equals("")))
						{
							response += line +"\n";
						}
						System.out.println(response);
						
						////getting the image 
						String imgData = "";
						while((line = br.readLine())!= null)
						{
							imgData += line;
						}
						
						////displaying the altered image data received to the console
						System.out.println(imgData);
						
						////extraction of the string 
		                String base64String = imgData.substring(imgData.indexOf('\'')+1,imgData.lastIndexOf('}')-1);
						
						byte[] decodedString = Base64.getDecoder().decode(base64String);
						
						this.image = new Image(new ByteArrayInputStream(decodedString));
						this.imvCTScan.setImage(image);
						
						////i want to put the image back into the same file we read it from then assign the current path to point to this new image contained in the same file
						
						RenderedImage imageToWrite = SwingFXUtils.fromFXImage(image, null);
						
						ImageIO.write( imageToWrite, "jpg", imageFile);///all in one line ;)
						this.current = imageFile.toPath();
		    	  
		    	  
		    	  
		    	  
		    	  
				
						}}} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			
			
			
			
		});
		this.btnErode.setOnAction((event) -> {
			
		      String encodedFile = "";
		      try {
		    	  
		    	  ///this is to check whether we have already connected to the server before,,if so then we need to connect again because its non persistent
		    	  if(connectionCounter >0)
		    	  {
		    		  
		    		  ///calling btnConnect on click method
		    	  this.btnConnect.fire();
		    	  }
		  		///checking if a file has been chosen yet
					if(this.image != null)
					{
						
						//"data","CTScan1.jpg"
						
						System.out.println(current);
						File imageFile = new File(current.toString());
						if(imageFile.exists()) {
						FileInputStream fis = new FileInputStream(imageFile);
						
						byte[] bytes = new byte[(int)imageFile.length()];
						fis.read(bytes);
						encodedFile = new String(Base64.getEncoder().encodeToString(bytes));
						byte[] bytesToSend = encodedFile.getBytes();
					    fis.close();
						
						////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						///HTTP request 1
						dos.write(("POST "+erosionURL+ " HTTP/1.1\r\n").getBytes());
						dos.write(("Content-Type: "+"application/text\r\n").getBytes());
						dos.write(("Content-Length: " + encodedFile.length()+"\r\n").getBytes());
						dos.write(("\r\n").getBytes());	
						dos.write(bytesToSend);
						dos.flush();
						dos.write(("\r\n").getBytes());
						
						this.taLog.appendText("First request sent \n");
						
						String response = "";
						String line;
						while(!((line = br.readLine()).equals("")))
						{
							response += line +"\n";
						}
						System.out.println(response);
						
						////getting the image 
						String imgData = "";
						while((line = br.readLine())!= null)
						{
							imgData += line;
						}
						
						////displaying the altered image data recived to the console
						System.out.println(imgData);
						
						
		                String base64String = imgData.substring(imgData.indexOf('\'')+1,imgData.lastIndexOf('}')-1);
						
						byte[] decodedString = Base64.getDecoder().decode(base64String);
						
						this.image = new Image(new ByteArrayInputStream(decodedString));
						this.imvCTScan.setImage(image);
						
						////i want to put the image back into the same file we read it from then assign the current path to point to this new image contained in the same file
						
						
                        RenderedImage imageToWrite = SwingFXUtils.fromFXImage(image, null);
						
						ImageIO.write( imageToWrite, "jpg", imageFile);///all in one line ;)
						this.current = imageFile.toPath();
		    	  
		    	  
		    	  
		    	  
		    	  
				
						}}} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			
			
			
			
			
			
			
		});
		this.btnHighlight.setOnAction((event) -> {
			
		      String encodedFile = "";
		      try {
		    	  
		    	  
		    	  if(connectionCounter >0)
		    	  {
		    	  this.btnConnect.fire();
		    	  }
		  		///checking if a file has been chosen yet
					if(this.image != null)
					{
						
						//"data","CTScan1.jpg"
						
						System.out.println(current);
						File imageFile = new File(current.toString());
						if(imageFile.exists()) {
						FileInputStream fis = new FileInputStream(imageFile);
						
						byte[] bytes = new byte[(int)imageFile.length()];
						fis.read(bytes);
						encodedFile = new String(Base64.getEncoder().encodeToString(bytes));
						byte[] bytesToSend = encodedFile.getBytes();
					    fis.close();
						
						////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
						///HTTP request 1
						dos.write(("POST "+orbURL+ " HTTP/1.1\r\n").getBytes());
						dos.write(("Content-Type: "+"application/text\r\n").getBytes());
						dos.write(("Content-Length: " + encodedFile.length()+"\r\n").getBytes());
						dos.write(("\r\n").getBytes());	
						dos.write(bytesToSend);
						dos.flush();
						dos.write(("\r\n").getBytes());
						
						this.taLog.appendText("First request sent \n");
						
						String response = "";
						String line;
						while(!((line = br.readLine()).equals("")))
						{
							response += line +"\n";
						}
						System.out.println(response);
						
						////getting the image 
						String imgData = "";
						while((line = br.readLine())!= null)
						{
							imgData += line;
						}
						
						////displaying the altered image data received to the console
						System.out.println(imgData);
						
						
		                String base64String = imgData.substring(imgData.indexOf('\'')+1,imgData.lastIndexOf('}')-1);
						
						byte[] decodedString = Base64.getDecoder().decode(base64String);
						
						this.image = new Image(new ByteArrayInputStream(decodedString));
						this.imvCTScan.setImage(image);
						
						////i want to put the image back into the same file we read it from then assign the current path to point to this new image contained in the same file
						
						
                        RenderedImage imageToWrite = SwingFXUtils.fromFXImage(image, null);
						
						ImageIO.write( imageToWrite, "jpg", imageFile);///all in one line ;)
						this.current = imageFile.toPath();
		    	  
		    	  
		    	  
		    	  
		    	  
		    	  
				
						}}} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			
			
			
			
			
			
			
			
		});
		
		this.btnDiagnose.setOnAction(event -> {
			
			String encodedFile;
			
			  if(connectionCounter >0)
	    	  {
	    	  this.btnConnect.fire();
	    	  }
			try
			{
				///checking if a file has been chosen yet
				if(this.image != null)
				{
					
					//"data","CTScan1.jpg"
					
					System.out.println(current);
					File imageFile = new File(current.toString());
					if(imageFile.exists()) {
					FileInputStream fis = new FileInputStream(imageFile);
					
					byte[] bytes = new byte[(int)imageFile.length()];
					fis.read(bytes);
					encodedFile = new String(Base64.getEncoder().encodeToString(bytes));
					byte[] bytesToSend = encodedFile.getBytes();
				    fis.close();
					
					////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					///HTTP request 1
					dos.write(("POST "+orbFeaturesURL+ " HTTP/1.1\r\n").getBytes());
					dos.write(("Content-Type: "+"application/text\r\n").getBytes());
					dos.write(("Content-Length: " + encodedFile.length()+"\r\n").getBytes());
					dos.write(("\r\n").getBytes());	
					dos.write(bytesToSend);
					dos.flush();
					dos.write(("\r\n").getBytes());
					
					this.taLog.appendText("First request sent \n");
					
					String response = "";
					String line;
					while(!((line = br.readLine()).equals("")))
					{
						response += line +"\n";
					}
					System.out.println(response);
					
					////getting the image 
					String features = "";
					while(!((line = br.readLine())!= null))
					{
						features += line;
					}
					
					////displaying the altered image data s to the console
					System.out.println(features);
					String number = features.substring(features.indexOf("[")+1, features.indexOf("]"));
					
					this.possibleNodules = Integer.valueOf(number);
					if(possibleNodules > 250)
					{
						txtPossibleNodule.setFill(Color.RED);
						txtPossibleNodule.setText("TRUE.Perform further testing with patient");
						txtNumberOfNodules.setText(number);
					}
					else {
						
						txtPossibleNodule.setFill(Color.LIME);
						txtPossibleNodule.setText("FALSE.If no large nodules are evident in the patient's CT scan then patient might not have any nodules");
						txtNumberOfNodules.setText(number);
						
					}
					System.out.println("This now implies that we diagnosed the image");
					
					RenderedImage imageToWrite = SwingFXUtils.fromFXImage(image, null);
					
					ImageIO.write( imageToWrite, "jpg", imageFile);///all in one line ;)
					this.current = imageFile.toPath();
	    	  
					
					
					
					}
					
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
			
			
			
			
		});
		
	
			
		
		this.btnReport.setOnAction(event -> {
			
			if(this.alarming.isSelected())
			{
				this.insignificant.setSelected(false);
				this.critical.setSelected(false);
				
			}
			if(this.critical.isSelected())
			{
				this.insignificant.setSelected(false);
				this.alarming.setSelected(false);
				
			}
			if(this.insignificant.isSelected()) {
				this.alarming.setSelected(false);
				this.critical.setSelected(false);
			}
				
	
			
			
			if(this.critical.isSelected()) {
				//ReportGen.generateReport(this.txtName.getText(),this.txtID.getText() , this.txtDate.getText(), 'c');
				taLog.appendText("Report Generated!");
			}
			if(this.insignificant.isSelected())
			{
				//ReportGen.generateReport(this.txtName.getText(),this.txtID.getText() , this.txtDate.getText(),'i' );
				taLog.appendText("Report Generated!");
			}
			if(this.alarming.isSelected())
			{
				//ReportGen.generateReport(this.txtName.getText(),this.txtID.getText() , this.txtDate.getText(),'a' );
				taLog.appendText("Report Generated!");
			}
			
			
		});
		
		
		
	}
	
	
	/**
	 * This is a private helper function that initializes the user interface for the pane
	 */
	
	private void initPane()
	{
		////initialize components
		
		///top
		 this.btnSelectFile = new Button("Select CT Scan");
		 this.btnConnect = new Button("Connect");
		 this.btnDiagnose = new Button("Diagnose");
		 btnDiagnose.setStyle("-fx-background-color: #53abd7");
		  this.btnDilate =new Button("Dilate Scan");
			this.btnErode = new Button("Erode Scan");
		this.btnHighlight = new Button("Highlight Feartures");
		 
		 HBox topBox = new HBox();
		 topBox.setSpacing(7);
		 topBox.setStyle("-fx-background-color: BLACK");
		 topBox.setPadding(new Insets(15,12,15,12));
		 topBox.getChildren().addAll(btnConnect,btnSelectFile,btnDilate,btnErode,btnHighlight,btnDiagnose);
		 
		
		 this.setTop(topBox);
		 
		 /*
		  *  box1.setPadding(new Insets(15,12,15,12));
    	 box1.setSpacing(10);
    	 box1.setStyle("-fx-background-color: #338877");
    	 box1.getChildren().addAll(lblUserName,txtUserName,lblPassword,pwfPassword,btnLogin);
		  */
		 
		 
		 
		 
		 
		 VBox leftBox = new VBox();
		 leftBox.setSpacing(10);
		 leftBox.setStyle("-fx-background-color: #53abd7");
		 leftBox.setPadding(new Insets(15,12,15,12));
		 lblDiagnosis = new Label("Diagnosis");
		 Separator s1 = new Separator(Orientation.HORIZONTAL);
		 
		 lblPossibleNodule = new Label("Possible Nodules");
		 txtPossibleNodule = new Text();
		 
		 lblNumberOfNodules = new Label("Number of possible nodules");
		 txtNumberOfNodules = new Text();
		
		 critical = new RadioButton("Critical");
		 alarming = new RadioButton("Alarming");
		 insignificant = new RadioButton("Insignificant");
		 
		 HBox subBox = new HBox();
		 subBox.setSpacing(50);
		 subBox.setStyle("-fx-background-color:WHITE");
		 subBox.getChildren().addAll(critical,alarming,insignificant);
		 
		 
		 Separator s2 = new Separator(Orientation.HORIZONTAL);
		
		
		 this.lblReport = new Label("Report");
		 lblName = new Label("Patient Name :");
		 txtName = new TextField("Enter Patient Name");
		 lblID = new Label("Patient ID :");
		 txtID = new TextField("Enter patient's official national ID number");
		 lblDate = new Label("Date :");
		 txtDate = new TextField("eg. 2021/10/04");
		 btnReport = new Button("Generate report");
		 btnReport.setStyle("-fx-background-color: BLACK");
		 btnReport.setTextFill(Color.WHITE);
		 
		 
		 Label lblLog = new Label("Log");
		 this.taLog = new TextArea();
		
		 VBox logBox = new VBox();
		 logBox.setSpacing(20);
		 logBox.setStyle("-fx-background-color : BLACK");
		 logBox.getChildren().addAll(lblLog,taLog);
		 this.setBottom(logBox);
		 
		 Separator s3 = new Separator(Orientation.HORIZONTAL);
		 Separator s4 = new Separator(Orientation.HORIZONTAL);
		 
		 leftBox.getChildren().addAll(s3,lblDiagnosis,s1,lblPossibleNodule,txtPossibleNodule,lblNumberOfNodules,txtNumberOfNodules,
				 subBox,s4,lblReport,s2,lblName,txtName,lblID,txtID,lblDate,txtDate,btnReport);
		 this.setLeft(leftBox);
		 
		
		 imvCTScan = new ImageView();
		 this.setCenter(imvCTScan);
		

	}

}
