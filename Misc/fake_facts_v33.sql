SELECT 
        -- node
	node.id AS node_id,
	node.corpus_ref AS node_corpus_ref,
	node.text_ref AS node_text_ref,
	node.name AS node_name,
	node.name AS node_salt_id,
	node.layer AS node_namespace,
	node.token_index AS node_token_index,
	NULL AS node_seg_index,
	NULL AS node_seg_name,
	node.span AS node_span,
	-- node_annotation
	node_annotation.namespace AS node_annotation_namespace,
	node_annotation.name AS node_annotation_name,
	node_annotation.value AS node_annotation_value,
	-- component
	component.id AS component_id,
	component.layer AS component_layer,
	component.name AS component_name,
	component.type AS component_type,
	-- rank
	rank.pre AS rank_id,
	rank.pre AS rank_pre,
	rank.parent AS rank_parent,
	-- edge_annotation
	edge_annotation.namespace AS edge_annotation_namespace,
	edge_annotation.name AS edge_annotation_name,
	edge_annotation.value AS edge_annotation_value
FROM
node, node_annotation, component, rank, edge_annotation
WHERE
	node.id = node_annotation.node_ref
	AND node.id = rank.node_ref
	AND component.id = rank.component_ref
	AND edge_annotation.rank_ref = rank.pre
	AND node.corpus_ref = ?