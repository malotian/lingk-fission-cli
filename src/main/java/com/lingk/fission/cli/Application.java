package com.lingk.fission.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@SpringBootApplication
@Command(name = "com.lingk.fission.cli.Application", synopsisSubcommandLabel = "COMMAND", subcommands = { ConfigureCommand.class, LoginAndConfigureCommand.class })
public class Application implements CommandLineRunner, Runnable {
	static Logger LOG = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	Configuration configuration;

	@Override
	public void run(String... args) {
		Configuration.setInstance(configuration);
		int exitCode = new CommandLine(this).execute(args);
		System.exit(exitCode);
	}

	@Spec
	CommandSpec spec;

	public void run() {
		throw new ParameterException(spec.commandLine(), "Missing required subcommand");
	}
}
