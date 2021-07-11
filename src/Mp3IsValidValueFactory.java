import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

public class Mp3IsValidValueFactory implements Callback<TableColumn.CellDataFeatures<Mp3File, CheckBox>, ObservableValue<CheckBox>> {
	@Override
	public ObservableValue<CheckBox> call(CellDataFeatures<Mp3File, CheckBox> param) {
		Mp3File mp3File = param.getValue();
        CheckBox checkBox = new CheckBox();
        checkBox.selectedProperty().setValue(mp3File.isValid());
        checkBox.selectedProperty().addListener((ov, old_val, new_val) -> {
        	mp3File.setValid(new_val);
        	//System.out.println("name" + mp3File.fileName + "isValid: " + mp3File.isValid());
        	
        });
        return new SimpleObjectProperty<>(checkBox);
	}


}


