-- find the top-level corpus
ALTER TABLE _corpus ADD top_level boolean;
UPDATE _corpus SET top_level = 'n';
UPDATE _corpus SET top_level = 'y' WHERE pre = (SELECT min(pre) FROM _corpus);
