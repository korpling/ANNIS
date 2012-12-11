ALTER TABLE _text ADD toplevel_corpus integer;
UPDATE _text SET toplevel_corpus = :id;