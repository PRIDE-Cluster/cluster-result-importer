package uk.ac.ebi.pride.tools.cluster.pride_cluster_importer;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;


@SuppressWarnings("static-access")
public class CliOptions {

	public enum OPTIONS {
		// VARIABLES
		FILE("in"),
		// ACTIONS
		HELP("help");

		private String value;

		OPTIONS(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	private static final Options options = new Options();

	static {
		// VARIABLES		
		Option outpath = OptionBuilder
				.hasArg()
				.withDescription("the file to import into the database.")
				.create(OPTIONS.FILE.getValue());
		options.addOption(outpath);
		
		// ACTIONS
		Option help = new Option(
				OPTIONS.HELP.toString(),
				"print this message.");
		options.addOption(help);
		
	}

	public static Options getOptions() {
		return options;
	}
}
