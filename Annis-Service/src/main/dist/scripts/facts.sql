CREATE TABLE facts AS SELECT DISTINCT
	node.id AS id,
	node.text_ref AS text_ref,
	node.corpus_ref AS corpus_ref,
	node.toplevel_corpus AS toplevel_corpus,
	node.namespace AS node_namespace,
	node.name AS node_name,
	node."left" AS "left",
	node."right" AS "right",
	node.token_index AS token_index,
	node.continuous AS continuous,
	node.span AS span,
	node.left_token AS left_token,
	node.right_token AS right_token,

	rank.pre AS pre,
	rank.post AS post,
	rank.parent AS parent,
	rank.root AS root,
	rank.level AS level,

	component.id AS component_id,
	component.type AS edge_type,
	component.namespace AS edge_namespace,
	component.name AS edge_name,
	
	node_annotation.namespace AS node_annotation_namespace,
	node_annotation.name AS node_annotation_name,
	node_annotation.value AS node_annotation_value,

	edge_annotation.namespace AS edge_annotation_namespace,
	edge_annotation.name AS edge_annotation_name,
	edge_annotation.value AS edge_annotation_value
FROM
	node 
	JOIN rank ON (rank.node_ref = node.id) 
	JOIN component ON (rank.component_ref = component.id)
	LEFT JOIN node_annotation ON (node_annotation.node_ref = node.id)
	LEFT JOIN edge_annotation ON (edge_annotation.rank_ref = rank.pre);

-- can't be run inside transaction
-- VACUUM ANALYZE facts;