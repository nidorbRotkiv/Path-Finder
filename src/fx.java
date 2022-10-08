import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class fx extends Application {

    private final Pane bottom = new Pane();
    private ListGraph<City> listGraph = new ListGraph<>();
    private City selectedDot1;
    private City selectedDot2;
    private ArrayList<City> dotList = new ArrayList<>();
    private int dotCounter;
    private boolean unsavedChanges;
    private ImageView imageView;
    private ArrayList<Label> labelList = new ArrayList<>();
    private String mapFromFile = "file:europa.gif";
    private Image map;
    private File file = new File("europa.graph");


    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setBottom(bottom);
        bottom.setId("outputArea");
        FlowPane center = new FlowPane();
        primaryStage.setTitle("PathFinder");

        VBox vbox = new VBox();
        MenuBar menuBar = new MenuBar();
        menuBar.setId("menu");
        root.getChildren().add(vbox);
        Menu menu = new Menu("File");
        menu.setId("menuFile");
        vbox.getChildren().add(menuBar);
        menuBar.getMenus().add(menu);

        MenuItem newMap = new MenuItem("New Map");
        newMap.setId("menuNewMap");
        menu.getItems().add(newMap);
        class NewMap implements EventHandler<ActionEvent> {
            public void handle(ActionEvent event) {
                bottom.getChildren().remove(imageView);
                selectedDot1 = null;
                selectedDot2 = null;
                listGraph.getNodesMap().clear();
                for (City city : dotList) {
                    bottom.getChildren().remove(city);
                }
                for (Label label : labelList) {
                    bottom.getChildren().remove(label);
                }
                dotCounter = 0;
                dotList.clear();

                map = new Image(mapFromFile);
                imageView = new ImageView(map);
                primaryStage.setMinHeight(850);
                bottom.getChildren().addAll(imageView);
            }
        }
        newMap.setOnAction(new NewMap());

        MenuItem open = new MenuItem("Open");
        open.setId("menuOpenFile");
        menu.getItems().add(open);
        class Open implements EventHandler<ActionEvent> {


            public void handle(ActionEvent event) {

                if (unsavedChanges) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setHeaderText("Unsaved changes, open anyway?");
                    Optional<ButtonType> answer = alert.showAndWait();
                    if (answer.get() == ButtonType.CANCEL) {
                        return;
                    }
                }
                try {
                    getMapFromFile();
                } catch (FileNotFoundException f) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.showAndWait();
                }
                new NewMap().handle(event);
                unsavedChanges = false;
                try {
                    openFile();
                } catch (FileNotFoundException f) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.showAndWait();
                }
            }

            private void drawDot(String name, double x, double y) {

                City city = new City(name, x, y);
                Label text = new Label(city.getName());
                text.setDisable(true);
                text.setStyle("-fx-opacity: 1.0;");
                text.setTranslateX(x + 3.14);
                text.setTranslateY(y + 3.14);
                bottom.getChildren().addAll(city, text);
                dotList.add(city);
                dotList.get(dotCounter).setOnMouseClicked(new ClickHandler());
                dotCounter++;
                listGraph.add(city);
            }

            private void drawLine(double cityX, double cityY, double destinationX, double destinationY) {

                Line line = new Line();
                line.setDisable(true);
                line.setStartX(cityX);
                line.setEndX(destinationX);
                line.setStartY(cityY);
                line.setEndY(destinationY);
                bottom.getChildren().add(line);

            }

            void openFile() throws FileNotFoundException {
                Scanner scanner = new Scanner(file);

                int counter = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();

                    if(line.isEmpty())
                        return;

                    //Rad 1, skippa
                    if (counter == 0) {
                        counter++;
                        continue;
                    }

                    //Rad 2, bygg upp en map med Cities(Dots)
                    if (counter == 1) {

                        String[] elements = line.split(";");
                        var elementList = Arrays.asList(elements);
                        for (int i = 0; i < elementList.size(); i += 3) {

                            String cityName = elementList.get(i);
                            double x = Double.parseDouble(elementList.get(i + 1));
                            double y = Double.parseDouble(elementList.get(i + 2));
                            drawDot(cityName, x, y);
                        }

                        counter++;
                        continue;
                    }

                    String[] elements = line.split(";");
                    var elementList = Arrays.asList(elements);

                    for (int i = 0; i < elementList.size() - 1; i += 4) {

                        String edgeName = elementList.get(i + 2);
                        int weight = Integer.parseInt(elementList.get(i + 3));
                        if (!listGraph.pathExists(getCity(elementList.get(i)), getCity(elementList.get(i+1)))) {
                            listGraph.connect(getCity(elementList.get(i)), getCity(elementList.get(i+1)), edgeName, weight);

                            drawLine(getCity(elementList.get(i)).getX(), getCity(elementList.get(i)).getY(),
                                    getCity(elementList.get(i+1)).getX(), getCity(elementList.get(i+1)).getY());
                        }
                    }
                    counter++;
                }
            }

            City getCity(String cityName){
                String[] array = cityName.split(" ");
                String result = array[0];
                for(City city : dotList) {
                    if(city.getName().equalsIgnoreCase(result)){
                        return city;
                    }
                }
                return null;
            }

            void getMapFromFile() throws FileNotFoundException {
                Scanner scanner = new Scanner(file);
                if(scanner.hasNext()) {
                    mapFromFile = scanner.nextLine();
                }
            }
        }
        open.setOnAction(new Open());

        MenuItem save = new MenuItem("Save");
        save.setId("menuSaveFile");
        menu.getItems().add(save);
        class Save implements EventHandler<ActionEvent> {
            private StringBuilder sb = new StringBuilder();

            public void handle(ActionEvent event) {

                sb.delete(0, sb.length());

                if (mapFromFile == null) {
                    sb.append("file:europa.gif" + "\n");
                } else {
                    sb.append(mapFromFile + "\n");
                }

                for (City dot : dotList) {
                    sb.append(dot.getName() + ";" + dot.getX() + ";" + dot.getY() + ";");
                }
                sb.append("\n");
                for (Map.Entry<City, Set<Edge<City>>> pair : listGraph.getNodesMap().entrySet()) {

                    for (Edge edge : pair.getValue()) {
                        sb.append(pair.getKey() + ";" + edge.getDestination() + ";" + edge.getName() + ";" + edge.getWeight() + "\n");
                    }
                }

                exportData();
                unsavedChanges = false;
            }


            private void exportData() {
                try (PrintWriter out = new PrintWriter("europa.graph")) {
                    out.println(sb.toString());
                } catch (java.io.IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error" + e.getMessage());
                    alert.showAndWait();
                }
            }
        }
        save.setOnAction(new Save());

        MenuItem saveImage = new MenuItem("Save Image");
        saveImage.setId("menuSaveImage");
        menu.getItems().add(saveImage);
        class SaveImageHandler implements EventHandler<ActionEvent> {
            public void handle(ActionEvent event) {
                try {
                    WritableImage image = root.snapshot(null, null);
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    ImageIO.write(bufferedImage, "png", new File("capture.png"));
                } catch (IOException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "IO-fel " + e.getMessage());
                    alert.showAndWait();
                }
            }
        }
        saveImage.setOnAction(new SaveImageHandler());

        MenuItem exit = new MenuItem("Exit");
        exit.setId("menuExit");
        menu.getItems().add(exit);
        class Exit implements EventHandler<ActionEvent> {
            public void handle(ActionEvent event) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Unsaved changes, exit anyway?");
                Optional<ButtonType> answer = alert.showAndWait();
                if (answer.get() == ButtonType.OK) {
                    primaryStage.close();
                }
            }
        }
        exit.setOnAction(new Exit());

        root.setTop(menuBar);

        center.setAlignment(Pos.CENTER);
        root.setCenter(center);

        class NewDot implements EventHandler<MouseEvent> {
            public void handle(MouseEvent event) {
                CityNamer cityNamer = new CityNamer();
                Optional<ButtonType> answer = cityNamer.showAndWait();

                if (answer.get() == ButtonType.OK) {

                    if (!cityNamer.getNameField().isEmpty()) {

                        City city = new City(cityNamer.getNameField(), event.getX(), event.getY());
                        Label text = new Label(city.getName());
                        text.setDisable(true);
                        text.setStyle("-fx-opacity: 1.0;");
                        text.setTranslateX(event.getX() + 3.14);
                        text.setTranslateY(event.getY() + 3.14);
                        bottom.getChildren().addAll(city, text);
                        labelList.add(text);
                        dotList.add(city);
                        dotList.get(dotCounter).setOnMouseClicked(new ClickHandler());
                        dotCounter++;
                        bottom.setOnMouseClicked(null);
                        bottom.setCursor(Cursor.DEFAULT);
                        listGraph.add(city);

                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Enter a valid name!");
                        alert.showAndWait();

                        bottom.setOnMouseClicked(null);
                        bottom.setCursor(Cursor.DEFAULT);
                    }
                } else {
                    bottom.setOnMouseClicked(null);
                    bottom.setCursor(Cursor.DEFAULT);
                }
            }
        }

        Button b1 = new Button("Find Path");
        b1.setId("btnFindPath");
        class FindPath implements EventHandler<ActionEvent> {
            public void handle(ActionEvent event) {
                if (selectedDot1 == null || selectedDot2 == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Two places must be selected");
                    alert.showAndWait();
                    return;
                }

                FindPathDialog fpd = new FindPathDialog();
                fpd.showAndWait();
            }
        }
        b1.setOnAction(new FindPath());

        Button b2 = new Button("Show connection");
        b2.setId("btnShowConnection");
        class ShowConnection implements EventHandler<ActionEvent> {
            public void handle(ActionEvent event) {
                if (selectedDot1 == null || selectedDot2 == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Two places must be selected");
                    alert.showAndWait();
                    return;
                }
                if (listGraph.getEdgeBetween(selectedDot1, selectedDot2) == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "No connection");
                    alert.showAndWait();
                    return;
                }
                ShowConnectionDialog scd = new ShowConnectionDialog();
                scd.showAndWait();
            }
        }
        b2.setOnAction(new ShowConnection());

        Button b3 = new Button("New Place");
        b3.setId("btnNewPlace");
        class NewPlace implements EventHandler<ActionEvent> {
            public void handle(ActionEvent event) {
                bottom.setCursor(Cursor.CROSSHAIR);
                bottom.setOnMouseClicked(new NewDot());
                unsavedChanges = true;
            }

        }
        b3.setOnAction(new NewPlace());

        Button b4 = new Button("New Connection");
        b4.setId("btnNewConnection");
        class NewConnection implements EventHandler<ActionEvent> {
            public void handle(ActionEvent event) {
                if (selectedDot1 == null || selectedDot2 == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Two places must be selected");
                    alert.showAndWait();
                    return;
                }
                if (listGraph.getEdgeBetween(selectedDot1, selectedDot2) != null) {
                    Alert existingConnection = new Alert(Alert.AlertType.ERROR, "Connection already exists!");
                    existingConnection.showAndWait();
                    return;
                }
                NewConnectionDialog ncd = new NewConnectionDialog();
                Optional<ButtonType> answer = ncd.showAndWait();
                if (answer.get() == ButtonType.OK) {
                    if (!ncd.getNameField().isEmpty() && !ncd.getTimeField().isEmpty()) {
                        listGraph.connect(selectedDot1, selectedDot2, ncd.getNameField(), Integer.parseInt(ncd.getTimeField()));
                        Line line = new Line();
                        line.setStartX(selectedDot1.getX());
                        line.setEndX(selectedDot2.getX());
                        line.setStartY(selectedDot1.getY());
                        line.setEndY(selectedDot2.getY());
                        bottom.getChildren().add(line);
                        line.setDisable(true);
                        unsavedChanges = true;
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Enter a valid name and time!");
                        alert.showAndWait();
                    }
                } else {
                    bottom.setOnMouseClicked(null);
                    bottom.setCursor(Cursor.DEFAULT);
                }
            }
        }
        b4.setOnAction(new NewConnection());

        Button b5 = new Button("Change Connection");
        b5.setId("btnChangeConnection");
        class ChangeConnection implements EventHandler<ActionEvent> {
            public void handle(ActionEvent event) {
                if (selectedDot1 == null || selectedDot2 == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Two places must be selected");
                    alert.showAndWait();
                    return;
                }
                if (listGraph.getEdgeBetween(selectedDot1, selectedDot2) == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "No connection");
                    alert.showAndWait();
                    return;
                }
                ChangeConnectionDialog cdg = new ChangeConnectionDialog();
                Optional<ButtonType> answer = cdg.showAndWait();
                if (answer.get() == ButtonType.OK) {
                    listGraph.setConnectionWeight(selectedDot1,
                            selectedDot2, Integer.parseInt(cdg.getTimeField()));
                    unsavedChanges = true;
                } else {
                    bottom.setOnMouseClicked(null);
                    bottom.setCursor(Cursor.DEFAULT);
                }
            }
        }
        b5.setOnAction(new ChangeConnection());

        center.setPadding(new Insets(15));
        center.setHgap(10);
        center.setPrefWidth(600);

        center.getChildren().addAll(b1, b2, b3, b4, b5);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    class ClickHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event) {
            City d = (City) event.getSource();

            if (selectedDot1 == null && d != selectedDot2) {
                selectedDot1 = d;
                d.setFill(Color.RED);
            } else if (selectedDot2 == null && d != selectedDot1) {
                selectedDot2 = d;
                d.setFill(Color.RED);
            } else if (d == selectedDot1) {
                selectedDot1 = null;
                d.setFill(Color.BLUE);
            } else if (d == selectedDot2) {
                selectedDot2 = null;
                d.setFill(Color.BLUE);
            }
        }
    }

    class CityNamer extends Alert {
        private TextField nameField = new TextField();

        CityNamer() {
            super(AlertType.CONFIRMATION);

            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10));
            grid.setHgap(5);
            grid.setVgap(10);
            grid.addRow(0, new Label("Name of place:"), nameField);
            setHeaderText(null);
            getDialogPane().setContent(grid);
        }

        String getNameField() {
            return nameField.getText();
        }
    }

    class NewConnectionDialog extends Alert {
        private TextField nameField = new TextField();
        private TextField timeField = new TextField();

        NewConnectionDialog() {
            super(AlertType.CONFIRMATION);

            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10));
            grid.setHgap(5);
            grid.setVgap(10);
            grid.addRow(0, new Label("Name:"), nameField);
            grid.addRow(1, new Label("Time:"), timeField);
            setHeaderText(null);
            getDialogPane().setContent(grid);
        }

        String getNameField() {
            return nameField.getText();
        }

        String getTimeField() {
            return timeField.getText();
        }
    }

    class ChangeConnectionDialog extends Alert {
        private TextField nameField = new TextField();
        private TextField timeField = new TextField();

        ChangeConnectionDialog() {
            super(AlertType.CONFIRMATION);

            nameField.setText(listGraph.getEdgeBetween(selectedDot1, selectedDot2).getName());
            nameField.setDisable(true);

            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10));
            grid.setHgap(5);
            grid.setVgap(10);
            grid.addRow(0, new Label("Name:"), nameField);
            grid.addRow(1, new Label("Time:"), timeField);
            setHeaderText(null);
            getDialogPane().setContent(grid);
        }

        String getTimeField() {
            return timeField.getText();
        }
    }

    class ShowConnectionDialog extends Alert {
        private TextField nameField = new TextField();
        private TextField timeField = new TextField();

        ShowConnectionDialog() {
            super(AlertType.CONFIRMATION);

            nameField.setText(listGraph.getEdgeBetween(selectedDot1, selectedDot2).getName());
            nameField.setDisable(true);
            timeField.setText(String.valueOf(listGraph.getEdgeBetween(selectedDot1, selectedDot2).getWeight()));
            timeField.setDisable(true);

            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setPadding(new Insets(10));
            grid.setHgap(5);
            grid.setVgap(10);
            grid.addRow(0, new Label("Connection from " + selectedDot1.getName() + " to " + selectedDot2.getName()));
            grid.addRow(1, new Label("Name:"), nameField);
            grid.addRow(2, new Label("Time:"), timeField);
            setHeaderText(null);
            getDialogPane().setContent(grid);
        }
    }

    class FindPathDialog extends Alert {
        FindPathDialog() {
            super(AlertType.INFORMATION);

            setHeaderText("The Path from " + selectedDot1.getName() + " to " + selectedDot2.getName());

            StringBuilder sb = new StringBuilder();
            List<Edge<City>> list = listGraph.getAnyPath(selectedDot1, selectedDot2);
            int counter = 0;
            for (Edge edge : list) {
                sb.append("to ");
                sb.append(edge.getDestination());
                sb.append(" by ");
                sb.append(edge.getName());
                sb.append(" takes ");
                sb.append(edge.getWeight());
                counter += edge.getWeight();
                sb.append(" hours.");
                sb.append("\n");
            }

            sb.append("Total: ");
            sb.append(counter);
            sb.append(" hours.");
            String s = sb.toString();
            TextArea textArea = new TextArea(s);
            textArea.setDisable(true);
            getDialogPane().setContent(textArea);

        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}