package com.starcom.app;

import java.io.File;
import java.util.List;

import javafx.application.Application;
import javafx.concurrent.Task;
//import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ImageTagger extends Application
{
  public static void main(String[] args)
  {
    Application.launch(ImageTagger.class, args);
  }

  @Override
  /** This method is called when initialization is complete **/
  public void init() {}

  @Override
  public void start(Stage stage) throws Exception
  {
    showMainTagger(stage, getParameters(), false);
  }

  private static void showMainTagger(Stage stage, Application.Parameters pars, boolean b_force_shot) throws Exception
  {
    FXMLLoader fxmlLoader = new FXMLLoader(ImageTagger.class.getResource("ImageTagger.fxml"));
    Parent root = (Parent)fxmlLoader.load();
    ImageTaggerFrame frame = fxmlLoader.getController();
    stage.setTitle("### JBlitzPaint ImageTagger ###");
    stage.setScene(new Scene(root, 500, 400));
    stage.getIcons().add(new Image(ImageTagger.class.getResourceAsStream("icons/video_display.png")));
    frame.onShowPre(stage);
    stage.show();
  }

}
