INSERT INTO annotation_category (toplevel_corpus, namespace, name)
(
  SELECT DISTINCT :id, namespace, name
  FROM annotations_:id
  WHERE name IS NOT NULL  AND type = 'node'
);