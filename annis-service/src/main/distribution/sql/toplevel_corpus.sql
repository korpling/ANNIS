-- find the top-level corpus
ALTER TABLE _corpus ADD top_level boolean;
CREATE TRIGGER unique_toplevel_corpus_name BEFORE UPDATE ON _corpus FOR EACH ROW EXECUTE PROCEDURE unique_toplevel_corpus_name();
UPDATE _corpus SET top_level = 'n';
UPDATE _corpus SET top_level = 'y' WHERE pre = (SELECT min(pre) FROM _corpus);

-- add the toplevel corpus to the node table
CREATE INDEX tmp_corpus_toplevel ON _corpus (id) WHERE top_level = 'y';
ALTER TABLE _node ADD toplevel_corpus bigint;
UPDATE _node SET toplevel_corpus = _corpus.id FROM _corpus WHERE _corpus.top_level = 'y';
DROP INDEX tmp_corpus_toplevel;