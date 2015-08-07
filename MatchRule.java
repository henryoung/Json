package jsonparseutil;

/**
 * This class is used for json data serialization and deserialization
 * 
 * @version 1.0 2015-08-07
 * @author Yang Yuke
 */
public final class MatchRule {
	private String PARTN_ROLE;
	private String SD_DOC;

	public String getPARTN_ROLE() {
		return PARTN_ROLE;
	}

	public void setPARTN_ROLE(String PARTN_ROLE) {
		this.PARTN_ROLE = PARTN_ROLE;
	}

	public String getSD_DOC() {
		return SD_DOC;
	}

	public void setSD_DOC(String D_DOC) {
		this.SD_DOC = D_DOC;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else {
			if (this.getClass() == obj.getClass()) {
				MatchRule m = (MatchRule) obj;
				if ((this.getPARTN_ROLE().equals(m.getPARTN_ROLE())) && (this.getSD_DOC().equals(m.getSD_DOC()))) {
					return true;
				} else {
					return false;
				}

			} else {
				return false;
			}
		}
	}
}
