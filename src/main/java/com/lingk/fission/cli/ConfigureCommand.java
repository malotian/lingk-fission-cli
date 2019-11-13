package com.lingk.fission.cli;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "c", description = "configure-fission")
public class ConfigureCommand implements Callable<Integer> {

	@Option(names = { "region" }, required = true, arity = "0..1", description = "region name", interactive = true)
	String region;

	@Option(names = { "cluster" }, required = true, arity = "0..1", description = "cluster name", interactive = true)
	String cluster;

	final ObjectMapper mapper = new ObjectMapper();

	public Integer call() throws Exception {
		Application.LOG.debug("Region: {}", region);
		Application.LOG.debug("Cluster: {}", cluster);

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("aws", "eks", "--region", region, "update-kubeconfig", "--name", cluster);
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

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}
}