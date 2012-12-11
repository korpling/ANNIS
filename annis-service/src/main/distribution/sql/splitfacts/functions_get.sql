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
