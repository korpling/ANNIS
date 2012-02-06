CREATE OR REPLACE FUNCTION getNodeAnno("name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM s_node_anno 
  WHERE "name" = $1 AND val = $2
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getNodeAnno(namespace varchar(150), "name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM s_node_anno 
  WHERE namespace=$1 AND "name" = $2 AND val = $3
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getNodeAnnoRegex("name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM s_node_anno 
  WHERE "name" = $1 AND val ~ $2
);
$f$ LANGUAGE SQL IMMUTABLE;

CREATE OR REPLACE FUNCTION getNodeAnnoRegex(namespace varchar(150), "name" varchar(150), val varchar(1500), toplevel_corpus bigint[]) 
RETURNS bigint[] AS $f$
SELECT ARRAY(
  SELECT id FROM s_node_anno 
  WHERE namespace=$1 AND "name" = $2 AND val ~ $3
);
$f$ LANGUAGE SQL IMMUTABLE;