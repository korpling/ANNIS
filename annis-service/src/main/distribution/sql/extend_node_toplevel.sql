-- add the toplevel corpus to the node table
CREATE INDEX tmp_corpus_toplevel ON _corpus (id) WHERE top_level = 'y';
ALTER TABLE _node ADD toplevel_corpus integer;
UPDATE _node SET toplevel_corpus = _corpus.id FROM _corpus WHERE _corpus.top_level = 'y';
DROP INDEX tmp_corpus_toplevel;