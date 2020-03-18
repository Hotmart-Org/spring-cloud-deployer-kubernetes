package org.springframework.cloud.deployer.spi.kubernetes;

public class KubernetesConfig {

	private String masterUrl;
	private String oauthToken;

	public KubernetesConfig(String masterUrl, String oauthToken) {
		this.masterUrl = masterUrl;
		this.oauthToken = oauthToken;
	}

	public String getMasterUrl() {
		return masterUrl;
	}

	public void setMasterUrl(String masterUrl) {
		this.masterUrl = masterUrl;
	}

	public String getOauthToken() {
		return oauthToken;
	}

	public void setOauthToken(String oauthToken) {
		this.oauthToken = oauthToken;
	}
}
