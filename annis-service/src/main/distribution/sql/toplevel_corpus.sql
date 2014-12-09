-- find the top-level corpus
ALTER TABLE _corpus ADD top_level boolean;
CREATE TRIGGER unique_toplevel_corpus_name BEFORE UPDATE ON _corpus FOR EACH ROW EXECUTE PROCEDURE unique_toplevel_corpus_name();
UPDATE _corpus SET top_level = 'n';
UPDATE _corpus SET top_level = 'y' WHERE pre = (SELECT min(pre) FROM _corpus);