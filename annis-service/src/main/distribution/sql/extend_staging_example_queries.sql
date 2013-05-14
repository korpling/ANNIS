ALTER TABLE _example_queries ADD type TEXT;
UPDATE _example_queries SET type = 'tok';

ALTER TABLE _example_queries ADD nodes INTEGER;

ALTER TABLE _example_queries ADD used_ops TEXT[];
UPDATE _example_queries SET used_ops = '{., >}';

-- Adds the already computed corpus top level id to the table.
ALTER TABLE _example_queries ADD corpus_ref INTEGER;
UPDATE _example_queries SET corpus_ref = :id;