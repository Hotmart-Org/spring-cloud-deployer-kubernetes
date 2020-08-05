package org.springframework.cloud.deployer.spi.kubernetes;

public interface KubernetesConfigProvider {
	
	public KubernetesConfig next(String configName);

}
