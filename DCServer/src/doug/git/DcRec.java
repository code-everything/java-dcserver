package doug.git;

public class DcRec {
	
	private int _id;		// Unique record ID (primary key autoincrement
	private String active;	// Record Active Flag ([0 = Inactive; 1 = Active]
	private String datetime; // Date/Time of format YYYYMMDDTHHMMSS (Hour is 24 hour clock
	private String device;	// Device Name
	private String comment; // Comment (free form)

	public DcRec() {
	}

	public DcRec(int _id, String active, String datetime, String device, String comment) {
		this._id = _id;
		this.active = active;
		this.datetime = datetime;
		this.device = device;
		this.comment = comment;
	}

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public String getActive() {
		return active;
	}

	public void setActive(String active) {
		this.active = active;
	}

	public String getDatetime() {
		return datetime;
	}

	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		return "DcRec [_id=" + _id + ", active=" + active + ", datetime="
				+ datetime + ", device=" + device + ", comment=" + comment + "]";
	}

}
