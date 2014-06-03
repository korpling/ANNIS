CREATE OR REPLACE FUNCTION getAnno(namespace varchar, "name" varchar, val varchar, valRegex varchar, toplevel_corpus integer[], "type" varchar) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id 
  FROM annotation_pool 
  WHERE 
    ($1 IS NULL OR namespace=$1) AND 
    ($2 IS NULL OR "name" = $2) AND 
    ($3 IS NULL OR val = $3) AND
    ($4 IS NULL OR val ~ $4) AND
    toplevel_corpus = ANY($5) AND 
    "type" = $6::annotype
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getAnnoNot(namespace varchar, "name" varchar, val varchar, valRegex varchar, toplevel_corpus integer[], "type" varchar) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id 
  FROM annotation_pool 
  WHERE 
    ($1 IS NULL OR namespace=$1) AND 
    ($2 IS NULL OR "name" = $2) AND 
    ($3 IS NULL OR val <> $3) AND
    ($4 IS NULL OR val !~ $4) AND
    toplevel_corpus = ANY($5) AND 
    "type" = $6::annotype
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getAnnoValue(anno_ref bigint, toplevel_corpus bigint, "type" varchar) 
RETURNS varchar AS $f$
SELECT val
FROM annotation_pool
WHERE
  id = $1 AND 
  toplevel_corpus = $2 AND
  "type" = $3::annotype
$f$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION getAnnoNamespace(anno_ref bigint, toplevel_corpus bigint, "type" varchar) 
RETURNS varchar AS $f$
SELECT namespace
FROM annotation_pool
WHERE
  id = $1 AND 
  toplevel_corpus = $2 AND
  "type" = $3::annotype
$f$ LANGUAGE SQL STABLE;

CREATE OR REPLACE FUNCTION getAnnoName(anno_ref bigint, toplevel_corpus bigint, "type" varchar) 
RETURNS varchar AS $f$
SELECT "name"
FROM annotation_pool
WHERE
  id = $1 AND 
  toplevel_corpus = $2 AND
  "type" = $3::annotype
$f$ LANGUAGE SQL STABLE;