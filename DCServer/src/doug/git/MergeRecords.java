package doug.git;

import java.util.ArrayList;

public class MergeRecords {

	private Dc_DbHelper masterDb;
	private Dc_DbHelper clientDb;

	private ArrayList<DcRec> masterRecs;
	private ArrayList<DcRec> clientRecs;

	private DcRec mRec;
	private DcRec cRec;

	private static String deviceName;

	public MergeRecords(String inClientName) {
		this.deviceName = inClientName;
	}

	public AddUpdate mergeRecs() {
		int mIdx, cIdx, foundIdx, updateCount, addCount;
		boolean found, cDone, mDone;

		masterDb = new Dc_DbHelper(Common.masterDbPath, false);

		try {
			masterRecs = masterDb.getAllDcList();
		} catch (Exception e) {
			e.printStackTrace();
		}

		updateCount = 0;
		addCount = 0;
		int nRecs = 0;

		clientDb = new Dc_DbHelper(Common.defaultCurrentClientDatabaseFilePath,
				false);
		try {
			nRecs = clientDb.getDcRecCount();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (nRecs > 0) {

			try {
				clientRecs = clientDb.getAllDcList();
			} catch (Exception e) {
				e.printStackTrace();
			}
			clientDb = null;

			cDone = false;
			cIdx = 0;
			while (!cDone) {
				cRec = clientRecs.get(cIdx);
				mDone = false;
				mIdx = 0;
				found = false;
				foundIdx = -1;
				while (!mDone) {
					mRec = masterRecs.get(mIdx);
					// Record matches if the datetime and device name are equal
					if (mRec.getDatetime().equals(cRec.getDatetime())
							&& mRec.getDevice().equals(cRec.getDevice())) {
						found = true;
						foundIdx = mIdx;
						mDone = true;
					} else {
						mIdx++;
						if (mIdx >= masterRecs.size()) {
							mDone = true;
						}
					}
				}

				// If found then see if update is required
				if (found) {
					// If either the active or comment is different
					// then update is required
					if (!mRec.getActive().equals(cRec.getActive())
							|| !mRec.getComment().equals(cRec.getComment())) {
						cRec.set_id(mRec.get_id());
						updateCount++;
						try {
							masterDb.updateDcRec(cRec);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					// Not found - add the record
					addCount++;
					try {
						masterDb.addDcRec(cRec);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				// Go to the next Client rec unless finished
				cIdx++;
				if (cIdx >= clientRecs.size()) {
					cDone = true;
				}
			}
		} else {
			Common.logit("           Empty client database file", 
					"STAT:    ***** Empty client database file");
		}

		AddUpdate au = new AddUpdate(addCount, updateCount);

		return au;

	}

}
