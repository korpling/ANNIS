/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.administration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import annis.AnnisBaseRunner;
import annis.UsageException;

public class AnnisAdminRunner extends AnnisBaseRunner {
	
	private static Logger log = Logger.getLogger(AnnisAdminRunner.class);

	// API for corpus administration
	@Autowired private CorpusAdministration corpusAdministration;
	private boolean keepStagingArea;
	
	public static void main(String[] args) {
		// get Runner from Spring
		AnnisBaseRunner.getInstance("annisAdminRunner", "annis/administration/AnnisAdminRunner-context.xml").run(args);
	}

  @Override
	public void run(String[] args) {
		
		// print help if no argument is given
		if (args.length == 0)
			throw new UsageException("missing command");
		
		// first parameter is command
		String command = args[0];
		
		// following parameters are arguments for the command
		List<String> commandArgs = Arrays.asList(args).subList(1, args.length);

		// command: help
		if ("help".equals(command) || "--help".equals(command)) {
			usage(null);
			
		// command: init
		} else if ("init".equals(command)) {
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
			
		// unknown command
		} else {
			throw new UsageException("Unknown command: " + command);
		}
	}

	static class OptionBuilder
  {
		
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
		
		corpusAdministration.initializeDatabase(host, port, database, user, password, defaultDatabase, superUser, superPassword);
	}

	private void doImport(List<String> commandArgs) {
		if (commandArgs.isEmpty())
			throw new UsageException("Where can I find the corpus you want to import?");

		corpusAdministration.importCorpora(!keepStagingArea, commandArgs);
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
		corpusAdministration.deleteCorpora(ids);
	}

	private void doList() {
		List<Map<String, Object>> stats = corpusAdministration.listCorpusStats();
		
		if (stats.isEmpty()) {
			System.out.println("Annis database is empty.");
			return;
		}

		printTable(stats);
	}

	private void doStats() {
		printTable(corpusAdministration.listTableStats());
	}

	private void doIndexes() {
		for (String indexDefinition : corpusAdministration.listUsedIndexes())
			System.out.println(indexDefinition + ";");
		for (String indexDefinition : corpusAdministration.listUnusedIndexes())
			System.out.println("-- " + indexDefinition + ";");
	}

	private void usage(String error) {
		Resource resource = new ClassPathResource("annis/administration/usage.txt");
		BufferedReader reader = null;
    try
    {
			reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
			for (String line = reader.readLine(); line != null; line = reader.readLine())
				System.out.println(line);
		} catch (IOException e) {
			log.warn("could not read usage information: " + e.getMessage());
		}
    finally
    {
      if(reader != null)
      {
        try
        {
          reader.close();
        }
        catch (IOException ex)
        {
          java.util.logging.Logger.getLogger(AnnisAdminRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
		if (error != null)
			error(error);
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
				final Object value = row.get(column);
				if (value == null)
					continue;
				int length = value.toString().length();
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
		String s = o != null ? o.toString() : "";
		if (s.length() > length)
			return s;

		StringBuilder padded = new StringBuilder();
		for (int i = 0; i < length - s.length(); ++i)
			padded.append(" ");
		padded.append(o);
		return padded.toString();
	}

	///// Getter / Setter

	public CorpusAdministration getCorpusAdministration() {
		return corpusAdministration;
	}

	public void setCorpusAdministration(CorpusAdministration administration) {
		this.corpusAdministration = administration;
	}

	public boolean isKeepStagingArea() {
		return keepStagingArea;
	}

	public void setKeepStagingArea(boolean keepStagingArea) {
		this.keepStagingArea = keepStagingArea;
	}

}
