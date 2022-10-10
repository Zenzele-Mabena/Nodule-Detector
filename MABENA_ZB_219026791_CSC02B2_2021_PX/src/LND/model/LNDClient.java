package LND.model;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
 * This class is solely responsible launching the client pane and displaying it
 * @author ZB MABENA 219026791
 * @version PX
 *
 */
public class LNDClient extends Application {

	public static void main(String[] args)  {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		LNDClientPane root = new LNDClientPane(primaryStage);
		Scene mainScene = new Scene(root,600,600);
		primaryStage.setScene(mainScene);
		primaryStage.setTitle("Nodule Detector 1.0");
		primaryStage.show();
		
	}

}
