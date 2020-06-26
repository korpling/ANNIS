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
package annis.sqlgen;

import annis.service.objects.AnnisCorpus;
import com.google.common.base.Joiner;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.handlers.AbstractListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListCorpusSqlHelper extends AbstractListHandler<AnnisCorpus> {

    private static final Logger log = LoggerFactory.getLogger(ListCorpusSqlHelper.class);

    public String createSqlQuery() {
        return "SELECT * FROM corpus_info";
    }

    public String createSqlQueryWithList(int numberOfCorpora) {
        List<String> questionMarks = new ArrayList<>();
        for (int i = 0; i < numberOfCorpora; i++) {
            questionMarks.add("?");
        }
        return "SELECT * FROM corpus_info WHERE name IN ("
                + (questionMarks.isEmpty() ? "NULL" : Joiner.on(",").join(questionMarks)) + ")";
    }

    @Override
    protected AnnisCorpus handleRow(ResultSet rs) throws SQLException {
        AnnisCorpus corpus = new AnnisCorpus();
        corpus.setName(rs.getString("name"));
        corpus.setDocumentCount(rs.getInt("docs"));
        corpus.setTokenCount(rs.getInt("tokens"));
        corpus.setSourcePath(rs.getString("source_path"));

        return corpus;
    }
}