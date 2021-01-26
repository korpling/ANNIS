package org.corpus_tools.annis.gui.admin.reflinks;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.FutureCallback;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.corpus_tools.annis.gui.Helper;
import org.corpus_tools.annis.gui.components.ExceptionDialog;

final class MigrationCallback implements FutureCallback<Integer> {

  private final MigrationPanel migrationPanel;
  private final Multimap<QueryStatus, URLShortenerDefinition> failedQueries;

  MigrationCallback(MigrationPanel migrationPanel,
      Multimap<QueryStatus, URLShortenerDefinition> failedQueries) {
    this.migrationPanel = migrationPanel;
    this.failedQueries = failedQueries;
  }

  @Override
  public void onSuccess(Integer successfulQueries) {
    this.migrationPanel.progress.setCaption("");

    // output summary and detailed list of failed queries
    final Collection<URLShortenerDefinition> unknownCorpusQueries =
        failedQueries.get(QueryStatus.UNKNOWN_CORPUS);

    StringBuilder detailedStatus = new StringBuilder();

    if (!unknownCorpusQueries.isEmpty()) {
      final Map<String, Integer> unknownCorpusCount = new TreeMap<>();
      for (final URLShortenerDefinition q : unknownCorpusQueries) {
        for (final String c : q.getUnknownCorpora()) {
          final int oldCount = unknownCorpusCount.getOrDefault(c, 0);
          unknownCorpusCount.put(c, oldCount + 1);
        }
      }
      final String unknownCorpusCaption = "Unknown corpus (" + unknownCorpusCount.size()
          + " unknown corpora and " + unknownCorpusQueries.size() + " queries)";
      detailedStatus.append(unknownCorpusCaption);
      detailedStatus.append("\n");
      detailedStatus.append(Strings.repeat("=", unknownCorpusCaption.length()));
      detailedStatus.append("\n");

      for (final Map.Entry<String, Integer> e : unknownCorpusCount.entrySet()) {
        detailedStatus.append("Corpus \"" + e.getKey() + "\": " + e.getValue() + " queries");
        detailedStatus.append("\n");
      }
      detailedStatus.append("\n");
    }

    printProblematicQueries("UUID already exists", failedQueries.get(QueryStatus.UUID_EXISTS),
        detailedStatus);
    printProblematicQueries("Count different", failedQueries.get(QueryStatus.COUNT_DIFFERS),
        detailedStatus);
    printProblematicQueries("Match list different", failedQueries.get(QueryStatus.MATCHES_DIFFER),
        detailedStatus);
    printProblematicQueries("Timeout", failedQueries.get(QueryStatus.TIMEOUT), detailedStatus);
    printProblematicQueries("Other server error", failedQueries.get(QueryStatus.SERVER_ERROR),
        detailedStatus);
    printProblematicQueries("Empty corpus list", failedQueries.get(QueryStatus.EMPTY_CORPUS_LIST),
        detailedStatus);
    printProblematicQueries("FAILED", failedQueries.get(QueryStatus.FAILED), detailedStatus);

    final String summaryString = "+ Successful: " + successfulQueries + " from "
        + (successfulQueries + failedQueries.size()) + " +";
    detailedStatus.append(Strings.repeat("+", summaryString.length()));
    detailedStatus.append("\n");
    detailedStatus.append(summaryString);
    detailedStatus.append("\n");
    detailedStatus.append(Strings.repeat("+", summaryString.length()));
    detailedStatus.append("\n");

    this.migrationPanel.setMessageAndScrollToEnd(detailedStatus.toString());
    this.migrationPanel.migrateButton.setEnabled(true);
    
    sendMail();
  }

  private void sendMail() {
    String mailAddress = this.migrationPanel.emailText.getValue();
    if (mailAddress != null && !mailAddress.isEmpty()) {
      try {
        // Send the whole content of the message text area as mail
        MultiPartEmail mail = Helper.createEMailFromConfiguration(migrationPanel.ui.getConfig());
        mail.addTo(mailAddress);
        mail.setSubject("ANNIS reference link migration finished");
        String message = this.migrationPanel.getMessages();
        if (message == null || message.isEmpty()) {
          message = "No further information provided";
        }
        mail.setMsg(message);
        mail.send();
      } catch (EmailException | UnknownHostException ex) {
        ExceptionDialog.show(ex, this.migrationPanel.ui);
      }
    }
  }

  @Override
  public void onFailure(Throwable t) {
    ExceptionDialog.show(t, this.migrationPanel.ui);
    this.migrationPanel.migrateButton.setEnabled(true);

    sendMail();
  }

  private void printProblematicQueries(final String statusCaption,
      final Collection<URLShortenerDefinition> queries, StringBuilder sb) {
    if (queries != null && !queries.isEmpty()) {
      final String captionWithCount = statusCaption + " (sum: " + queries.size() + ")";
      sb.append(captionWithCount);
      sb.append("\n");
      sb.append(Strings.repeat("=", captionWithCount.length()));
      sb.append("\n\n");

      for (final URLShortenerDefinition q : queries) {
        if (q.getQuery() != null && q.getQuery().getCorpora() != null) {
          sb.append(MigrationPanel.CORPUS_PREFIX + q.getQuery().getCorpora() + "\"");
          sb.append("\n");
        }
        sb.append(MigrationPanel.UUID_PREFIX + q.getUuid() + "\"");
        sb.append("\n");
        if (q.getQuery() != null && q.getQuery().getQuery() != null) {
          sb.append(MigrationPanel.QUERY_PREFIX);
          sb.append("\n");
          sb.append(q.getQuery().getQuery().trim());
          sb.append("\n");
        }
        if (q.getErrorMsg() != null) {
          sb.append("Error: " + q.getErrorMsg());
          sb.append("\n");
        }

        sb.append("-------");
        sb.append("\n");
      }
      sb.append("\n");
    }
  }

}