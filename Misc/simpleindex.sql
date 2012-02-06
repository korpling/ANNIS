CREATE OR REPLACE FUNCTION simplefactsindex(varchar) RETURNS void 
AS $$
DECLARE 
  stdcols varchar[] = ARRAY[
    'id', 'left', 'right', 'token_index',
    'is_token', 'continuous', 'left_token',
    'right_token', 'pre', 'post', 'parent',
    'root', 'level', 'component_id', 'edge_type',
    'node_anno'
	
  ];
	strcols varchar[] = ARRAY[
    'span', 
    'edge_annotation_namespace', 
    'edge_annotation_name', 'edge_annotation_value',
    'node_namespace', 'node_name',
    'edge_namespace', 'edge_name'

  ];
	colname varchar;
	indexname varchar;
BEGIN

  FOREACH colname IN ARRAY stdcols
  LOOP
    indexname := 'idx_' || $1 
    || '_' || colname;
    EXECUTE 'DROP INDEX IF EXISTS  "' || indexname 
      || '"';
		EXECUTE 'CREATE INDEX  "' || indexname 
      || '" ON '
      || $1 || ' ("' || colname || '", corpus_ref);';
  END LOOP;

  FOREACH colname IN ARRAY strcols
  LOOP
    indexname := 'idx_' || $1 
      || '_' || colname;
    EXECUTE 'DROP INDEX IF EXISTS  "' || indexname 
      || '"';
    EXECUTE 'CREATE INDEX  "' || indexname 
      || '" ON '
      || $1 || ' ("' || colname || '" varchar_pattern_ops, corpus_ref);';
	END LOOP;

  RETURN;

END
$$ LANGUAGE plpgsql;
