/*
 * Copyright 2016-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.deployer.spi.kubernetes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.deployer.spi.core.RuntimeEnvironmentInfo;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;
import org.springframework.cloud.deployer.spi.task.TaskStatus;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

public class HotmartKubernetesTaskLauncher implements TaskLauncher {

	private final Log logger = LogFactory.getLog(getClass().getName());

	private ContainerFactory containerFactory;
	private KubernetesDeployerProperties properties;
	private KubernetesConfigProvider kubernetesConfigProvider;

	public HotmartKubernetesTaskLauncher(
			ContainerFactory containerFactory, KubernetesDeployerProperties properties, KubernetesConfigProvider kubernetesConfigProvider) {
		this.containerFactory = containerFactory;
		this.properties = properties;
		this.kubernetesConfigProvider = kubernetesConfigProvider;
	}

	@Override
	public String launch(AppDeploymentRequest request) {
		KubernetesTaskLauncher taskLauncher = getTaskLauncher(request.getDeploymentProperties().get("com.hotmart.transcoder.config-name"));

		Map<String, String> deploymentProperties = new HashMap<String, String>(request.getDeploymentProperties());
		
		String envVars = deploymentProperties.get("spring.cloud.deployer.kubernetes.environmentVariables");
		
		if(envVars != null) {
			envVars += ",";
		} else {
			envVars = "";
		}
		
		envVars += "KUBERNETES_MASTER=" + taskLauncher.client.getConfiguration().getMasterUrl() + 
	            ",KUBERNETES_AUTH_TOKEN=" + taskLauncher.client.getConfiguration().getOauthToken();
		
		deploymentProperties.put("spring.cloud.deployer.kubernetes.environmentVariables", envVars);
		
		AppDeploymentRequest requestClone = new AppDeploymentRequest(request.getDefinition(), request.getResource(), deploymentProperties, request.getCommandlineArguments());
		
		return taskLauncher.launch(requestClone);
	}

	@Override
	public void cancel(String id) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void cleanup(String id) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public void destroy(String appName) {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public RuntimeEnvironmentInfo environmentInfo() {
		throw new IllegalStateException("Not implemented");
	}

	@Override
	public TaskStatus status(String id) {
		throw new IllegalStateException("Not implemented");
	}

	private KubernetesTaskLauncher getTaskLauncher(String configName) {
		KubernetesConfig kubernetesConfig = kubernetesConfigProvider.next(configName);
		
		logger.info("Using config " + kubernetesConfig.getMasterUrl());

		Cache<String, KubernetesTaskLauncher> cache = CacheBuilder.newBuilder().build();
		
		try {
			return cache.get(kubernetesConfig.getMasterUrl(), loader(kubernetesConfig));
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private Callable<KubernetesTaskLauncher> loader(KubernetesConfig kubernetesConfig) {
		return () -> {
			Config config = new ConfigBuilder()
					.withMasterUrl(kubernetesConfig.getMasterUrl())
					.withOauthToken(kubernetesConfig.getOauthToken())
					.build();

			@SuppressWarnings("resource")
			KubernetesClient client = new DefaultKubernetesClient(config)
			.inNamespace(properties.getNamespace());

			return new KubernetesTaskLauncher(properties, client, containerFactory);
		};
	}
}
