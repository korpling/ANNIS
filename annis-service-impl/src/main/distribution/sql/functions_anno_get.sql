-- node

CREATE OR REPLACE FUNCTION getNodeAnnoByName("name" varchar(150), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM node_anno 
  WHERE "name" = $1
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getNodeAnnoByNamespaceName(namespace varchar(150), "name" varchar(150), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM node_anno 
  WHERE "namespace" = $1 AND "name" = $2 
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getNodeAnnoByNameVal("name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM node_anno 
  WHERE "name" = $1 AND val = $2
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getNodeAnnoByNamespaceNameVal(namespace varchar(150), "name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM node_anno 
  WHERE namespace=$1 AND "name" = $2 AND val = $3
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getNodeAnnoByNameValRegex("name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM node_anno 
  WHERE "name" = $1 AND val ~ $2
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getNodeAnnoByNamespaceNameRegex(namespace varchar(150), "name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM node_anno 
  WHERE namespace=$1 AND "name" = $2 AND val ~ $3
);
$f$ LANGUAGE SQL IMMUTABLE;

-- edge

CREATE OR REPLACE FUNCTION getEdgeAnnoByName("name" varchar(150), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM edge_anno
  WHERE "name" = $1
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getEdgeAnnoByNamespaceName(namespace varchar(150), "name" varchar(150), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM edge_anno
  WHERE "namespace" = $1 AND "name" = $2 
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getEdgeAnnoByNameVal("name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM edge_anno
  WHERE "name" = $1 AND val = $2
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getEdgeAnnoByNamespaceNameVal(namespace varchar(150), "name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM edge_anno
  WHERE namespace=$1 AND "name" = $2 AND val = $3
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getEdgeAnnoByNameValRegex("name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM edge_anno
  WHERE "name" = $1 AND val ~ $2
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getEdgeAnnoByNamespaceNameRegex(namespace varchar(150), "name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM edge_anno
  WHERE namespace=$1 AND "name" = $2 AND val ~ $3
);
$f$ LANGUAGE SQL IMMUTABLE;