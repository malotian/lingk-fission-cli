package com.lingk.fission.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "lc", description = "login-interactivly-and-configure-fission")
public class LoginAndConfigureCommand implements Callable<Integer> {

	static Logger LOG = LoggerFactory.getLogger(LoginAndConfigureCommand.class);

	@Option(names = { "username" }, required = true, arity = "0..1", description = "auth0 psername", interactive = true)
	String username;

	@Option(names = { "password" }, required = true, arity = "0..1", description = "auth0 password", interactive = true)
	String password;

	@Option(names = { "region" }, required = true, arity = "0..1", description = "eks region name", interactive = true)
	String region;

	@Option(names = { "cluster" }, required = true, arity = "0..1", description = "eks cluster name", interactive = true)
	String cluster;

	final ObjectMapper mapper = new ObjectMapper();

	public Integer call() throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		LOG.debug("username: {}", username);
		LOG.debug("password: {}", password);
		LOG.debug("client_id: {}", Configuration.getInstance().getClientId());
		LOG.debug("client_secret: {}", Configuration.getInstance().getClientSecret());

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "password");
		map.add("username", username);
		map.add("password", password);
		map.add("client_id", Configuration.getInstance().getClientId());
		map.add("client_secret", Configuration.getInstance().getClientSecret());
		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(Configuration.getInstance().getTokenUri(), request, String.class);
		JsonNode json = mapper.readTree(response.getBody());
		StringWriter helper = new StringWriter();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(helper, json);
		LOG.debug("response: {}", helper);

		Jwt jwt = JwtHelper.decode(json.get("id_token").asText());
		helper = new StringWriter();
		JsonNode claims = mapper.readTree(jwt.getClaims());
		mapper.writeValue(helper, claims);
		LOG.info("setting AWS_ACCESS_KEY_ID={}", claims.get("https://fission.lingkcore.com/aws.session").get("awsaccessKeyId"));
		LOG.info("setting AWS_SECRET_ACCESS_KEY={}", claims.get("https://fission.lingkcore.com/aws.session").get("awssecretKey"));
		LOG.info("setting AWS_SESSION_TOKEN={}", claims.get("https://fission.lingkcore.com/aws.session").get("sessionToken"));

		ProcessBuilder processBuilder = new ProcessBuilder();
		Map<String, String> environment = processBuilder.environment();
		environment.put("AWS_ACCESS_KEY_ID", claims.get("https://fission.lingkcore.com/aws.session").get("awsaccessKeyId").asText());
		environment.put("AWS_SECRET_ACCESS_KEY", claims.get("https://fission.lingkcore.com/aws.session").get("awssecretKey").asText());
		environment.put("AWS_SESSION_TOKEN", claims.get("https://fission.lingkcore.com/aws.session").get("sessionToken").asText());

		String[] AWS_EKS_KUBE_CONFIG = { "aws", "eks", "--region", region, "update-kubeconfig", "--name", cluster };

		List<String> commands = new ArrayList<String>();
		commands.addAll(Arrays.asList(AWS_EKS_KUBE_CONFIG));
		LOG.info(String.join(" ", commands));

		processBuilder.command(commands);

		try {
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			int exitCode = process.waitFor();
			if (exitCode != 0)
				System.err.println("exited with code:" + exitCode);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return 0;
	}
}