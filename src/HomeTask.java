import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.sound.sampled.UnsupportedAudioFileException;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.MultimediaInfo;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

@SuppressWarnings("restriction")
public class HomeTask extends Application {
	static ObservableList<Mp3File> mp3Files;
	static ConnectionString connectionString;
	static MongoClientSettings settings;
	static MongoClient mongoClient;
	static MongoDatabase database;
	static MongoCollection collection;

	public static void main(String[] args) {
		// get all data from DB
		connectionString = new ConnectionString(
				"mongodb+srv://Noa:Noadev@mycluster.5py6b.mongodb.net/myFirstDatabase?retryWrites=true&w=majority");
		settings = MongoClientSettings.builder().applyConnectionString(connectionString).build();
		mongoClient = MongoClients.create(settings);
		database = mongoClient.getDatabase("audiocodes");
		collection = database.getCollection("mp3files");
		collection.createIndex(Indexes.descending("fileName"), new IndexOptions().unique(true));

		mp3Files = FXCollections.observableArrayList();
		insertToListFromDB();

		launch(args);
	}

	@SuppressWarnings("restriction")
	@Override
	public void start(Stage primaryStage) {

		// create first UI
		primaryStage.setTitle("File access");
		Label pathLabel = new Label("Path of file");
		TextField textField = new TextField();
		Button buttonSelectFile = new Button("Select folder");
		Button buttonShowsFiles = new Button("Show files");
		GridPane pane = new GridPane();

		pane.addRow(0, pathLabel, textField, buttonSelectFile);
		pane.addRow(3, buttonShowsFiles);
		pane.setPadding(new Insets(10));
		pane.setHgap(20);
		pane.setVgap(10);
		pane.setMinSize(500, 150);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(25);
		ColumnConstraints c2 = new ColumnConstraints();
		c2.setPercentWidth(60);
		ColumnConstraints c3 = new ColumnConstraints();
		c3.setPercentWidth(35);
		pane.getColumnConstraints().addAll(c1, c2, c3);

		pane.setStyle("-fx-padding: 50;" + "-fx-border-style: solid inside;" + "-fx-border-width: 2;"
				+ "-fx-border-insets: 5;" + "-fx-border-radius: 5;" + "-fx-border-color: grey;");

		Scene scene = new Scene(pane, 600, 200);
		// end of first client

		// create second UI
		TableView<Mp3File> table = new TableView<Mp3File>();
		Button buttonBack = new Button();

		table.setEditable(true);
		TableColumn<Mp3File, CheckBox> col1 = new TableColumn<Mp3File, CheckBox>("Is Valid");
		TableColumn col2 = new TableColumn("File Name");
		TableColumn col3 = new TableColumn("Length");
		TableColumn col4 = new TableColumn("Size");

		buttonBack.setText("Back");

		buttonBack.setOnAction(e -> {

			primaryStage.setTitle("File access");
			primaryStage.setWidth(600);
			primaryStage.setHeight(200);
			primaryStage.setScene(scene);
			primaryStage.show();
		});

		col1.setCellValueFactory(new Mp3IsValidValueFactory());
		col2.setCellValueFactory(new PropertyValueFactory<Mp3File, String>("fileName"));
		col3.setCellValueFactory(new PropertyValueFactory<Mp3File, String>("length"));
		col4.setCellValueFactory(new PropertyValueFactory<Mp3File, String>("size"));
		table.getColumns().addAll(col1, col2, col3, col4);

		table.setItems(mp3Files);

		final VBox vbox = new VBox();
		vbox.setSpacing(5);
		vbox.setPadding(new Insets(10, 0, 0, 10));
		vbox.getChildren().addAll(table, buttonBack);
		Scene scene2 = new Scene(vbox);
		// end of second UI

		primaryStage.setScene(scene);
		primaryStage.show();
		primaryStage.setAlwaysOnTop(true);

		pathLabel.setLabelFor(textField);
		pathLabel.setMnemonicParsing(true);

		textField.setPrefSize(400, 20);
		System.out.println(textField.getText());

		buttonSelectFile.setOnAction(e -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			File selectedDirectory = directoryChooser.showDialog(primaryStage);

			if (selectedDirectory == null) {
				// No Directory selected
			} else {
				extract(selectedDirectory.getAbsolutePath());

				// show second client
				primaryStage.setTitle("isValid Table");
				primaryStage.setWidth(400);
				primaryStage.setHeight(500);
				primaryStage.setScene(scene2);
				primaryStage.show();

			}

		});

		buttonShowsFiles.setOnAction(e -> {
			primaryStage.setTitle("isValid Table");
			primaryStage.setWidth(400);
			primaryStage.setHeight(500);
			primaryStage.setScene(scene2);
			primaryStage.show();
		});
	}

	private static void extract(String nameOfFolder) {
		mp3Files.clear();
		File f = new File(nameOfFolder);
		File listOfFiles[] = f.listFiles();
		long time = 0;

		// file x new on
		for (File x : listOfFiles) {
			if (x == null)
				return;
			if (x.isDirectory())
				extract(x.getPath());
			else if (x.getName().endsWith(".mp3")) {
				try {
					time = getDurationWithMp3Spi(x);
				} catch (UnsupportedAudioFileException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Mp3File mp3File = new Mp3File(x.getName(), x.length() / 1024 / 1024 + "MB", time + "(s)", false);
				mp3Files.add(mp3File);
				Gson gson = new Gson();
				Bson filter = Filters.eq("fileName", mp3File.fileName);
				Document doc = Document.parse(gson.toJson(mp3File));

				try {
					collection.insertOne(doc);
				} catch (Exception e) {
					// System.out.println(e.getMessage());
				}
				;
				mp3Files.clear();
				insertToListFromDB();
			}
		}
	}

	// using JAVE jar
	private static long getDurationWithMp3Spi(File file) throws UnsupportedAudioFileException, IOException {
		Encoder encoder = new Encoder();
		try {
			MultimediaInfo mi = encoder.getInfo(file);
			long ls = mi.getDuration();
			return ls / 1000;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}

	static void insertToListFromDB() {
		FindIterable<Document> iterDoc = collection.find();
		Iterator it = iterDoc.iterator();
		Gson gson = new Gson();
		while (it.hasNext()) {
			Document doc = (Document) it.next();
			mp3Files.add(new Mp3File(doc.getString("fileName"), doc.getString("size"), doc.getString("length"),
					doc.getBoolean(("isValid"))));
		}
	}
}
