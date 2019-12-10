package com.lingk.fission.cli;

import java.text.MessageFormat;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "c", description = "configure-fission")
public class ConfigureCommand implements Callable<Integer> {

	static Logger LOG = LoggerFactory.getLogger(ConfigureCommand.class);

	public static Integer execute(String region, String cluster, String env) throws Exception {
		final DefaultExecutor executor = new DefaultExecutor();

		if (StringUtils.isEmpty(env)) {
			env = "dev";
		}
		
		if (StringUtils.isEmpty(cluster)) {
			cluster = MessageFormat.format("fission-{0}-{1}", env, region);
		}
		
		UUID uuid = UUID.randomUUID();
		final String AWS_EKS_KUBECONFIG = "aws eks --region " + region + " update-kubeconfig --name " + cluster + "-" + env + " --profile lingk-fission-cli-profile";

		int exitCode = executor.execute(CommandLine.parse(AWS_EKS_KUBECONFIG));
		ConfigureCommand.LOG.info("executed: {}, exitCode: {}", AWS_EKS_KUBECONFIG, exitCode);

		return 0;

	}

	@Option(names = { "cluster" }, arity = "0..1", hidden = true, description = "cluster name")
	String cluster;

	@Option(names = { "region" }, required = true, arity = "0..1", description = "region name", interactive = true)
	String region;

	@Option(names = { "env" }, required = true, arity = "0..1", description = "test/prod", interactive = true)
	String env;

	@Override
	public Integer call() throws Exception {
		return ConfigureCommand.execute(region, cluster, env);
	}
}