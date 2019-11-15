package com.lingk.fission.cli;

import java.util.concurrent.Callable;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "c", description = "configure-fission")
public class ConfigureCommand implements Callable<Integer> {

	@Option(names = { "region" }, required = true, arity = "0..1", description = "region name", interactive = true)
	String region;

	@Option(names = { "cluster" }, required = true, arity = "0..1", description = "cluster name", interactive = true)
	String cluster;

	static Logger LOG = LoggerFactory.getLogger(ConfigureCommand.class);

	public Integer call() throws Exception {
		return execute(region, cluster);
	}

	public static Integer execute(String region, String cluster) throws Exception {
		DefaultExecutor executor = new DefaultExecutor();
		String AWS_EKS_KUBE_CONFIG = "aws eks --region " + region + " update-kubeconfig --name " + cluster + " --profile lingk-fission";

		int exitCode = executor.execute(CommandLine.parse(AWS_EKS_KUBE_CONFIG));
		LOG.info("executed: {}, exitCode: {}", AWS_EKS_KUBE_CONFIG, exitCode);
		return 0;

	}
}