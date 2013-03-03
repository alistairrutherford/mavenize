/**
 * -----------------------------------------------------------------------
 * (c) - Alistair Rutherford - www.netthreads.co.uk - March 2013
 * -----------------------------------------------------------------------
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 * -----------------------------------------------------------------------
 */
package com.netthreads.javafx.mavenize.controller;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netthreads.javafx.mavenize.app.ApplicationMessages;
import com.netthreads.javafx.mavenize.app.AssertHelper;
import com.netthreads.javafx.mavenize.client.MavenizeClient;
import com.netthreads.javafx.mavenize.model.ProjectResult;
import com.netthreads.mavenize.pom.PomGenerator;

/**
 * MavenizeFX Dialog controller.
 * 
 */
public class MavenizeFXController implements Initializable, ImplementsRefresh
{
	private Logger logger = LoggerFactory.getLogger(MavenizeFXController.class);

	//private static final String TEST_SOURCE = "C:\\temp\\test\\source";
	//private static final String TEST_TARGET = "C:\\temp\\test\\dest";

	@FXML
	private TextField sourceInput;

	@FXML
	private TextField targetInput;

	@FXML
	private TextField versionInput;

	@FXML
	private ComboBox<String> packageCombo;

	@FXML
	private Button sourceButton;

	@FXML
	private Button targetButton;

	@FXML
	private Button activateButton;

	@FXML
	private Label versionLabel;

	@FXML
	private Hyperlink weblinkLabel;

	@FXML
	private TableView<ProjectResult> dataTable;

	private DirectoryChooser directoryChooser;

	// Model list
	private LinkedList<ProjectResult> list;
	private ObservableList<ProjectResult> observableList;

	private MavenizeClient mavenizeClient;

	private Stage stage;

	/**
	 * Construct controller.
	 * 
	 */
	public MavenizeFXController()
	{
		directoryChooser = new DirectoryChooser();

		// Create observable list.
		list = new LinkedList<ProjectResult>();

		observableList = FXCollections.synchronizedObservableList(FXCollections.observableList(list));
	}

	/**
	 * Initialise controller.
	 * 
	 */
	@Override
	public void initialize(URL url, ResourceBundle rsrcs)
	{
		assert sourceInput != null : AssertHelper.fxmlInsertionError("sourceInput");
		assert targetInput != null : AssertHelper.fxmlInsertionError("targetInput");
		assert sourceButton != null : AssertHelper.fxmlInsertionError("sourceButton");
		assert targetButton != null : AssertHelper.fxmlInsertionError("targetButton");
		assert dataTable != null : AssertHelper.fxmlInsertionError("dataTable");
		assert versionInput != null : AssertHelper.fxmlInsertionError("versionInput");
		assert packageCombo != null : AssertHelper.fxmlInsertionError("packageCombo");
		assert versionLabel != null : AssertHelper.fxmlInsertionError("versionLabel");
		assert weblinkLabel != null : AssertHelper.fxmlInsertionError("weblinkLabel");

		logger.debug("initialize");

		InputStream stream = getClass().getResourceAsStream("/control_play_blue.png");

		// Go button graphic.
		Image goImage = new Image(stream);
		activateButton.setGraphic(new ImageView(goImage));

		// Assemble data table.
		buildDataTable(dataTable);

		// Assemble package types.
		buildPackageTypes(packageCombo);

		versionInput.setText(PomGenerator.DEFAULT_VERSION);

		versionLabel.setText(ApplicationMessages.APP_TITLE_TEXT + " - " + ApplicationMessages.APP_VERSION_TEXT);

		weblinkLabel.setText(ApplicationMessages.URL_TEXT);

		/**
		 * TEST
		 */
//		sourceInput.setText(TEST_SOURCE);
//		targetInput.setText(TEST_TARGET);
	}

	/**
	 * Build list of package types.
	 * 
	 * @param packageCombo
	 */
	private void buildPackageTypes(ComboBox<String> packageCombo)
	{
		ObservableList<String> items = packageCombo.getItems();

		items.clear();
		for (String packageType : PomGenerator.PACKAGE_TYPES)
		{
			items.add(packageType);
		}

		packageCombo.setValue(PomGenerator.PACKAGE_TYPES[0]);
	}

	/**
	 * Source Button Action Handler.
	 * 
	 * @param event
	 */
	public void sourceButtonAction(ActionEvent event)
	{
		logger.debug("sourceButtonAction");

		Window window = getWindow(sourceButton);

		if (window != null)
		{
			File directory = directoryChooser.showDialog(window);

			if (directory != null)
			{
				sourceInput.setText(directory.getPath());
			}
		}
	}

	/**
	 * Target Button Action Handler.
	 * 
	 * @param event
	 */
	public void targetButtonAction(ActionEvent event)
	{
		logger.debug("targetButtonAction");

		Window window = getWindow(targetButton);

		if (window != null)
		{
			File directory = directoryChooser.showDialog(window);

			if (directory != null)
			{
				targetInput.setText(directory.getPath());
			}
		}

	}

	/**
	 * Activate Button Action Handler.
	 * 
	 * @param event
	 */
	public void activateButtonAction(ActionEvent event)
	{
		logger.debug("activateButtonAction");

		String sourcePath = sourceInput.getText();
		String targetPath = targetInput.getText();
		String versionText = versionInput.getText();

		if (sourcePath == null || sourcePath.isEmpty())
		{
			// Alert
			Alert alert = new Alert(stage, ApplicationMessages.MSG_ERROR_INVALID_SOURCE);

			alert.showAndWait();
		}
		else if (targetPath == null || targetPath.isEmpty())
		{
			// alert
			Alert alert = new Alert(stage, ApplicationMessages.MSG_ERROR_INVALID_TARGET);

			alert.showAndWait();
		}
		else if (versionText == null || versionText.isEmpty())
		{
			// alert
			Alert alert = new Alert(stage, ApplicationMessages.MSG_ERROR_INVALID_VERSION);

			alert.showAndWait();
		}
		else if (sourcePath.equals(targetPath))
		{
			Alert alert = new Alert(stage, ApplicationMessages.MSG_ERROR_INVALID_PATHS);

			alert.showAndWait();
		}
		else
		{
			// Wait for
			mavenizeClient.process(sourceInput.getText(), targetInput.getText(), versionInput.getText(), packageCombo.getValue());
		}
	}

	/**
	 * Build table columns.
	 * 
	 * @param dataTable
	 */
	@SuppressWarnings("unchecked")
	private void buildDataTable(TableView<ProjectResult> dataTable)
	{
		// ---------------------------------------------------------------
		// Build columns.
		// ---------------------------------------------------------------

		// GroupId
		TableColumn<ProjectResult, String> groupIdCol = new TableColumn<ProjectResult, String>(ProjectResult.TITLE_GROUP_ID);

		groupIdCol.setCellValueFactory(new PropertyValueFactory<ProjectResult, String>(ProjectResult.ATTR_GROUP_ID));

		// ArtifactId
		TableColumn<ProjectResult, String> artifactIdCol = new TableColumn<ProjectResult, String>(ProjectResult.TITLE_ARTIFACT_ID);

		artifactIdCol.setCellValueFactory(new PropertyValueFactory<ProjectResult, String>(ProjectResult.ATTR_ARTIFACT_ID));

		// FilePath
		TableColumn<ProjectResult, String> filePathCol = new TableColumn<ProjectResult, String>(ProjectResult.TITLE_FILE_PATH);

		filePathCol.setCellValueFactory(new PropertyValueFactory<ProjectResult, String>(ProjectResult.ATTR_FILE_PATH));

		// File count
		TableColumn<ProjectResult, Integer> fileCountCol = new TableColumn<ProjectResult, Integer>(ProjectResult.TITLE_FILE_COUNT);

		fileCountCol.setCellValueFactory(new PropertyValueFactory<ProjectResult, Integer>(ProjectResult.ATTR_FILE_COUNT));

		// Status text
		TableColumn<ProjectResult, String> statusCol = new TableColumn<ProjectResult, String>(ProjectResult.TITLE_STATUS);

		statusCol.setCellValueFactory(new PropertyValueFactory<ProjectResult, String>(ProjectResult.ATTR_STATUS));

		// Working indicator
		TableColumn<ProjectResult, Integer> workingCol = new TableColumn<ProjectResult, Integer>(ProjectResult.TITLE_WORKING);
		workingCol.setCellValueFactory(new PropertyValueFactory<ProjectResult, Integer>(ProjectResult.ATTR_WORKING));

		// Custom Cell factory converts index to image.
		workingCol.setCellFactory(new Callback<TableColumn<ProjectResult, Integer>, TableCell<ProjectResult, Integer>>()
		{
			@Override
			public TableCell<ProjectResult, Integer> call(TableColumn<ProjectResult, Integer> item)
			{
				WorkingTableCell cell = new WorkingTableCell();

				return cell;
			}
		});

		// ---------------------------------------------------------------
		// Set widths and bind to data table width.
		// ---------------------------------------------------------------
		groupIdCol.prefWidthProperty().bind(dataTable.widthProperty().divide(6));
		artifactIdCol.prefWidthProperty().bind(dataTable.widthProperty().divide(6));
		filePathCol.prefWidthProperty().bind(dataTable.widthProperty().divide(3));
		fileCountCol.prefWidthProperty().bind(dataTable.widthProperty().divide(9));
		statusCol.prefWidthProperty().bind(dataTable.widthProperty().divide(10));
		workingCol.prefWidthProperty().bind(dataTable.widthProperty().divide(10));

		// ---------------------------------------------------------------
		// Add columns.
		// ---------------------------------------------------------------
		dataTable.getColumns().setAll(groupIdCol, artifactIdCol, filePathCol, fileCountCol, statusCol, workingCol);

		// ---------------------------------------------------------------
		// Assign list
		// ---------------------------------------------------------------
		dataTable.setItems(observableList);
	}

	/**
	 * Get window from node.
	 * 
	 * @param node
	 * 
	 * @return The node Window.
	 */
	private Window getWindow(Node node)
	{
		Window window = null;

		Scene scene = node.getScene();

		if (scene != null)
		{
			window = scene.getWindow();
		}

		return window;
	}

	/**
	 * Controller client act as an intermediary between the workers and the UI controller.
	 * 
	 * @param mavenizeClient
	 */
	public void setClient(MavenizeClient mavenizeClient)
	{
		this.mavenizeClient = mavenizeClient;

		// Have a level of indirection here but we could have bound straight to
		// the service.
		activateButton.disableProperty().bind(mavenizeClient.getActiveProperty());
	}

	/**
	 * Return list object.
	 * 
	 * @return The list object.
	 */
	public ObservableList<ProjectResult> getObservableList()
	{
		return observableList;
	}

	/**
	 * Return source path.
	 * 
	 * @return The source path.
	 */
	public TextField getSourceInput()
	{
		return sourceInput;
	}

	/**
	 * Return target path.
	 * 
	 * @return The source path.
	 */
	public TextField getTargetInput()
	{
		return targetInput;
	}

	/**
	 * Assign stage.
	 * 
	 * @param stage
	 */
	public void setStage(Stage stage)
	{
		this.stage = stage;
	}

	/**
	 * Trick to force data table refresh.
	 * 
	 */
	@Override
	public void refresh()
	{
		Platform.runLater(new Runnable()
		{
			public void run()
			{
				ObservableList<TableColumn<ProjectResult, ?>> columns = dataTable.getColumns();
				TableColumn<ProjectResult, ?> column = columns.get(0);

				if (column != null)
				{
					column.setVisible(false);
					column.setVisible(true);
				}
			}
		});

	}

}
