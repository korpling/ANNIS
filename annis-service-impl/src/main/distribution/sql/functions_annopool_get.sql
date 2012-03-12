CREATE OR REPLACE FUNCTION getAnnoByName("name" varchar(150), toplevel_corpus bigint[], "type" varchar) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM annotation_pool 
  WHERE "name" = $1 AND toplevel_corpus = ANY($2) AND "type" = $3::annotype::annotype
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getAnnoByNamespaceName(namespace varchar(150), "name" varchar(150), toplevel_corpus bigint[], "type" varchar) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM annotation_pool 
  WHERE "namespace" = $1 AND "name" = $2 AND toplevel_corpus = ANY($3) AND "type" = $4::annotype 
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getAnnoByNameVal("name" varchar(150), val varchar(1500), toplevel_corpus bigint[], "type" varchar) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM annotation_pool 
  WHERE "name" = $1 AND val = $2 AND toplevel_corpus = ANY($3) AND "type" = $4::annotype 
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getAnnoByNamespaceNameVal(namespace varchar(150), "name" varchar(150), val varchar(1500), toplevel_corpus bigint[], "type" varchar) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM annotation_pool 
  WHERE namespace=$1 AND "name" = $2 AND val = $3 AND toplevel_corpus = ANY($4) AND "type" = $5::annotype
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getAnnoByNameValRegex("name" varchar(150), val varchar(1500), toplevel_corpus bigint[], "type" varchar) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM annotation_pool 
  WHERE "name" = $1 AND val ~ $2 AND toplevel_corpus = ANY($3) AND "type" = $4::annotype
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getAnnoByNamespaceNameValRegex(namespace varchar(150), "name" varchar(150), val varchar(1500), toplevel_corpus bigint[] ,"type" varchar) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM annotation_pool 
  WHERE namespace=$1 AND "name" = $2 AND val ~ $3 AND toplevel_corpus = ANY($4) AND "type" = $5::annotype
);
$f$ LANGUAGE SQL IMMUTABLE;
