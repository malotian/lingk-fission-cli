package com.lingk.fission.cli;

import java.io.StringWriter;
import java.util.concurrent.Callable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
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

	@Option(names = { "cluster" }, arity = "0..1", hidden = true, description = "cluster name")
	String cluster;

	final ObjectMapper mapper = new ObjectMapper();

	@Option(names = { "password" }, required = true, arity = "0..1", description = "auth0 password", interactive = true)
	String password;

	@Option(names = { "region" }, required = true, arity = "0..1", description = "region name", interactive = true)
	String region;

	@Option(names = { "username" }, required = true, arity = "0..1", description = "auth0 psername", interactive = true)
	String username;

	@Override
	public Integer call() throws Exception {
		final RestTemplate restTemplate = new RestTemplate();

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		LoginAndConfigureCommand.LOG.debug("username: {}", username);
		LoginAndConfigureCommand.LOG.debug("password: {}", password);
		LoginAndConfigureCommand.LOG.debug("client_id: {}", Configuration.getInstance().getClientId());
		LoginAndConfigureCommand.LOG.debug("client_secret: {}", Configuration.getInstance().getClientSecret());

		final MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "password");
		map.add("username", username);
		map.add("password", password);
		map.add("client_id", Configuration.getInstance().getClientId());
		map.add("client_secret", Configuration.getInstance().getClientSecret());
		final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

		final ResponseEntity<String> response = restTemplate.postForEntity(Configuration.getInstance().getTokenUri(), request, String.class);
		final JsonNode json = mapper.readTree(response.getBody());
		StringWriter helper = new StringWriter();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.writeValue(helper, json);
		LoginAndConfigureCommand.LOG.debug("response: {}", helper);

		final Jwt jwt = JwtHelper.decode(json.get("id_token").asText());
		helper = new StringWriter();
		final JsonNode claims = mapper.readTree(jwt.getClaims());
		mapper.writeValue(helper, claims);

		final DateTime dt = new DateTime(claims.get("https://fission.lingkcore.com/aws.session").get("Expiration").asText());
		LoginAndConfigureCommand.LOG.info("session will be valid till: " + dt.toString(DateTimeFormat.fullDateTime()));

		final DefaultExecutor executor = new DefaultExecutor();
		final String AWS_CONFIGURE_SET_AWS_ACCESS_KEY_ID = "aws configure set aws_access_key_id "
				+ claims.get("https://fission.lingkcore.com/aws.session").get("AccessKeyId").asText() + " --profile lingk-fission";
		final String AWS_CONFIGURE_SET_AWS_SECRET_ACCESS_KEY = "aws configure set aws_secret_access_key "
				+ claims.get("https://fission.lingkcore.com/aws.session").get("SecretAccessKey").asText() + " --profile lingk-fission";
		final String AWS_CONFIGURE_SET_AWS_SESSION_TOKEN = "aws configure set aws_session_token "
				+ claims.get("https://fission.lingkcore.com/aws.session").get("SessionToken").asText() + " --profile lingk-fission";

		int exitValue = 0;
		exitValue = executor.execute(CommandLine.parse(AWS_CONFIGURE_SET_AWS_ACCESS_KEY_ID));
		LoginAndConfigureCommand.LOG.info("executed: {}, exitCode: {}", AWS_CONFIGURE_SET_AWS_ACCESS_KEY_ID, exitValue);
		exitValue = executor.execute(CommandLine.parse(AWS_CONFIGURE_SET_AWS_SECRET_ACCESS_KEY));
		LoginAndConfigureCommand.LOG.info("executed: {}, exitCode: {}", AWS_CONFIGURE_SET_AWS_SECRET_ACCESS_KEY, exitValue);
		exitValue = executor.execute(CommandLine.parse(AWS_CONFIGURE_SET_AWS_SESSION_TOKEN));
		LoginAndConfigureCommand.LOG.info("executed: {}, exitCode: {}", AWS_CONFIGURE_SET_AWS_SESSION_TOKEN, exitValue);

		return ConfigureCommand.execute(region, cluster);
	}
}