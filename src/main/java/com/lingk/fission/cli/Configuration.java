package com.lingk.fission.cli;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Configuration {
	static Configuration singleton;

	public static Configuration getInstance() {
		return Configuration.singleton;
	}

	public static void setInstance(Configuration instance) {
		Configuration.singleton = instance;
	}

	@Value("${oauth2.client.id}")
	String clientId;

	@Value("${oauth2.client.secret}")
	String clientSecret;

	@Value("${oauth2.token.uri}")
	String tokenUri;

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public String getTokenUri() {
		return tokenUri;
	}

}
