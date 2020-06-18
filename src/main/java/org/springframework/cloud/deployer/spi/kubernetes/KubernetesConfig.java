package org.springframework.cloud.deployer.spi.kubernetes;

public class KubernetesConfig {

	private String name;
	private String masterUrl;
	private String oauthToken;

	public KubernetesConfig() {}

	public KubernetesConfig(String masterUrl, String oauthToken) {
		this.masterUrl = masterUrl;
		this.oauthToken = oauthToken;
	}

	public KubernetesConfig(String name, String masterUrl, String oauthToken) {
		this.name = name;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
