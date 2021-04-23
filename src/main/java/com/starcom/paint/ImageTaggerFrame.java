package com.starcom.paint;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.starcom.paint.tools.ITool;
import com.starcom.paint.tools.ITool.EventType;
import com.starcom.paint.tools.SizeTool;
import com.starcom.pix.ImageTagger;
import com.starcom.system.ClipboardTool;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.ColorInput;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;

import static com.starcom.debug.LoggingSystem.*;
import java.util.Optional;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;

public class ImageTaggerFrame
{
  enum MenuType {Load, Save, Settings};
  enum ToolBarType {Canvas, Paint};
  public static String ARROW_TOOL = "ArrowTool";
  public static String CROP_TOOL = "CropTool";
  public static Color color = Color.RED;
  @FXML private Pane pixpane;
  @FXML private Pane txtpane;
  @FXML private Pane pixlistpane;
  @FXML private ProgressBar progressBar;
//  @FXML private ScrollPane scrollPane;
//  @FXML private GridPane toolbar_paint;
//  @FXML private GridPane toolbar_canvas;
  private Stage stage;
  private ContextMenu contextMenu;
  private Node lastSelectedToolButton;
  ArrayList<CheckMenuItem> toolBarList;
  private ClipboardTool clipTool = new ClipboardTool();
  HashMap<String,ITool> tools = new HashMap<String,ITool>();
  boolean isDrag = false;
  ITool currentTool;
  //DropShadow dropShadowFx = new DropShadow( 20, Color.AQUA );
  //BoxBlur dropShadowFx = new BoxBlur();
  InnerShadow dropShadowFx = new InnerShadow(50, Color.AQUA);
  Glow glowFx = new Glow();
  static String changedValue = null;
  static String changedFile = null;

  @FXML public void initialize()
  {
//    toolbar_paint.managedProperty().bind(toolbar_paint.visibleProperty());
//    toolbar_canvas.managedProperty().bind(toolbar_canvas.visibleProperty());
//    toolbar_canvas.setVisible(false);
//    pane.setOnMouseMoved((ev) -> currentTool.handle(EventType.MOVE, ev)); // Not used yet!
    //pane.setOnMousePressed((ev) -> currentTool.handle(EventType.CLICK, ev));
    //pane.setOnMouseReleased((ev) -> currentTool.handle(EventType.RELEASE, ev));
    //pane.setOnMouseDragged((ev) -> currentTool.handle(EventType.DRAG, ev));
    //scrollPane.setOnKeyPressed((ev) -> onKey(ev));
    //pane.setStyle("-fx-border-color: black");
  }

  public void onShowPre(Stage stage)
  {
    this.stage = stage;
  }
  
//  private void onKey(KeyEvent ev)
//  {
//    if (ev.getCode() == KeyCode.DELETE)
//    {
//      PaintObject.clearFocusObject(pane);
//    }
//  }

//  private void clipChildren()
//  {
//    int arc = 3;
//    final Rectangle outputClip = new Rectangle();
//    outputClip.setArcWidth(arc);
//    outputClip.setArcHeight(arc);
//    pane.setClip(outputClip);
//    pane.layoutBoundsProperty().addListener((ov, oldValue, newValue) ->
//    {
//      outputClip.setWidth(newValue.getWidth());
//      outputClip.setHeight(newValue.getHeight());
//    });
//  }
  
//  @FXML void selectTool(ActionEvent event)
//  {
//    Object source = event.getSource();
//    if (source instanceof Node)
//    {
//      PaintObject.clearGizmos(pane);
//      Node sourceN = (Node) source;
//      changeButtonActive(lastSelectedToolButton, false);
//      lastSelectedToolButton = sourceN;
//      changeButtonActive(lastSelectedToolButton, true);
//      String id = sourceN.getId();
//      id = id.substring(0,1).toUpperCase() + id.substring(1);
//      selectTool(id);
//      System.out.println("Tool selected: " + sourceN.getId());
//    }
//    else
//    {
//      throw new IllegalStateException("Selected from unknown source: " + source);
//    }
//  }
  
  @FXML void selectAction(ActionEvent event)
  {
    Object source = event.getSource();
    if (source instanceof Node)
    {
      Node sourceN = (Node) source;
      if (sourceN.getId().equals("saveBut"))
      {
        showMenu(sourceN, MenuType.Save);
      }
      else if (sourceN.getId().equals("loadBut"))
      {
        showMenu(sourceN, MenuType.Load);
      }
    }
    else
    {
      throw new IllegalStateException("Selected from unknown source: " + source);
    }
  }
  
  void showMenu(Node sourceN, MenuType type)
  {
    if (contextMenu != null && contextMenu.isShowing())
    {
      contextMenu.hide();
    }
    contextMenu = new ContextMenu();
    if (type == MenuType.Save)
    {
//      MenuItem m_clip = new MenuItem("To Clip");
      MenuItem m_file = new MenuItem("To File");
      m_file.setOnAction((event) -> loadSaveFile(true));
      MenuItem m_meta = new MenuItem("Save Metadata");
      m_meta.setOnAction((event) -> saveMetadata());
//      m_clip.setOnAction((event) -> loadSaveClip(true));
//      contextMenu.getItems().addAll(m_clip, m_file);
      contextMenu.getItems().addAll(m_file, m_meta);
    }
    else if (type == MenuType.Load)
    {
//      MenuItem m_clip = new MenuItem("From Clip");
      MenuItem m_file = new MenuItem("From File");
      MenuItem m_folder = new MenuItem("From Folder");
//      MenuItem m_scr = new MenuItem("Screenshot");
//      MenuItem m_empty = new MenuItem("Empty");
      m_file.setOnAction((event) -> loadSaveFile(false));
//      m_scr.setOnAction((event) -> loadScrShot());
      m_folder.setOnAction((event) -> loadFolder());
//      m_clip.setOnAction((event) -> loadSaveClip(false));
//      m_empty.setOnAction((event) -> openEmptyPix(pane, 400, 200));
//      contextMenu.getItems().addAll(m_clip, m_file, m_scr, m_empty);
      contextMenu.getItems().addAll(m_file, m_folder);
    }
//    else // Settings
//    {
//      initToolbarList();
//      SeparatorMenuItem sep1 = new SeparatorMenuItem();
//      SeparatorMenuItem sep2 = new SeparatorMenuItem();
//      MenuItem m_color = new MenuItem("Set color");
//      CheckMenuItem toolBar_paint = toolBarList.get(0);
//      CheckMenuItem toolBar_canvas =  toolBarList.get(1);
//      MenuItem m_about = new MenuItem("About");
//      m_color.setOnAction((event) -> selectColor(sourceN));
//      m_about.setOnAction((event) -> showAbout(sourceN));
//      contextMenu.getItems().addAll(m_color, sep1, toolBar_paint, toolBar_canvas, sep2, m_about);
//    }
    Point pos = MouseInfo.getPointerInfo().getLocation();
    contextMenu.show(sourceN, pos.x, pos.y);
  }

//  private void initToolbarList()
//  {
//    if (toolBarList == null)
//    {
//      toolBarList = new ArrayList<CheckMenuItem>();
//      CheckMenuItem toolBar_paint = new CheckMenuItem("ToolBar: Paint");
//      toolBar_paint.setSelected(true);
//      toolBar_paint.setOnAction((event) -> selectToolbar(toolBar_paint, toolBarList, ToolBarType.Paint));
//      CheckMenuItem toolBar_canvas = new CheckMenuItem("ToolBar: Canvas");
//      toolBar_canvas.setOnAction((event) -> selectToolbar(toolBar_canvas, toolBarList, ToolBarType.Canvas));
//      toolBarList.add(toolBar_paint);
//      toolBarList.add(toolBar_canvas);
//    }
//  }

//  private void selectToolbar(CheckMenuItem toolBar, ArrayList<CheckMenuItem> toolBarList, ToolBarType type)
//  {
//    for (CheckMenuItem curItem : toolBarList) { curItem.setSelected(false); }
//    toolBar.setSelected(true);
//    if (type == ToolBarType.Paint)
//    {
//      toolbar_paint.setVisible(true);
//      toolbar_canvas.setVisible(false);
//    }
//    else
//    {
//      toolbar_paint.setVisible(false);
//      toolbar_canvas.setVisible(true);
//    }
//  }

  private void showAbout(Node sourceN)
  {
    Alert alert = new Alert(AlertType.INFORMATION);
    StringBuilder sb = new StringBuilder();
    sb.append("This software is released under the GPLv3.\n\n");
    sb.append("Author: Paul Kashofer Austria\n");
    sb.append("Web: https://github.com/Starcommander/JBlitzPaint");
    alert.setContentText(sb.toString());
    alert.show();
  }

  private void selectColor(Node sourceN)
  {
    Popup win = new Popup();
    win.setAutoHide(true);
    ColorPicker colorPicker = new ColorPicker(color);
    colorPicker.setOnAction((event) -> color = colorPicker.getValue() );
    win.getContent().add(colorPicker);
    Point pos = MouseInfo.getPointerInfo().getLocation();
    win.show(sourceN, pos.x, pos.y);
  }

//  void loadSaveClip(boolean do_save)
//  {
//    if (do_save)
//    {
//      WritableImage image = new WritableImage((int)pane.getWidth(), (int)pane.getHeight());
//      pane.snapshot(null, image);
//      clipTool.putImageToClipboard(image);
//    }
//    else
//    {
//      PaintObject.clearAllObjects(pane);
//      pane.setCursor(Cursor.WAIT);
//      Image contentPix = null;
//      contentPix = clipTool.getImageFromClipboard();
//      if (contentPix==null) { contentPix = new WritableImage(400,200); }
//      pane.setCursor(Cursor.DEFAULT);
//      openPix(pane, contentPix);
//    }
//  }

//  void loadScrShot()
//  {
//    stage.setIconified(true);
////    stage.hide();
//    try
//    {
//      Thread.sleep(3000);
////      BlitzPaint.showMain(new Stage(), null, true);
//    } catch (Exception e) {}
////    String[] args = {"-screenshot"};
////    Application.launch(BlitzPaint.class, args);
//    BlitzPaint.takeShot(stage, this);
//    stage.setIconified(false);
//  }
  
  public static boolean openPix(Pane pane, Pane txtpane, Image pix, boolean doFit)
  {
    if (changedValue!=null)
    {
        if (!showAlert(AlertType.CONFIRMATION, "Discard changes?")) { return false; }
        changedValue = null;
        changedFile = null;
    }
    pane.getChildren().clear();
    double w;
    double h;
    if (doFit)
    {
        h = ((Pane)pane.getParent()).getHeight();
        double factor = pix.getWidth()/pix.getHeight();
        w = h * factor;
    }
    else
    {
        w = pix.getWidth();
        h = pix.getHeight();
    }
    pane.setMaxSize(w, h);
    pane.getChildren().add(createImageView(pix,(int)w,(int)h));
    assignText(txtpane, pix);
    return true;
  }
  
  private void saveMetadata()
  {
      if (changedValue==null) { return; }
      if (changedFile==null) { showAlert(AlertType.ERROR, "Saving not possible, lost file name."); return; }
      try
      {
        ImageTagger imageTagger = new ImageTagger();
        String imageFile = changedFile;
        imageTagger.readMetadataFromFile(imageFile);
        imageTagger.getMetadataProp().put("comment", changedValue);
        imageTagger.writeMetadataImage(imageFile);
      }
      catch (Exception e)
      {
        severe(ImageTaggerFrame.class, e, "Error writing metadata.");
      }
      changedValue = null;
  }
  
  static boolean showAlert(AlertType al, String txt)
  {
    Alert alert = new Alert(al);
    alert.setTitle("Confirmation Dialog");
    alert.setHeaderText("Warning, changed metadata not stored");
    alert.setContentText(txt);
    if (alert.showAndWait().get() == ButtonType.OK) { return true; }
    return false;
  }
  
  private static void assignText(Pane pane, Image pix)
  {
    String txt = "";
    try
    {
      ImageTagger imageTagger = new ImageTagger();
      if (pix.getUrl().startsWith("file:"))
      {
        String imageFile = pix.getUrl().substring(5); // file:xxx
        changedFile = imageFile;
        imageTagger.readMetadataFromFile(imageFile);
        for (java.util.Map.Entry<Object, Object> curEntry : imageTagger.getMetadataProp().entrySet())
        {
          txt = (String)curEntry.getValue();
          break;
        }
      }
    }
    catch (IOException e)
    {
        severe(ImageTaggerFrame.class, e, "Error reading metadata.");
    }
    
    TextField tf = new TextField(txt);
    tf.textProperty().addListener(createTextListener());
    
//    pane.getScene().widthProperty().addListener(createResizer(tf));

    double width = pane.getScene().getWidth();
    if (width > 60) { width-=20; }
    tf.setMaxWidth(width);
    tf.setMinWidth(width);
    TextField tf_file = new TextField(pix.getUrl());
//    tf_file.setStyle("-fx-text-fill: #BA55D3;");
    tf_file.setStyle("-fx-background-color: cyan;");
    tf_file.setEditable(false);
    VBox vbox = new VBox();
    vbox.getChildren().add(tf_file);
    vbox.getChildren().add(tf);
    pane.getChildren().clear();
    pane.getChildren().add(vbox);
  }
  
  private static InvalidationListener createTextListener()
  {
      InvalidationListener l = new InvalidationListener()
      {
          @Override
          public void invalidated(Observable o)
          {
              StringProperty s = (StringProperty)o;
              changedValue = s.getValue();
          }
      };
      return l;
  }
  
  public static void openEmptyPix(Pane pane, int sizeX, int sizeY)
  {
    PaintObject.clearAllObjects(pane);
    WritableImage contentPix = new WritableImage(sizeX, sizeY);
    openPix(pane, null, contentPix, false);
  }
  
  void loadFolder()
  {
      pixlistpane.getChildren().clear();
      DirectoryChooser dirChooser = new DirectoryChooser();
      File dir = dirChooser.showDialog(null);
      if (dir == null) { return; }
      Task openWorker = createWorker(dir);
      progressBar.progressProperty().bind(openWorker.progressProperty());
      new Thread(openWorker).start();
  }
  
  private Task createWorker(File dir)
  { // Open images and add to Sidebar
    return new Task()
    {
      @Override
      protected Object call()
      {
        File[] files = dir.listFiles();
        ArrayList<ImageView> ivs = new ArrayList<ImageView>();
        for (int i=0; i<files.length; i++)
        {
          String fName = files[i].getName().toLowerCase();
          if (fName.endsWith(".jpg") || fName.endsWith(".jpeg"))
          {
              Image contentPix = new Image("file:" + files[i].getPath());
              ImageView iv = createImageView(contentPix, 200, 200);
              iv.setOnMouseClicked((ev) -> selectImg(ev));
              iv.setOnMouseEntered((ev) -> trySetEffect(ev, glowFx));
              iv.setOnMouseExited((ev) -> trySetEffect(ev, null));
              ivs.add(iv);
          }
          updateProgress(i+1, files.length);
        }
        Platform.runLater(() -> addPictures(ivs));
        updateProgress(0, 100);
        return true;
      }
    };
  }
  
  void addPictures(ArrayList<ImageView> ivs)
  {
    VBox hbox = new VBox();
    for (ImageView iv : ivs) { hbox.getChildren().add(iv); }
    pixlistpane.getChildren().add(hbox);
  }
  
  void trySetEffect(MouseEvent ev, Effect fx)
  {
      ImageView iv = (ImageView)ev.getSource();
      if (iv.effectProperty().get() == glowFx)
      {
          if (fx == null) { iv.effectProperty().set(null); }
      }
      if (iv.effectProperty().get() == null)
      {
          iv.effectProperty().set(fx);
      }
  }
  
  void selectImg(MouseEvent ev)
  {
      ImageView iv = (ImageView)ev.getSource();
      boolean isOpen = openPix(pixpane, txtpane, iv.getImage(), true);
      if (!isOpen) { return; }
      for (Node o : pixlistpane.getChildren())
      {
          for (Node oo : ((Pane)o).getChildren())
          {
              if (oo.effectProperty().get() == dropShadowFx) { oo.effectProperty().set(null); }
          }
      }
      iv.effectProperty().set(dropShadowFx);
  }
  
  void loadSaveFile(boolean do_save)
  {
    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Image files",
        "*.png", "*.jpg", "*.bmp",
        "*.PNG", "*.JPG", "*.BMP");
    fileChooser.getExtensionFilters().add(extFilter);
   
    //Show save file dialog
    File file = null;
    if (do_save)
    {
      fileChooser.setTitle("Save to file");
      file = fileChooser.showSaveDialog(null);
    }
    else
    {
      fileChooser.setTitle("Load from file");
      file = fileChooser.showOpenDialog(null);
    }
    if(file != null)
    {
      loadSaveFileDirect(do_save, file.getPath());
    }
  }
  
  void loadSaveFileDirect(boolean do_save, String file)
  {
    if (do_save)
    {
      WritableImage image = new WritableImage((int)pixpane.getWidth(), (int)pixpane.getHeight());
      pixpane.snapshot(null, image);
      com.starcom.pix.Saver.save(SwingFXUtils.fromFXImage(image,null), file);
    }
    else
    {
      Image contentPix = new Image("file:" + file);
      openPix(pixpane, txtpane, contentPix, false);
    }
  }
  
//  void changeButtonActive(Node button, boolean b_active)
//  {
//    if (b_active)
//    {
//      button.setStyle("-fx-background-color: yellowgreen");
//    }
//    else
//    {
//      button.setStyle(null);
//    }
//  }
  
//  private void selectTool(String id)
//  {
//    System.out.println("Selected tool: " + id);
//    ITool lastTool = currentTool;
//    currentTool = tools.get(id);
//    if (lastTool != null) { lastTool.onDeselected(); }
//    if (currentTool != null)
//    {
//      currentTool.onSelected();
//      return;
//    }
//    String toolClass = ITool.class.getPackage().getName() + "." + id;
//    try
//    {
//      currentTool = (ITool) Class.forName(toolClass).newInstance();
//    }
//    catch (Exception e) { throw new IllegalArgumentException(e); }
//    currentTool.init(pane);
//    currentTool.onSelected();
//    tools.put(id, currentTool);
//  }
  public static ImageView createImageView(Image image, int sizeX, int sizeY)
  {
      ImageView iv = createImageView(image);
      iv.setFitWidth(sizeX);
      iv.setFitHeight(sizeY);
      return iv;
  }

  public static ImageView createImageView(Image image)
  {
    ImageView iv = new ImageView();
    iv.setImage(image);
    return iv;
  }
}
