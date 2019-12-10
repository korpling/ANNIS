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
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import annis.AnnisBaseRunner;
import annis.AnnisRunnerException;
import annis.UsageException;
import annis.dao.QueryDao;
import annis.dao.QueryDaoImpl;
import annis.dao.ShortenerDao;
import annis.dao.autogenqueries.QueriesGenerator;
import annis.service.objects.AnnisCorpus;

public class AnnisAdminRunner extends AnnisBaseRunner {

    private static final Logger log = LoggerFactory.getLogger(AnnisAdminRunner.class);
    // API for corpus administration

    private CorpusAdministration corpusAdministration;
    private final QueryDao queryDao;

    private final QueriesGenerator queriesGenerator;

    public AnnisAdminRunner() throws GraphANNISException {
        this.queryDao = QueryDaoImpl.create();
        this.queriesGenerator = QueriesGenerator.create(this.queryDao);

        DeleteCorpusDao deleteCorpusDao = DeleteCorpusDao.create(queryDao);
        AdministrationDao adminDao = AdministrationDao.create(queryDao, deleteCorpusDao);

        this.corpusAdministration = CorpusAdministration.create(adminDao, ShortenerDao.create());
    }

    public static void main(String[] args) {
        // get Runner from Spring
        try {
            AnnisBaseRunner.setupLogging(true);
            new AnnisAdminRunner().run(args);
        } catch (AnnisRunnerException ex) {
            log.error(ex.getMessage() + " (error code " + ex.getExitCode() + ")", ex);
            System.exit(ex.getExitCode());
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
            System.exit(1);
        }
    }

    @Override
    public void run(String[] args) throws InterruptedException {

        // print help if no argument is given
        if (args.length == 0) {
            usage(null);
        } else {

            // first parameter is command
            String command = args[0];

            // following parameters are arguments for the command
            List<String> commandArgs = Arrays.asList(args).subList(1, args.length);

            if ("help".equals(command) || "--help".equals(command)) {
                usage(null);
            } else if ("import".equals(command)) {
                doImport(commandArgs);
            } else if ("migrate-url-shortener".equals(command)) {
                doMigrateUrlShortener(commandArgs);
            } else if ("export".equals(command)) {
                doExport(commandArgs);
            } else if ("delete".equals(command)) {
                doDelete(commandArgs);
            } else if ("list".equals(command)) {
                doList();
            } else if ("genexamples".equals(command)) {
                doGenerateExampleQueries(commandArgs);
            } else if ("delexamples".equals(command)) {
                doDeleteExampleQueries(commandArgs);
            } else if ("cleanup-data".equals(command)) {
                doCleanupData(commandArgs);
            } else if ("check-db-schema-version".equals(command)) {
                doCheckDBSchemaVersion();
            } else if ("dump".equals(command)) {
                doDumpTable(commandArgs);
            } else if ("restore".equals(command)) {
                doRestoreTable(commandArgs);
            } else {
                throw new UsageException("Unknown command: " + command);
       
            }
        }
        
        getQueryDao().shutdown();
    }

    /**
     * @return the queriesGenerator
     */
    public QueriesGenerator getQueriesGenerator() {
        return queriesGenerator;
    }

    static class OptionBuilder {

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

        public OptionBuilder addToggle(String opt, String longOpt, String description) {
            options.addOption(opt, longOpt, false, description);
            return this;
        }

        public Options createOptions() {
            return options;
        }
    }

    private void doImport(List<String> commandArgs) {
        Options options = new OptionBuilder()
                .addToggle("o", "overwrite", "Overwrites a corpus, when it is already stored in the database.")
                .addParameter("m", "mail", "e-mail adress to where status updates should be send")
                .addParameter("a", "alias", "an alias name for this corpus")
                .addLongParameter("service-url",
                        "Get the paths to import from the ANNIS service accessible by this URL. This helps to migrate "
                                + "corpora from an older instance. "
                                + "Make sure that the ANNIS Service and this script run on the same machine (otherwise the paths are not valid.")
                .addLongParameter("service-username", "Optional username when using the ANNIS service")
                .addLongParameter("service-password", "Optional password when using the ANNIS service").createOptions();

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmdLine = parser.parse(options, commandArgs.toArray(new String[commandArgs.size()]));

            List<String> corpusPaths;
            String serviceURL = cmdLine.getOptionValue("service-url");
            String userName = cmdLine.getOptionValue("service-username");
            String password = cmdLine.getOptionValue("service-password");
            if (userName != null && password == null && System.console() != null) {
                // no password given as argument, ask the user interactively
                char[] providedPassword = System.console().readPassword("Password for user '%s': ", userName);
                password = new String(providedPassword);
                Arrays.fill(providedPassword, ' ');
            }

            if (serviceURL != null) {
                corpusPaths = getCorpusPathsFromService(serviceURL, userName, password);
            } else {
                if (cmdLine.getArgList().isEmpty()) {
                    throw new ParseException("Where can I find the corpus you want to import?");
                }
                corpusPaths = cmdLine.getArgList();
            }
            password = null;

            boolean overwrite = cmdLine.hasOption('o');
            corpusAdministration.importCorporaSave(overwrite, cmdLine.getOptionValue("alias"),
                    cmdLine.getOptionValue("mail"), false, corpusPaths);

        } catch (ParseException ex) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("annis-admin.sh import [OPTION] DIR1 DIR2 ...", options);
        }
    }

    void doMigrateUrlShortener(List<String> commandArgs) {
        Options options = new OptionBuilder().addLongParameter("service-url",
                "Get the paths to import from the ANNIS service accessible by this URL. This helps to migrate "
                        + "corpora from an older instance. "
                        + "Make sure that the ANNIS Service and this script run on the same machine (otherwise the paths are not valid.")
                .addLongParameter("service-username", "Optional username when using the ANNIS service")
                .addLongParameter("service-password", "Optional password when using the ANNIS service").addToggle("s",
                        "skip-existing", "Skip all existing UUIDs that already have been migrated to the new instance.")
                .createOptions();

        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmdLine = parser.parse(options, commandArgs.toArray(new String[commandArgs.size()]));

            List<String> urlShortenerFiles = cmdLine.getArgList();
            if (urlShortenerFiles.isEmpty()) {
                throw new ParseException("Where can I find the url shortener export files you want to migrate?");
            }

            final boolean skipExisting = cmdLine.hasOption('s');

            String userName = cmdLine.getOptionValue("service-username");
            String password = cmdLine.getOptionValue("service-password");
            if (userName != null && password == null && System.console() != null) {
                // no password given as argument, ask the user interactively
                char[] providedPassword = System.console().readPassword("Password for user '%s': ", userName);
                password = new String(providedPassword);
                Arrays.fill(providedPassword, ' ');
            }

            Multimap<QueryStatus, URLShortenerDefinition> failedQueries = HashMultimap.create();

            int successfulQueries = corpusAdministration.migrateUrlShortener(urlShortenerFiles,
                    cmdLine.getOptionValue("service-url"), userName, password, skipExisting, failedQueries);
            password = null;

            // output summary and detailed list of failed queries
            Collection<URLShortenerDefinition> unknownCorpusQueries = failedQueries.get(QueryStatus.UnknownCorpus);

            System.out.println();

            if (!unknownCorpusQueries.isEmpty()) {
                Map<String, Integer> unknownCorpusCount = new TreeMap<>();
                for (URLShortenerDefinition q : unknownCorpusQueries) {
                    for (String c : q.getUnknownCorpora()) {
                        int oldCount = unknownCorpusCount.getOrDefault(c, 0);
                        unknownCorpusCount.put(c, oldCount + 1);
                    }
                }
                String unknownCorpusCaption = "Unknown corpus (" + unknownCorpusCount.size() + " unknown corpora and "
                        + unknownCorpusQueries.size() + " queries)";
                System.out.println(unknownCorpusCaption);
                System.out.println(Strings.repeat("=", unknownCorpusCaption.length()));
                System.out.println();

                for (Map.Entry<String, Integer> e : unknownCorpusCount.entrySet()) {
                    System.out.println("Corpus \"" + e.getKey() + "\": " + e.getValue() + " queries");
                }
                System.out.println();
            }

            printProblematicQueries("UUID already exists", failedQueries.get(QueryStatus.UUIDExists));
            printProblematicQueries("Count different", failedQueries.get(QueryStatus.CountDiffers));
            printProblematicQueries("Match list different", failedQueries.get(QueryStatus.MatchesDiffer));
            printProblematicQueries("Timeout", failedQueries.get(QueryStatus.Timeout));
            printProblematicQueries("Other server error", failedQueries.get(QueryStatus.ServerError));
            printProblematicQueries("Empty corpus list", failedQueries.get(QueryStatus.EmptyCorpusList));
            printProblematicQueries("Failed", failedQueries.get(QueryStatus.Failed));

            String summaryString = "+ Successful: " + successfulQueries + " from "
                    + (successfulQueries + failedQueries.size()) + " +";
            System.out.println(Strings.repeat("+", summaryString.length()));
            System.out.println(summaryString);
            System.out.println(Strings.repeat("+", summaryString.length()));

        } catch (ParseException ex) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("annis-admin.sh migrate-url-shortener [OPTION] FILE", options);
        }
    }

    private void printProblematicQueries(String statusCaption, Collection<URLShortenerDefinition> queries) {
        if (queries != null && !queries.isEmpty()) {
            String captionWithCount = statusCaption + " (sum: " + queries.size() + ")";
            System.out.println(captionWithCount);
            System.out.println(Strings.repeat("=", captionWithCount.length()));
            System.out.println();

            for (URLShortenerDefinition q : queries) {
                if (q.getQuery() != null && q.getQuery().getCorpora() != null) {
                    System.out.println("Corpus: \"" + q.getQuery().getCorpora() + "\"");
                }
                System.out.println("UUID: \"" + q.getUuid() + "\"");
                if (q.getQuery() != null && q.getQuery().getQuery() != null) {
                    System.out.println("Query:");
                    System.out.println(q.getQuery().getQuery().trim());
                }
                if (q.getErrorMsg() != null) {
                    System.out.println("Error: " + q.getErrorMsg());
                }

                System.out.println("-------");
            }
            System.out.println();
        }
    }

    private List<String> getCorpusPathsFromService(String uri, String username, String password) {
        Client client = ClientBuilder.newClient();
        if (username != null && password != null) {
            HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basic(username, password);
            client.register(authFeature);
        }
        WebTarget target = client.target(uri).path("annis").path("query").path("corpora");

        List<AnnisCorpus> corpora = target.request(MediaType.APPLICATION_XML_TYPE)
                .get(new GenericType<List<AnnisCorpus>>() {
                });
        List<String> corpusPaths = new LinkedList<>();
        for (AnnisCorpus c : corpora) {
            if (c.getSourcePath() != null) {
                corpusPaths.add(c.getSourcePath());
            }
        }
        return corpusPaths;
    }

    private void doExport(List<String> commandArgs) {
        Options options = new OptionBuilder().createOptions();

        try {

            CommandLineParser parser = new DefaultParser();
            CommandLine cmdLine = parser.parse(options, commandArgs.toArray(new String[commandArgs.size()]));

            if (cmdLine.getArgs().length < 2) {
                throw new ParseException("Needs two arguments: corpus name and output folder");
            }
            queryDao.exportCorpus(cmdLine.getArgs()[0], new File(cmdLine.getArgs()[1]));

        } catch (ParseException ex) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("annis-admin.sh export CORPUS DIR ...", options);
        } catch (GraphANNISException e) {
            log.error("Could not export corpus", e);
        }
    }

    private void doDelete(List<String> commandArgs) {
        if (commandArgs.isEmpty()) {
            throw new UsageException("What corpus do you want to delete?");
        }

        // convert ids from string to int
        List<String> names = new ArrayList<>();
        for (String n : commandArgs) {
            names.add(n);
        }
        corpusAdministration.deleteCorpora(names);
    }

    private void doList() {
        List<AnnisCorpus> corpora = queryDao.listCorpora();

        if (corpora.isEmpty()) {
            System.out.println("ANNIS database is empty.");
            return;
        }

        List<Map<String, Object>> asTable = new LinkedList<>();

        for (AnnisCorpus c : corpora) {
            asTable.add(c.asTableRow());
        }

        printTable(asTable);
    }

    private void doDeleteExampleQueries(List<String> commandArgs) {
        if (commandArgs == null || commandArgs.isEmpty()) {
            queriesGenerator.delExampleQueries(null);
        } else {
            queriesGenerator.delExampleQueries(commandArgs);
        }
    }

    private void doCleanupData(List<String> commandArgs) {
        corpusAdministration.cleanupData();
    }

    private void doGenerateExampleQueries(List<String> commandArgs) {
        if (commandArgs == null || commandArgs.isEmpty()) {
            queriesGenerator.generateQueries(false);
        } else {
            boolean overwrite = false;

            for (String c : commandArgs) {
                if ("--overwrite".equals(c) || "-o".equals(c)) {
                    overwrite = true;
                }
            }

            for (String corpusNames : commandArgs) {
                if (corpusNames.startsWith("--") || corpusNames.startsWith("-")) {
                    continue;
                }
                queriesGenerator.generateQueries(corpusNames, overwrite);
            }
        }
    }

    public void doCheckDBSchemaVersion() {
        if (corpusAdministration.checkDatabaseSchemaVersion()) {
            out.println("Correct ANNNIS database schema version.");
            System.exit(0);
        } else {
            out.println("Wrong ANNNIS database schema version.");
            System.exit(1);
        }

    }

    public void doDumpTable(List<String> commandArgs) {
        Preconditions.checkArgument(commandArgs.size() >= 2, "Need the table name and the output file as argument: annis-admin.sh dump <table> <file>");
        corpusAdministration.dumpTable(commandArgs.get(0), new File(commandArgs.get(1)));
    }

    public void doRestoreTable(List<String> commandArgs) {
        Preconditions.checkArgument(commandArgs.size() >= 2, "Need the table name and the input file as argument: annis-admin.sh restore <table> <file>");
        corpusAdministration.restoreTable(commandArgs.get(0), new File(commandArgs.get(1)));
    }

    private void usage(String error) {
        InputStream resource = this.getClass().getResourceAsStream("/annis/administration/usage.txt");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource, "UTF-8"));) {

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                System.out.println(line);
            }
        } catch (IOException e) {
            log.warn("could not read usage information: " + e.getMessage());
        }
        if (error != null) {
            error(error);
        }
    }

    private void printTable(List<Map<String, Object>> table) {
        // use first element to get metadata (like column names)
        Map<String, Object> first = table.get(0);
        List<String> columnNames = new ArrayList<>(first.keySet());

        // determine length of column
        Map<String, Integer> columnSize = new HashMap<>();
        for (String column : columnNames) {
            columnSize.put(column, column.length());
        }
        for (Map<String, Object> row : table) {
            for (Map.Entry<String, Object> e : row.entrySet()) {
                String column = e.getKey();
                final Object value = e.getValue();
                if (value == null) {
                    continue;
                }
                int length = value.toString().length();
                if (columnSize.get(column) < length) {
                    columnSize.put(column, length);
                }
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
        if (s.length() > length) {
            return s;
        }

        StringBuilder padded = new StringBuilder();
        for (int i = 0; i < length - s.length(); ++i) {
            padded.append(" ");
        }
        padded.append(o);
        return padded.toString();
    }

    ///// Getter / Setter
    public CorpusAdministration getCorpusAdministration() {
        return corpusAdministration;
    }

    public void setCorpusAdministration(CorpusAdministration corpusAdministration) {
        this.corpusAdministration = corpusAdministration;
    }

    public QueryDao getQueryDao() {
        return queryDao;
    }

}
