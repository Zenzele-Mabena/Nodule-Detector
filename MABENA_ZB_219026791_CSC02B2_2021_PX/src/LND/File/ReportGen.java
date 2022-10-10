package LND.File;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


/**
 * This class is responsible for generating reports for patients that are diagnosed use the Nodule Detector
 * @author ZB MABENA 219026791
 *@version PX
 */
public class ReportGen {
	
	
	
	public static void generateReport(String name,String id,String date,char state)
	{
		////write to the file based on the information 
		System.out.println("Report being generated");
		
		File report = new File("data/"+name+"("+date+")");
		
		try {
			PrintWriter writer = new PrintWriter(report);
			String text = new String("             REPORT OF : "+name +" "+id+ "\n");
			writer.write(text);
			text = "Date : "+date +"\n";
			writer.append(text);
			switch (state) {
			case 'c':
				writer.append(name +" was found to have very clearly visible nodules appearing and is in a very CRITICAL stage \n");
				writer.append("Patient should return for further test within the next week");
				break;
				
			case 'i':
				writer.append(name +" was has  no clearly visible nodules and if nodules are present then they INSIGNIFICANT based on size \n");
				writer.append("Patient should return for further test within the next 3 months");
				break;
				
			case 'a':
				writer.append(name +" was found to have  visible nodules appearing.The spotted nodules are ALARMING but not yet criitical \n");
				writer.append("Patient should return for further test within the next month");
				break;

			default:
				break;
			}
			
			writer.close();
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
