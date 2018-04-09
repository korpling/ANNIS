/*
 * Copyright 2013 SFB 632.
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
package annis.sqlgen;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.handlers.AbstractListHandler;

import annis.examplequeries.ExampleQuery;

/**
 * Generates SQL query for example queries
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class ListExampleQueriesHelper extends AbstractListHandler<ExampleQuery> {

    public final static String SQL = "SELECT example_query, description, corpus\n"
            + "FROM example_queries WHERE corpus=?";

    @Override
    protected ExampleQuery handleRow(ResultSet rs) throws SQLException {
        ExampleQuery exampleQuery = new ExampleQuery();

        exampleQuery.setExampleQuery(rs.getString("example_query"));
        exampleQuery.setDescription(rs.getString("description"));
        exampleQuery.setCorpusName(rs.getString("corpus"));

        return exampleQuery;
    }
}
