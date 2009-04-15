package annisservice.administration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import annisservice.AnnisBaseRunner;
import annisservice.administration.exceptions.AdministrationException;
import annisservice.administration.exceptions.UsageException;
import annisservice.exceptions.AnnisServiceException;
import annisservice.handler.GetWekaHandler;

public class AnnisAdminRunner extends AnnisBaseRunner {
	
	private static Logger log = Logger.getLogger(AnnisAdminRunner.class);

	private GetWekaHandler getWekaHandler; // for weka export
	
	private CorpusAdministration administration;
	
	public AnnisAdminRunner(CorpusAdministration administration) {
		this.administration = administration;
	}

	public AnnisAdminRunner() {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("annis-admin.xml");
		this.administration = (CorpusAdministration) ctx.getBean("administration");
		this.getWekaHandler = (GetWekaHandler) ctx.getBean("getWekaHandler");
	}
	
	public static void main(String[] args) {
		AnnisAdminRunner runner = new AnnisAdminRunner();
		try {
			runner.run(args);
		} catch (UsageException e) {
			runner.error(e.getMessage());
		} catch (AdministrationException e) {
			System.out.println("ERROR: " + e.getMessage());
		} catch (Throwable e) {
			log.warn("Uncaught exception", e);
			System.out.println("ERROR: " + e.getMessage());
		}
	}

	void run(String[] args) throws AdministrationException {
		
		// log messages to console
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%d{HH:MM:ss.SSS} %C{1} %p: %m\n")));
		
		// print help if no argument is given
		if (args.length == 0)
			throw new UsageException("missing command");
		
		// first parameter is command
		String command = args[0];
		
		// following parameters are arguments for the command
		List<String> commandArgs = Arrays.asList(args).subList(1, args.length);

		// command: init
		if ("init".equals(command)) {
			doInit(commandArgs);
		
		// command: import
		} else if ("import".equals(command)) {
			doImport(commandArgs);
			
		// command: delete
		} else if ("delete".equals(command)) {
			doDelete(commandArgs);
		
		// command status
		} else if ("list".equals(command)) {
			doList();
			
		// command: stats
		} else if ("stats".equals(command)) {
			doStats();

		// command: indexes
		} else if ("indexes".equals(command)) {
			doIndexes();
			
		// command: arff
		} else if ("weka".equals(command)) {
			doWeka(commandArgs);
			
		// unknown command
		} else {
			throw new UsageException("Unknown command: " + command);
		}
	}

	class OptionBuilder {
		
		private Options options;
		
		public OptionBuilder() {
			options = new Options();
		}
		
		public OptionBuilder addParameter(String opt, String longOpt, String description) {
			options.addOption(opt, longOpt, true, description);
			return this;
		}
		
		public OptionBuilder addLongParameter(String longOpt, String description) {
			options.addOption(null, longOpt, true, description);
			return this;
		}
		
		public OptionBuilder addRequiredParameter(String opt, String longOpt, String description) {
			Option option = new Option(opt, longOpt, true, description);
			option.setRequired(true);
			options.addOption(option);
			return this;
		}
		
		public OptionBuilder addToggle(String opt, String longOpt, boolean hasArg, String description) {
			options.addOption(opt, longOpt, false, description);
			return this;
		}
		
		public Options createOptions() {
			return options;
		}
	}
	
	private String getValue(Options options, String option) {
		return options.getOption(option).getValue();
	}

	private String getDefaultValue(Options options, String option,
			String defaultValue) {
		return options.getOption(option).getValue(defaultValue);
	}
	
	private void doInit(List<String> commandArgs) {
		Options options = new OptionBuilder()
			.addParameter("h", "host", "database server host (defaults to localhost)")
			.addLongParameter("port", "database server port")
			.addRequiredParameter("d", "database", "name of the Annis database (REQUIRED)")
			.addRequiredParameter("u", "user", "name of the Annis user (REQUIRED)")
			.addRequiredParameter("p", "password", "password of the Annis suer (REQUIRED)")
			.addParameter("D", "defaultdb", "name of the PostgreSQL default database (defaults to \"postgres\")")
			.addParameter("U", "superuser", "name of a PostgreSQL super user (defaults to \"postgres\")")
			.addParameter("P", "superpassword", "password of a PostgreSQL super user")
			.createOptions();
		CommandLineParser parser = new PosixParser();
		try {
			parser.parse(options, commandArgs.toArray(new String[] { }));
		} catch (ParseException e) {
			throw new UsageException(e.getMessage());
		}
		
		String host = getDefaultValue(options, "host", "localhost");
		String port = getDefaultValue(options, "port", "5432");
		String database = getValue(options, "database");
		String user = getValue(options, "user");
		String password = getValue(options, "password");
		String defaultDatabase = getDefaultValue(options, "defaultdb", "postgres");
		String superUser = getDefaultValue(options, "superuser", "postgres");
		String superPassword = getValue(options, "superpassword");
		
		administration.initializeDatabase(host, port, database, user, password, defaultDatabase, superUser, superPassword);
	}

	private void doImport(List<String> commandArgs) {
		if (commandArgs.isEmpty())
			throw new UsageException("Where can I find the corpus you want to import?");

		administration.importCorpora(commandArgs);
	}

	private void doDelete(List<String> commandArgs) {
		if (commandArgs.isEmpty())
			throw new UsageException("What corpus do you want to delete?");
		
		// convert ids from string to int
		List<Long> ids = new ArrayList<Long>();
		for (String id : commandArgs)
			try {
				ids.add(Long.parseLong(id));
			} catch (NumberFormatException e) {
				throw new UsageException("Not a number: " + id);
			}
		administration.deleteCorpora(ids);
	}

	private void doList() {
		List<Map<String, Object>> stats = administration.listCorpusStats();
		
		if (stats.isEmpty()) {
			System.out.println("Annis database is empty.");
			return;
		}

		printTable(stats);
	}

	private void doStats() {
		printTable(administration.listTableStats());
	}

	private void doIndexes() {
		for (String indexDefinition : administration.listUsedIndexes())
			System.out.println(indexDefinition + ";");
		for (String indexDefinition : administration.listUnusedIndexes())
			System.out.println("-- " + indexDefinition + ";");
	}
	
	private void doWeka(List<String> commandArgs) {
		Options options = new OptionBuilder()
			.addRequiredParameter("c", "corpora", "list of corpus ids (REQUIRED)")
			.createOptions();
		
		CommandLineParser parser = new PosixParser();
		CommandLine commandLine = null;
		try {
			commandLine = parser.parse(options, commandArgs.toArray(new String[] { }));
		} catch (ParseException e) {
			throw new UsageException(e.getMessage());
		}

		String corpora = getValue(options, "corpora");
		String[] corporaStrings = corpora.split(",");
		List<Long> corpusList = new ArrayList<Long>();
		for (String corpus : corporaStrings)
			corpusList.add(Long.parseLong(corpus));
		
		String query = StringUtils.join(commandLine.getArgs());
//		System.out.println(query);

		try {
			Map<String, Object> args = new HashMap<String, Object>();
			args.put(getWekaHandler.CORPUS_LIST, corpusList);
			args.put(getWekaHandler.ANNIS_QUERY, query);
			List<List<String>> result = (List<List<String>>) getWekaHandler.handleRequest(args);
			
			if (result == null) {
				System.out.println("no matches found");
				return;
			}
			
			
			List<String> header = result.get(0);
			result.remove(0);
			
			System.out.println("@relation test");
			System.out.println();
			for (String attribute : header) {
				System.out.println("@attribute " + attribute + " string");
			}
			System.out.println();
			System.out.println("@data");
			System.out.println();
			
			for (List<String> line : result) {
				StringBuffer sb = new StringBuffer();
				for (String field : line) {
					sb.append("'");
					sb.append(field);
					sb.append("',");
				}
				sb.setLength(sb.length() - ",".length());
				System.out.println(sb);
			}
		} catch (AnnisServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void error(String error) {
		Resource resource = new ClassPathResource("annisservice/administration/usage.txt");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
				System.out.println(line);
		} catch (IOException e) {
			log.warn("could not read usage information: " + e.getMessage());
		}
		if (error != null) {
			System.out.println();
			System.out.println("ERROR: " + error);
		}
	}

	private void printTable(List<Map<String, Object>> table) {
		// use first element to get metadata (like column names)
		Map<String, Object> first = table.get(0);
		List<String> columnNames = new ArrayList<String>(first.keySet());
		
		// determine length of column
		Map<String, Integer> columnSize = new HashMap<String, Integer>();
		for (String column : columnNames)
			columnSize.put(column, column.length());
		for (Map<String, Object> row : table) {
			for (String column : row.keySet()) {
				int length = row.get(column).toString().length();
				if (columnSize.get(column) < length)
					columnSize.put(column, length);
			}
		}
		
		// print header
		StringBuffer sb = new StringBuffer();
		for (String column : columnNames) {
			sb.append(pad(column, columnSize.get(column)));
			sb.append(" | ");
		}
		sb.setLength(sb.length() - " | ".length());
		System.out.println(sb);
		
		// print values
		for (Map<String, Object> row : table) {
			sb = new StringBuffer();
			for (String column : columnNames) {
				sb.append(pad(row.get(column), columnSize.get(column)));
				sb.append(" | ");
			}
			sb.setLength(sb.length() - " | ".length());
			System.out.println(sb);
		}
	}
	
	private String pad(Object o, int length) {
		String s = o.toString();
		if (s.length() > length)
			return s;

		StringBuffer padded = new StringBuffer();
		for (int i = 0; i < length - s.length(); ++i)
			padded.append(" ");
		padded.append(o);
		return padded.toString();
	}

	///// Getter / Setter

	public CorpusAdministration getAdministration() {
		return administration;
	}

	public void setAdministration(CorpusAdministration administration) {
		this.administration = administration;
	}

}
