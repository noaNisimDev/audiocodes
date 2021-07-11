import org.bson.Document;
import org.bson.conversions.Bson;

import com.google.gson.Gson;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

public class Mp3File {

	String _id;
	String fileName;
	String size;
	String length;
	boolean isValid = false;
	
	public Mp3File(String _id, String fileName, String size, String length, boolean isValid) {
		super();
		this._id = _id;
		this.fileName = fileName;
		this.size = size;
		this.length = length;
		this.isValid = isValid;
	}
	public Mp3File(String fileName, String size, String length, boolean isValid) {
		super();
		this.fileName = fileName;
		this.size = size;
		this.length = length;
		this.isValid = isValid;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
		Gson gson = new Gson();

		Bson filter = Filters.eq("fileName", this.fileName);
		Document doc = Document.parse(gson.toJson(this));
		ReplaceOptions options = new ReplaceOptions().upsert(true);
		HomeTask.collection.replaceOne(filter, doc, options);
	}

	@Override
	public String toString() {
		return "Mp3File [fileName=" + fileName + ", size=" + size + ", length=" + length + ", isValid=" + isValid + "]";
	}
	
	
}
