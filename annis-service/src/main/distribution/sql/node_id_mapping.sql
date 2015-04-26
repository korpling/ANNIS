DROP TABLE IF EXISTS _nodeidmapping;
CREATE UNLOGGED TABLE _nodeidmapping 
(
  "old" bigint, "new" bigint,
  PRIMARY KEY ("old"),
  UNIQUE("new")
);

INSERT INTO _nodeidmapping("old", "new")
SELECT id AS "old", row_number() OVER () AS "new"
FROM
(
  SELECT n.id AS id
  FROM _node AS n, _corpus AS c, _text as t
  WHERE
    n.corpus_ref = c.id AND n.text_ref = t.id  AND c.id = t.corpus_ref
  ORDER BY c.name, t.name, n.left_token, n."name"
) as ordered;