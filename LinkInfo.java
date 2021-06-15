
public class LinkInfo {

	private int pageNo;
	private int redirectPageTo;
	private float lowerLeftX;
	private float lowerLeftY;
	private float upperRightX;
	private float upperRightY;

	public LinkInfo() {

	}

	public LinkInfo(int pageNo, int redirectPageTo, float lowerLeftX, float lowerLeftY, float upperRightX,
			float upperRightY) {
		super();
		this.pageNo = pageNo;
		this.redirectPageTo = redirectPageTo;
		this.lowerLeftX = lowerLeftX;
		this.lowerLeftY = lowerLeftY;
		this.upperRightX = upperRightX;
		this.upperRightY = upperRightY;
	}

	public int getPageNo() {
		return pageNo;
	}

	public int getRedirectPageTo() {
		return redirectPageTo;
	}

	public float getLowerLeftX() {
		return lowerLeftX;
	}

	public float getLowerLeftY() {
		return lowerLeftY;
	}

	public float getUpperRightX() {
		return upperRightX;
	}

	public float getUpperRightY() {
		return upperRightY;
	}

}
