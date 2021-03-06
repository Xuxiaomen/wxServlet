package ai.yale.wxserver.vo;

/**
 * 长链接转成短链接post的参数
 * @author 徐梦
 *
 */
public class LongLinkToShortLinkVo {
	private String action;
	private String long_url;
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getLong_url() {
		return long_url;
	}
	public void setLong_url(String long_url) {
		this.long_url = long_url;
	}
	@Override
	public String toString() {
		return "LongLinkToShortLinkVo [action=" + action + ", long_url=" + long_url + "]";
	}
}
