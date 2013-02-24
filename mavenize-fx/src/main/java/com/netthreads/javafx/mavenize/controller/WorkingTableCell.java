package com.netthreads.javafx.mavenize.controller;

import java.io.InputStream;

import javafx.scene.control.TableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import com.netthreads.javafx.mavenize.app.ApplicationStyles;
import com.netthreads.javafx.mavenize.model.ProjectResult;

/**
 * Working status custom cell.
 * 
 */
public class WorkingTableCell extends TableCell<ProjectResult, Integer>
{
	private String[] ICONS =
	{
	        "/bullet_white.png", "/bullet_red.png", "/bullet_green.png"
	};
	
	private ImageView imageView;
	private HBox hBox;
	
	/**
	 * Construct cell image holder.
	 * 
	 */
	public WorkingTableCell()
	{
		imageView = new ImageView();
		
		hBox = new HBox();
		hBox.getChildren().add(imageView);
		hBox.getStyleClass().add(ApplicationStyles.STYLE_WORKING_STATUS_CELL);
	}
	
	/**
	 * This will take the value and lookup the appropriate icon for display in
	 * the cell.
	 */
	@Override
	protected void updateItem(Integer item, boolean empty)
	{
		super.updateItem(item, empty);
		
		if (!empty)
		{
			if (item < ICONS.length)
			{
				String iconName = ICONS[item];
				
				InputStream stream = getClass().getResourceAsStream(iconName);
				Image goImage = new Image(stream);
				
				imageView.setImage(goImage);
				
				setGraphic(hBox);
			}
		}
	}
	
}
