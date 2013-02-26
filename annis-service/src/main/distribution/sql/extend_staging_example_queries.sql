-- Adds the already computed corpus top level id to the table.
ALTER TABLE _example_queries ADD corpus_ref integer;
UPDATE _example_queries SET corpus_ref = :id;