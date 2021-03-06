package ai.yale.wxserver.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import ai.yale.wxserver.bean.Article;
import ai.yale.wxserver.bean.NewsMessage;
import ai.yale.wxserver.bean.TextMessage;
import ai.yale.wxserver.vo.AccessTokenVo;
import ai.yale.wxserver.vo.AccumulatedUserDataVo;
import ai.yale.wxserver.vo.DateVo;
import ai.yale.wxserver.vo.JsapiSignatureVo;
import ai.yale.wxserver.vo.JsapiTicketVo;
import ai.yale.wxserver.vo.LinkRespMessageVo;
import ai.yale.wxserver.vo.LongLinkToShortLinkVo;
import ai.yale.wxserver.vo.QRCodeRequestVo;
import ai.yale.wxserver.vo.QRCodeTicketVo;
import ai.yale.wxserver.vo.SummaryUserDataVo;
import ai.yale.wxserver.vo.UploadTemporaryMeterialResultVo;
import ai.yale.wxserver.vo.WxResultVo;

/**
 * @Title: WxUtil
 * @Description: 微信公共库
 * @author 徐梦
 *
 */

@Component
public class WxUtils {

	@Autowired
	Configuration configuration;

	/**
	 * 校验微信配置参数
	 * 
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @return
	 */
	public boolean checkSignature(String signature, String timestamp, String nonce) {

		String[] arr = new String[] { configuration.getToken(), timestamp, nonce };
		Arrays.sort(arr);
		StringBuffer content = new StringBuffer();
		for (int i = 0; i < arr.length; i++) {
			content.append(arr[i]);
		}

		String localSignature = SecurityUtils.SHA1(content.toString());

		return localSignature.equals(signature);

	}

	/**
	 * 接收微信消息并转储为map
	 * 
	 * @param request
	 * @return
	 */
	public Map<String, String> receiveMessage(HttpServletRequest request) {
		InputStream inputStream;
		try {
			inputStream = request.getInputStream();
			Map<String, String> map = MessageUtils.streamToMap(inputStream);
			inputStream.close();
			return map;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 生成文本回复消息
	 * 
	 * @param receivedMessage
	 * @param content
	 * @return
	 */
	public String replyTextMessage(Map<String, String> receivedMessage, String content) {
		TextMessage reply = new TextMessage();
		reply.setToUserName(receivedMessage.get("FromUserName"));
		reply.setFromUserName(receivedMessage.get("ToUserName"));
		reply.setCreateTime((new Date()).getTime());
		reply.setMsgType(Configuration.MESSAGE_TEXT);
		reply.setContent(content);
		return MessageUtils.textMessageToXml(reply);
	}

	/**
	 * 生成图文回复消息
	 * 
	 * @param receivedMessage
	 * @param content
	 * @return
	 */
	public String replyNewsMessage(Map<String, String> receivedMessage, List<Article> articles) {
		NewsMessage reply = new NewsMessage();
		reply.setToUserName(receivedMessage.get("FromUserName"));
		reply.setFromUserName(receivedMessage.get("ToUserName"));
		reply.setCreateTime((new Date()).getTime());
		reply.setMsgType(Configuration.MESSAGE_NEWS);
		reply.setArticleCount((long) articles.size());
		reply.setArticles(articles);
		return MessageUtils.newsMessageToXml(reply);
	}

	/**
	 * 获取AccessToken
	 * 
	 * @return AccessTokenVo
	 */
	public AccessTokenVo getAccessToken() {
		RestTemplate restTemplate = new RestTemplate();
		String url = configuration.getAccessTokenUrl().replace("APPID", configuration.getAppId()).replace("APPSECRET",
				configuration.getAppSecret());
		AccessTokenVo vo = restTemplate.getForObject(url, AccessTokenVo.class);
		return vo;
	}

	/**
	 * 上传临时文件并获取media id
	 * 
	 * @return
	 */
	public UploadTemporaryMeterialResultVo uploadTemporaryMeterial(String accessToken, String type, File file) {
		RestTemplate restTemplate = new RestTemplate();
		String url = configuration.getUploadTemproryMeterialUrl().replace("ACCESS_TOKEN", accessToken).replace("TYPE",
				type);
		FileSystemResource fileSystemResource = new FileSystemResource(file);
		MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
		param.add("file", fileSystemResource);
		param.add("filename", fileSystemResource.getFilename());
		try {
			param.add("filelength", fileSystemResource.contentLength());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		restTemplate.getMessageConverters().add(new WxMessageConverter());
		UploadTemporaryMeterialResultVo vo = restTemplate.postForObject(url, param,
				UploadTemporaryMeterialResultVo.class);
		return vo;

	}

	/**
	 * 创建微信菜单
	 * 
	 * @param accessToken
	 * @param menu
	 * @return
	 */
	public WxResultVo createMenu(String accessToken, String menu) {
		RestTemplate restTemplate = new RestTemplate();
		String url = configuration.getCreateMenuUrl().replace("ACCESS_TOKEN", accessToken);
		JSONObject obj = JSON.parseObject(menu);
		WxResultVo vo = restTemplate.postForObject(url, obj, WxResultVo.class);
		return vo;
	}

	/**
	 * 获取jsapi ticket
	 * 
	 * @param accessToken
	 * @return JsapiTicketVo
	 */
	public JsapiTicketVo getJsapiTicket(String accessToken) {
		String url = configuration.getJsapiTicketUrl().replace("ACCESS_TOKEN", accessToken);
		RestTemplate restTemplate = new RestTemplate();
		JsapiTicketVo vo = restTemplate.getForObject(url, JsapiTicketVo.class);
		return vo;
	}

	/**
	 * 获取jsapi signature
	 * 
	 * @param accessToken
	 * @return
	 */
	public JsapiSignatureVo getJsapiSignature(String jsapiTicket, String url) {
		JsapiSignatureVo vo = new JsapiSignatureVo();
		vo.setNonceStr(UUID.randomUUID().toString());
		vo.setTimestamp(System.currentTimeMillis() / 1000);
		vo.setAppId(configuration.getAppId());
		String src = configuration.getJsapiSignString().replace("TICKET", jsapiTicket)
				.replace("NONCESTR", vo.getNonceStr()).replace("TIMESTAMP", vo.getTimestamp() + "").replace("URL", url);
		vo.setSignature(SecurityUtils.SHA1(src));
		return vo;
	}

	/**
	 * 创建二维码ticket
	 */
	public QRCodeTicketVo createQRCodeTicket(String accessToken, QRCodeRequestVo vo) {
		RestTemplate restTemplate = new RestTemplate();
		String url = configuration.getCreateQrcodeUrl().replace("TOKEN", accessToken);
		QRCodeTicketVo result = restTemplate.postForObject(url, vo, QRCodeTicketVo.class);
		return result;
	}

	/**
	 * 长链接转换成短链接
	 */
	public LinkRespMessageVo linkLongToShort(String accessToken, LongLinkToShortLinkVo vo) {
		RestTemplate restTemplate = new RestTemplate();
		String url = configuration.getLonglinkToShortlinkUrl().replace("ACCESS_TOKEN", accessToken);
		LinkRespMessageVo result = restTemplate.postForObject(url, vo, LinkRespMessageVo.class);
		return result;
	}

	/**
	 * 获取用户增减数据
	 * 
	 * @param accessToken
	 * @param vo
	 * @return
	 */
	public SummaryUserDataVo userDataSummary(String accessToken, DateVo vo) {
		RestTemplate restTemplate = new RestTemplate();
		String url = configuration.getGetUserSummaryUrl().replace("ACCESS_TOKEN", accessToken);
		SummaryUserDataVo result = restTemplate.postForObject(url, vo, SummaryUserDataVo.class);
//		String result = restTemplate.postForObject(url, vo, String.class);
		System.out.println(result);
		return null;
	}

	/**
	 * 获取累计用户数据
	 * 
	 * @param accessToken
	 * @param vo
	 * @return
	 */
	public AccumulatedUserDataVo userDateAccumulated(String accessToken, DateVo vo) {
		RestTemplate restTemplate = new RestTemplate();
		String url = configuration.getGetUserCumulateUrl().replace("ACCESS_TOKEN", accessToken);
		AccumulatedUserDataVo result = restTemplate.postForObject(url, vo, AccumulatedUserDataVo.class);
		return result;
	}
}
