/*
 * Copyright 2012 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.administration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.http.client.utils.URIBuilder;
import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;

import annis.AnnisRunnerException;
import annis.CommonHelper;
import annis.ServiceConfig;
import annis.dao.ShortenerDao;
import annis.exceptions.AnnisException;
import annis.service.objects.AnnisCorpus;
import annis.service.objects.ImportJob;
import annis.tabledefs.Table;
import annis.utils.ANNISFormatHelper;
import au.com.bytecode.opencsv.CSVReader;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
public class CorpusAdministration {

    private AdministrationDao administrationDao;
    private ShortenerDao shortenerDao;

    private final ServiceConfig cfg = ConfigFactory.create(ServiceConfig.class);

    private static final Logger log = LoggerFactory.getLogger(CorpusAdministration.class);

    public CorpusAdministration() {
    }

    public static CorpusAdministration create(AdministrationDao administrationDao, ShortenerDao shortenerDao) {
        CorpusAdministration corpusAdmin = new CorpusAdministration();
        corpusAdmin.setAdministrationDao(administrationDao);
        corpusAdmin.setShortenerDao(shortenerDao);

        corpusAdmin.checkDatabaseSchemaVersion();

        return corpusAdmin;
    }

    public void deleteCorpora(List<String> corpora) {
        // check if corpus exists
        List<AnnisCorpus> existingCorpora = administrationDao.getQueryDao().listCorpora(corpora);
        Set<String> existingCorpusNames = new HashSet<>();
        for (AnnisCorpus c : existingCorpora) {
            existingCorpusNames.add(c.getName());
        }
        for (String toDelete : corpora) {
            if (!existingCorpusNames.contains(toDelete)) {
                throw new AnnisRunnerException("Corpus does not exist (or is not a top-level corpus): " + toDelete, 51);
            }
        }
        log.info("Deleting corpora: " + corpora);
        getDeleteCorpusDao().deleteCorpora(corpora);
        log.info("Finished deleting corpora: " + corpora);
    }

    public void cleanupData() {
        administrationDao.cleanupData();
    }

    /**
     * Imports several corpora and catches a possible thrown
     * {@link DefaultAdministrationDao.ConflictingCorpusException} when the
     * overwrite flag is set to false.
     *
     *
     * @param overwrite
     *                              If set to false, a conflicting corpus is not
     *                              silently reimported.
     * @param aliasName
     *                              An common alias name for all imported corpora or
     *                              null
     * @param statusEmailAdress
     *                              an email adress for informating the admin about
     *                              statuses
     * @param waitForOtherTasks
     *                              If true wait for other imports to finish, if
     *                              false abort the import.
     * @param paths
     *                              Valid pathes to corpora.
     * @return True if all corpora where imported successfully.
     */
    public ImportStatus importCorporaSave(boolean overwrite, String aliasName, String statusEmailAdress,
            boolean waitForOtherTasks, List<String> paths) {

        // init the import stats. From the beginning everything is ok
        ImportStatus importStats = new ImportStatsImpl();
        importStats.setStatus(true);

        List<File> roots = new LinkedList<>();
        for (String path : paths) {
            File f = new File(path);

            if (f.isFile()) {
                // might be a ZIP-file
                try (ZipFile zip = new ZipFile(f);) {

                    // get the names of all corpora included in the ZIP file
                    // in order to get a folder name
                    Map<String, ZipEntry> corpora = ANNISFormatHelper.corporaInZipfile(zip);

                    // unzip and add all resulting corpora to import list
                    log.info("Unzipping " + f.getPath());
                    String corpusNames = Joiner.on(", ").join(corpora.keySet());
                    if(corpusNames.length() > 200) {
                        // some systems don't handle large file names well
                        corpusNames = corpusNames.substring(0, 200);
                    }
                    File outDir = createZIPOutputDir(corpusNames);
                    roots.addAll(unzipCorpus(outDir, zip));

                } catch (ZipException ex) {
                    log.error("" + f.getAbsolutePath() + " might not be a valid ZIP file and will be ignored", ex);
                } catch (IOException ex) {
                    log.error("IOException when importing file " + f.getAbsolutePath() + ", will be ignored", ex);
                }
            } else {
                try {
                    roots.addAll(ANNISFormatHelper.corporaInDirectory(f).values());
                } catch (IOException ex) {
                    log.error("Could not find any corpus in " + f.getPath(), ex);
                    importStats.setStatus(false);
                    importStats.addException(f.getAbsolutePath(), ex);
                }
            }
        } // end for each given path

        // import each corpus separately
        for (File r : roots) {
            try {
                log.info("Importing corpus from: " + r.getPath());
                if (getAdministrationDao().importCorpus(r.getPath(), aliasName, overwrite)) {
                    log.info("Finished import from: " + r.getPath());
                    getAdministrationDao().sendImportStatusMail(statusEmailAdress, r.getPath(),
                            ImportJob.Status.SUCCESS, null);
                } else {
                    importStats.setStatus(false);
                    getAdministrationDao().sendImportStatusMail(statusEmailAdress, r.getPath(), ImportJob.Status.ERROR,
                            null);
                }
            }

            catch (AdministrationDao.ConflictingCorpusException ex) {
                importStats.setStatus(false);
                importStats.addException(r.getPath(), ex);
                log.error("Error on conflicting top level corpus name for {}", r.getPath());
                getAdministrationDao().sendImportStatusMail(statusEmailAdress, r.getPath(), ImportJob.Status.ERROR,
                        ex.getMessage());
            } catch (Throwable ex) {
                importStats.setStatus(false);
                importStats.addException(r.getPath(), ex);
                log.error("Error on importing corpus", ex);
                getAdministrationDao().sendImportStatusMail(statusEmailAdress, r.getPath(), ImportJob.Status.ERROR,
                        ex.getMessage());
            }
        } // end for each corpus

        return importStats;
    }

    public int migrateUrlShortener(List<String> paths, String serviceURL, String username, String password,
            boolean skipExisting, Multimap<QueryStatus, URLShortenerDefinition> failedQueries) {
        if (paths == null || serviceURL == null) {
            return 0;
        }

        int successfulQueries = 0;

        Client client = ClientBuilder.newClient();
        if (username != null && password != null) {
            HttpAuthenticationFeature authFeature = HttpAuthenticationFeature.basic(username, password);
            client.register(authFeature);

            // test authentication and fail early
            String result = client.target(serviceURL).path("annis").path("admin").path("is-authenticated").request()
                    .get(String.class);
            Preconditions.checkArgument("true".equalsIgnoreCase(result), "Authentication failed");
        }

        WebTarget searchService = client.target(serviceURL).path("annis").path("query").path("search");

        for (String p : paths) {
            File urlShortenerFile = new File(p);
            if (urlShortenerFile.isFile()) {
                try (CSVReader csvReader = new CSVReader(new FileReader(urlShortenerFile), '\t')) {
                    String[] line;
                    while ((line = csvReader.readNext()) != null) {
                        if (line.length == 4) {

                            // parse URL
                            try {
                                URLShortenerDefinition q = URLShortenerDefinition.parse(line[3], line[0], line[2]);
                                if (q != null) {
                                    // check if all corpora exist in the new instance
                                    List<String> corpusNames = q.getQuery() == null || q.getQuery().getCorpora() == null
                                            ? new LinkedList<>()
                                            : new LinkedList<>(q.getQuery().getCorpora());
                                    Set<String> knownCorpora = getAdministrationDao().getQueryDao()
                                            .listCorpora(corpusNames).stream().map((c) -> c.getName())
                                            .collect(Collectors.toSet());

                                    for (String c : corpusNames) {
                                        if (!knownCorpora.contains(c)) {
                                            q.addUnknownCorpus(c);
                                        }
                                    }

                                    if (!q.getUnknownCorpora().isEmpty()) {
                                        failedQueries.put(QueryStatus.UnknownCorpus, q);
                                    } else if (corpusNames.isEmpty()) {
                                        q.setErrorMsg("Corpus name is empty");
                                        failedQueries.put(QueryStatus.Failed, q);
                                    } else if (getShortenerDao().unshorten(q.getUuid()) != null) {
                                        if (skipExisting) {
                                            continue;
                                        } else {
                                            failedQueries.put(QueryStatus.UUIDExists, q);
                                        }
                                    } else {
                                        // check the query
                                        try {
                                            log.info("UUID {}, testing query {} on corpus {}", q.getUuid(),
                                                    q.getQuery().getQuery().trim(), q.getQuery().getCorpora());
                                            QueryStatus status = q.test(getAdministrationDao().getQueryDao(),
                                                    searchService);

                                            // insert URLs into new database
                                            String temporary = null;

                                            if (status != QueryStatus.Ok) {
                                                failedQueries.put(status, q);
                                                // Link the UUID to an error page temporarily, until the issue is
                                                // fixed.
                                                // Remember the original URL, so the temporary URL can just be set
                                                // to null to
                                                // resolve
                                                // to the original URL when the issue is fixed in ANNIS.
                                                try {
                                                    URI tempURI = new URIBuilder().setPath("/unsupported-query")
                                                            .addParameter("url", q.getUri().toASCIIString()).build();
                                                    temporary = tempURI.toASCIIString();
                                                } catch (URISyntaxException e) {
                                                    log.error("Could not create proper URI for unsupported query", e);
                                                }

                                                String lineSeparator = System.getProperty("line.separator");

                                                StringBuilder sb = new StringBuilder();
                                                sb.append("Query Error: " + status + lineSeparator);
                                                sb.append("Corpus: \"" + q.getQuery().getCorpora() + "\""
                                                        + lineSeparator);
                                                sb.append("UUID: \"" + q.getUuid() + "\"" + lineSeparator);
                                                sb.append("Query:" + lineSeparator);
                                                sb.append(q.getQuery().getQuery().trim() + lineSeparator);
                                                sb.append("Error Message: " + q.getErrorMsg());

                                                log.warn(sb.toString());
                                            }
                                            getShortenerDao().migrate(q.getUri().toASCIIString(), temporary,
                                                    "anonymous", q.getUuid(), q.getCreationTime() == null ? new Date()
                                                            : q.getCreationTime().toDate());

                                            if (status == QueryStatus.Ok) {
                                                successfulQueries++;
                                            }

                                        } catch (GraphANNISException ex) {
                                            q.setErrorMsg(ex.getMessage());
                                            failedQueries.put(QueryStatus.Failed, q);
                                        } catch (Throwable ex) {
                                            String lineSeparator = System.getProperty("line.separator");
                                            StringBuilder sb = new StringBuilder();
                                            sb.append("Query Error: " + QueryStatus.Failed + lineSeparator);
                                            sb.append("Corpus: \"" + q.getQuery().getCorpora() + "\"" + lineSeparator);
                                            sb.append("UUID: \"" + q.getUuid() + "\"" + lineSeparator);
                                            sb.append("Query:" + lineSeparator);
                                            sb.append(q.getQuery().getQuery().trim() + lineSeparator);
                                            sb.append("Error Message: " + ex.getMessage());

                                            q.setErrorMsg(ex.getMessage());

                                            log.warn(sb.toString(), ex);
                                            failedQueries.put(QueryStatus.Failed, q);
                                        }
                                    }
                                }
                            } catch (Throwable ex) {

                                String lineSeparator = System.getProperty("line.separator");

                                String errorMsg = ex.getMessage();
                                if (errorMsg == null) {
                                    errorMsg = ex.getClass().toString();
                                    ex.printStackTrace();

                                }

                                StringBuilder sb = new StringBuilder();
                                sb.append("Query Error: " + QueryStatus.Failed + lineSeparator);
                                sb.append("UUID: \"" + line[0] + "\"" + lineSeparator);
                                sb.append("Error Message: " + errorMsg);

                                URLShortenerDefinition q = new URLShortenerDefinition(null,
                                        URLShortenerDefinition.parseUUID(line[0]), null);
                                q.setErrorMsg(errorMsg);
                                failedQueries.put(QueryStatus.Failed, q);

                                log.warn(sb.toString());
                            }
                        }
                    }

                } catch (FileNotFoundException ex) {
                    log.error("File with URL shortener table not found", ex);
                } catch (IOException ex) {
                    log.error("Migrating URL shortener table failed", ex);
                }
            }
        }

        return successfulQueries;
    }

    /**
     * Extract the zipped ANNIS corpus files to an output directory.
     *
     * @param outDir
     *                   The ouput directory.
     * @param zip
     *                   ZIP-file to extract.
     * @return A list of root directories where the tab-files are located if found,
     *         null otherwise.
     */
    private List<File> unzipCorpus(File outDir, ZipFile zip) {
        List<File> rootDirs = new ArrayList<>();

        Enumeration<? extends ZipEntry> zipEnum = zip.entries();
        while (zipEnum.hasMoreElements()) {
            ZipEntry e = zipEnum.nextElement();
            File outFile = new File(outDir, e.getName().replaceAll("\\/", "/"));

            if (e.isDirectory()) {
                if (!outFile.mkdirs()) {
                    log.warn("Could not create output directory " + outFile.getAbsolutePath());
                }
            } // end if directory
            else {
                if ("corpus.tab".equals(outFile.getName()) || "corpus.annis".equals(outFile.getName())) {
                    rootDirs.add(outFile.getParentFile());
                }

                if (!outFile.getParentFile().isDirectory()) {
                    if (!outFile.getParentFile().mkdirs()) {
                        {
                            log.warn("Could not create output directory for file " + outFile.getAbsolutePath());
                        }
                    }
                }
                try (FileOutputStream outStream = new FileOutputStream(outFile);) {

                    ByteStreams.copy(zip.getInputStream(e), outStream);
                } catch (FileNotFoundException ex) {
                    log.error(null, ex);
                } catch (IOException ex) {
                    log.error(null, ex);
                }
            } // end else is file
        } // end for each entry in zip file

        return rootDirs;
    }

    public File createZIPOutputDir(String corpusName) {
        File outDir = new File(System.getProperty("user.home"),
                ".annis/zip-imports/" + CommonHelper.getSafeFileName(corpusName));
        if (outDir.exists()) {
            try {
                // delete old data inside the corpus directory
                FileUtils.deleteDirectory(outDir);
            } catch (IOException ex) {
                log.warn("Could not recursivly delete the output directory", ex);
            }
        }
        if (!outDir.mkdirs()) {
            throw new IllegalStateException("Could not create directory " + outDir.getAbsolutePath());
        }
        return outDir;
    }

    public static class ImportStatsImpl implements ImportStatus {

        boolean status = true;

        private final static String SEPERATOR = "--------------------------\n";

        final Map<String, List<Throwable>> exceptions;

        public ImportStatsImpl() {
            exceptions = new HashMap<>();
        }

        @Override
        public boolean getStatus() {
            return status;
        }

        @Override
        public List<Throwable> getThrowables() {
            List<Throwable> allThrowables = new ArrayList<>();

            for (List<Throwable> l : exceptions.values()) {
                allThrowables.addAll(l);
            }

            return allThrowables;
        }

        @Override
        public List<Throwable> getThrowable(String corpusName) {
            return exceptions.get(corpusName);
        }

        @Override
        public void addException(String corpusName, Throwable ex) {
            if (!exceptions.containsKey(corpusName)) {
                exceptions.put(corpusName, new ArrayList<Throwable>());
            }

            exceptions.get(corpusName).add(ex);
        }

        @Override
        public void setStatus(boolean status) {
            this.status = status;
        }

        @Override
        public void add(ImportStatus importStats) {
            if (importStats == null) {
                return;
            }

            status &= importStats.getStatus();
            exceptions.putAll(importStats.getAllThrowable());
        }

        @Override
        public List<Exception> getExceptions() {
            List<Exception> exs = new ArrayList<>();

            if (exceptions != null) {
                for (List<Throwable> throwables : exceptions.values()) {
                    for (Throwable throwable : throwables) {
                        if (throwable instanceof Exception) {
                            exs.add((Exception) throwable);
                        }
                    }
                }
            }

            return exs;
        }

        @Override
        public Map<String, List<Throwable>> getAllThrowable() {
            return this.exceptions;
        }

        @Override
        public String printMessages() {
            StringBuilder txtMessages = new StringBuilder();
            for (Entry<String, List<Throwable>> e : exceptions.entrySet()) {
                txtMessages.append(SEPERATOR);
                txtMessages.append("Error in corpus: ").append(e.getKey()).append("\n");
                txtMessages.append(SEPERATOR);

                for (Throwable th : e.getValue()) {
                    Exception exception = (Exception) th;
                    txtMessages.append(exception.getLocalizedMessage()).append("\n");
                }
            }

            return txtMessages.toString();
        }

        @Override
        public String printDetails() {
            StringBuilder details = new StringBuilder();
            for (Entry<String, List<Throwable>> e : exceptions.entrySet()) {
                details.append(SEPERATOR);
                details.append("Error in corpus: ").append(e.getKey()).append("\n");
                details.append(SEPERATOR);

                for (Throwable th : e.getValue()) {
                    details.append(th.getLocalizedMessage()).append("\n");
                    StackTraceElement[] st = th.getStackTrace();

                    for (int i = 0; i < st.length; i++) {
                        details.append(st[i].toString());
                        details.append("\n");
                    }
                }
            }

            return details.toString();
        }

        @Override
        public String printType() {
            StringBuilder type = new StringBuilder();

            for (Entry<String, List<Throwable>> e : exceptions.entrySet()) {
                String name = e.getKey().split("/")[e.getKey().split("/").length - 1];
                type.append("(").append(name).append(": ");

                for (Throwable th : e.getValue()) {
                    type.append(th.getClass().getSimpleName()).append(" ");
                }

                type.append(") ");
            }

            return type.toString();
        }
    }

    public void sendCopyStatusMail(String adress, String origDBFile, ImportJob.Status status, String additionalInfo) {
        if (adress == null || origDBFile == null) {
            return;
        }

        // check valid properties
        if (cfg.mailSender() == null || cfg.mailSender().isEmpty()) {
            log.warn("Could not send status mail because \"annis.mail-sender\" "
                    + "property was not configured in conf/annis-service-properties.");
            return;
        }

        try {
            SimpleEmail mail = new SimpleEmail();
            List<InternetAddress> to = new LinkedList<>();
            to.add(new InternetAddress(adress));

            StringBuilder sbMsg = new StringBuilder();
            sbMsg.append("Dear Sir or Madam,\n");
            sbMsg.append("\n");
            sbMsg.append("this is the requested status update to the ANNIS corpus import "
                    + "you have started. Please note that this message is automated and "
                    + "if you have any question regarding the import you have to ask the "
                    + "administrator of the ANNIS instance directly.\n\n");

            mail.setTo(to);
            if (status == ImportJob.Status.SUCCESS) {
                mail.setSubject("ANNIS copy finished successfully (" + origDBFile + ")");
                sbMsg.append("Status:\nThe corpora from \"").append(origDBFile)
                        .append("\" were successfully imported and can be used from now on.\n");
            } else if (status == ImportJob.Status.ERROR) {
                mail.setSubject("ANNIS copy *failed* (" + origDBFile + ")");
                sbMsg.append("Status:\nUnfortunally the corpora from \"").append(origDBFile)
                        .append("\" could not be imported successfully. "
                                + "You may ask the administrator of the ANNIS installation for "
                                + "assistance why the corpus import failed.\n");
            } else if (status == ImportJob.Status.RUNNING) {
                mail.setSubject("ANNIS copy started (" + origDBFile + ")");
                sbMsg.append("Status:\nThe import of the corpora from \"").append(origDBFile)
                        .append("\" was started.\n");
            } else if (status == ImportJob.Status.WAITING) {
                mail.setSubject("ANNIS copy was scheduled (" + origDBFile + ")");
                sbMsg.append("Status:\nThe import of the corpora from \"").append(origDBFile)
                        .append("\" was scheduled and is currently waiting for other imports to "
                                + "finish. As soon as the previous imports are finished this copy "
                                + "job will be executed.\n");
            } else {
                // we don't know how to handle this, just don't send a message
                return;
            }
            if (additionalInfo != null && !additionalInfo.isEmpty()) {
                sbMsg.append("Addtional information:\n");
                sbMsg.append(additionalInfo).append("\n");
            }

            sbMsg.append("\n\nSincerely yours,\n\nthe ANNIS import service.");
            mail.setMsg(sbMsg.toString());
            mail.setHostName("localhost");
            mail.setFrom(cfg.mailSender());

            mail.send();
            log.info("Send status ({}) mail to {}.", new String[] { status.name(), adress });

        } catch (AddressException | EmailException ex) {
            log.warn("Could not send mail: " + ex.getMessage());
        }
    }

    public boolean checkDatabaseSchemaVersion() {
        try {
            administrationDao.checkDatabaseSchemaVersion();
        } catch (AnnisException ex) {
            return false;
        }
        return true;
    }

    /**
     * Imports several corpora.
     *
     * @param overwrite
     *                              if false, a conflicting top level corpus is
     *                              silently skipped.
     * @param aliasName
     *                              An common alias name for all imported corpora or
     *                              null
     * @param statusEmailAdress
     *                              If not null the email adress of the user who
     *                              started the import.
     * @param waitForOtherTasks
     *                              If true wait for other imports to finish, if
     *                              false abort the import.
     * @param paths
     *                              the paths to the corpora
     * @return True if all corpora where imported successfully.
     */
    public ImportStatus importCorporaSave(boolean overwrite, String aliasName, String statusEmailAdress,
            boolean waitForOtherTasks, String... paths) {
        return importCorporaSave(overwrite, aliasName, statusEmailAdress, waitForOtherTasks, Arrays.asList(paths));
    }

    public void dumpTable(String tableName, File outputFile) {
        Table table = null;
        switch (tableName) {
        case "url_shortener":
            table = AdministrationDao.urlShortenerTable;
            break;
        case "user_config":
            table = AdministrationDao.userConfigTable;
            break;
        }
        if (table == null) {
            log.info("Can't dump unknown table with name {}", tableName);
            return;
        } else {
            log.info("Dumping table {} to file {}", tableName, outputFile);
            administrationDao.dumpServiceDataTable(table, outputFile);
        }
    }

    public void restoreTable(String tableName, File inputFile) {
        Table table = null;
        switch (tableName) {
        case "url_shortener":
            table = AdministrationDao.urlShortenerTable;
            break;
        case "user_config":
            table = AdministrationDao.userConfigTable;
            break;
        }
        if (table == null) {
            log.info("Can't restore unknown table with name {}", tableName);
            return;
        } else {
            log.info("Restoring table {} from file {}", tableName, inputFile);
            administrationDao.restoreServiceDataTable(table, inputFile);
        }
    }

    ///// Helper
    protected void writeDatabasePropertiesFile(String host, String port, String database, String user, String password,
            boolean useSSL, String schema) {
        File file = new File(System.getProperty("annis.home") + "/conf", "database.properties");
        try (BufferedWriter writer = new BufferedWriter(new FileWriterWithEncoding(file, "UTF-8"));) {

            writer.write("# database configuration\n");
            writer.write("datasource.driver=org.postgresql.Driver\n");
            writer.write("datasource.url=jdbc:postgresql://" + host + ":" + port + "/" + database + "\n");
            writer.write("datasource.username=" + user + "\n");
            writer.write("datasource.password=" + password + "\n");
            writer.write("datasource.ssl=" + (useSSL ? "true" : "false") + "\n");
            if (schema != null) {
                writer.write("datasource.schema=" + schema + "\n");
            }
        } catch (IOException e) {
            log.error("Couldn't write database properties file", e);
            throw new FileAccessException(e);
        }
        log.info("Wrote database configuration to " + file.getAbsolutePath());
    }

    ///// Getter / Setter
    public AdministrationDao getAdministrationDao() {
        return administrationDao;
    }

    public void setAdministrationDao(AdministrationDao administrationDao) {
        this.administrationDao = administrationDao;
    }

    public DeleteCorpusDao getDeleteCorpusDao() {
        return administrationDao.getDeleteCorpusDao();
    }

    public ShortenerDao getShortenerDao() {
        return shortenerDao;
    }

    public void setShortenerDao(ShortenerDao shortenerDao) {
        this.shortenerDao = shortenerDao;
    }

}
