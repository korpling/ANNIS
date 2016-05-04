-- This will set the "continous" property of the spans correctly
UPDATE _node AS parent_node SET continuous = false
WHERE 
parent_node.continuous = true 
AND parent_node.token_index IS NULL -- exclude token explicitly since they are part of 'c' components
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
)
-- restrict to spans by selecting nodes that are part of a c-component
AND EXISTS (
  SELECT 1 FROM _rank, _component
  WHERE
    component_ref = _component.id
    AND node_ref = parent_node.id
    AND "type" = 'c'
)
;
