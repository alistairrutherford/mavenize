/**
 * (c) - Alistair Rutherford - www.netthreads.co.uk
 */
package com.netthreads.javafx.mavenize;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.netthreads.javafx.mavenize.client.MavenizeClient;
import com.netthreads.javafx.mavenize.controller.MavenizeFXController;

/**
 * Mavenize tool GUI.
 * 
 */
public class MavenizeFX extends Application
{
	public static final String APPLICATION_TITLE = "Mavenize";
	public static final String FXML_FILE = "/mavenizeui.fxml";
	public static final String CSS_FILE = "/mavenizeui";
	
	public static final String ID_ROOT = "root";
	
	private MavenizeClient mavenizeClient;
	private MavenizeFXController mavenizeFXController;
	
	/**
	 * Load layout and display.
	 * 
	 */
	@Override
	public void start(Stage stage) throws Exception
	{
		// ---------------------------------------------------------------
		// Resources
		// ---------------------------------------------------------------
		URL layoutURL = getClass().getResource(FXML_FILE);
		URL cssURL = loadCSS(CSS_FILE);
		
		// ---------------------------------------------------------------
		// Load view.
		// ---------------------------------------------------------------
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(layoutURL);
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		
		Parent root = (Parent) fxmlLoader.load(layoutURL.openStream());
		root.setId(ID_ROOT);
		
		// ---------------------------------------------------------------
		// Get controller and assign model object
		// ---------------------------------------------------------------
		mavenizeFXController = fxmlLoader.getController();
		
		// ---------------------------------------------------------------
		// Client/Model
		// ---------------------------------------------------------------
		mavenizeClient = new MavenizeClient(mavenizeFXController.getObservableList(), mavenizeFXController);
		
		// ---------------------------------------------------------------
		// Controller
		// ---------------------------------------------------------------
		mavenizeFXController.setClient(mavenizeClient);
		mavenizeFXController.setStage(stage);
		
		// View.
		stage.setTitle(APPLICATION_TITLE);
		
		// Scene
		Scene scene = new Scene(root, 780, 400);
		
		scene.getStylesheets().addAll(cssURL.toExternalForm());
		
		stage.setScene(scene);
		
		stage.show();
	}
	
	/**
	 * JavaFx binary encodes CSS so check for both.
	 * 
	 * @param resource
	 * 
	 * @return The URL.
	 */
	private URL loadCSS(String resource)
	{
		URL cssURL = getClass().getResource(resource + ".css");
		if (cssURL == null)
		{
			cssURL = getClass().getResource(resource + ".bss");
		}
		
		return cssURL;
	}
	
	/**
	 * Main method.
	 * 
	 * @param args
	 *            Command line arguments
	 */
	public static void main(String[] args)
	{
		Application.launch(args);
	}
	
}
