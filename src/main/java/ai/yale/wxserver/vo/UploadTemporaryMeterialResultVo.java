package ai.yale.wxserver.vo;

/**
 * 新增临时素材返回的json数据包
 * @author 徐梦
 *
 */
public class UploadTemporaryMeterialResultVo {
	private String type;
	private String media_id;
	private String created_at;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMedia_id() {
		return media_id;
	}
	public void setMedia_id(String media_id) {
		this.media_id = media_id;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}
	@Override
	public String toString() {
		return "UploadTemporaryMeterialResultVo [type=" + type + ", media_id=" + media_id + ", created_at=" + created_at
				+ "]";
	}
	
	
	
}
