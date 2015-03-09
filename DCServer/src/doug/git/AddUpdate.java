package doug.git;

public class AddUpdate {
	
	int added;
	int updated;

	public AddUpdate(int add, int update) {
		this.added = add;
		this.updated = update;
	}

	public int getAdded() {
		return added;
	}

	public void setAdded(int added) {
		this.added = added;
	}

	public int getUpdated() {
		return updated;
	}

	public void setUpdated(int updated) {
		this.updated = updated;
	}

}
