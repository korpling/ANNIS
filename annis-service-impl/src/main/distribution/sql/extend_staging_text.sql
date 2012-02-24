ALTER TABLE _text ADD toplevel_corpus BIGINT;
UPDATE _text SET toplevel_corpus = :id;