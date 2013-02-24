package com.netthreads.javafx.mavenize;

import java.util.LinkedList;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DataViewTest extends Application implements RefreshDataView
{
	private TableView<ServiceResult> dataTable = new TableView<ServiceResult>();
	
	private ObservableList<ServiceResult> observableList;
	
	private ResultService resultService;
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void start(Stage stage)
	{
		observableList = FXCollections.synchronizedObservableList(FXCollections.observableList(new LinkedList<ServiceResult>()));
		
		resultService = new ResultService(observableList, this);
		
		Button refreshBtn = new Button("Update");
		refreshBtn.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				resultService.reset();
				
				resultService.start();
			}
		});
		
		Button clearBtn = new Button("Clear");
		clearBtn.setOnAction(new EventHandler<ActionEvent>()
		{
			@Override
			public void handle(ActionEvent arg0)
			{
				observableList.clear();
			}
		});
		
		TableColumn<ServiceResult, String> nameCol = new TableColumn<ServiceResult, String>("Value");
		nameCol.setCellValueFactory(new PropertyValueFactory<ServiceResult, String>("value"));
		
		nameCol.setPrefWidth(200);
		
		dataTable.getColumns().setAll(nameCol);
		
		// productTable.getItems().addAll(products);
		dataTable.setItems(observableList);
		
		Scene scene = new Scene(new Group());
		stage.setTitle("Table View Sample");
		stage.setWidth(300);
		stage.setHeight(500);
		
		final VBox vOuterBox = new VBox();
		vOuterBox.setSpacing(5);
		
		final HBox hbox = new HBox();
		hbox.setSpacing(5);
		hbox.setPadding(new Insets(10, 0, 0, 10));
		hbox.getChildren().addAll(refreshBtn, clearBtn);
		
		final VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 0, 0, 10));
		vbox.getChildren().addAll(dataTable);
		
		vOuterBox.getChildren().addAll(hbox, vbox);
		
		((Group) scene.getRoot()).getChildren().addAll(vOuterBox);
		
		stage.setScene(scene);
		stage.show();
	}
	
	@Override
	public void refresh()
	{
		Platform.runLater(new Runnable()
		{
			public void run()
			{
				ObservableList<TableColumn<ServiceResult, ?>> columns = dataTable.getColumns();
				TableColumn<ServiceResult, ?> column = columns.get(0);
				column.setVisible(false);
				
				dataTable.getColumns().get(0).setVisible(true);
			}
		});
		
	}
}