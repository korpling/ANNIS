-- This will set the "continous" property of the spans correctly

UPDATE _node AS parent_node SET continuous = false
WHERE
parent_node.continuous = true
AND EXISTS
(
	SELECT generate_series(parent_node.left_token, parent_node.right_token) AS s
		
	EXCEPT

	SELECT child_node.token_index AS s
	FROM _node AS child_node, _rank AS child_rank, _rank AS parent_rank
	WHERE 
		child_rank.node_ref = child_node.id
		AND parent_rank.node_ref = parent_node.id
		AND child_rank.parent = parent_rank.pre
		AND child_rank.component_ref = parent_rank.component_ref
		AND child_node.token_index IS NOT NULL
);

