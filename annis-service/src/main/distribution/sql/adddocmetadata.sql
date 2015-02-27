DELETE FROM _corpus_annotation WHERE namespace='annis' AND name='doc';
INSERT INTO _corpus_annotation (corpus_ref, namespace, "name", "value")
(
SELECT c.id, 'annis', 'doc', c."name"
FROM _corpus AS c
WHERE c."type" = 'DOCUMENT'
);