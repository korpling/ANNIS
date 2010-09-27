-- Create left outer join of rank table with its annoations
CREATE TABLE edges AS SELECT
	pre, post, node_ref, parent, type as edge_type, name, component_ref as zshg, level
FROM rank, component
WHERE rank.component_ref = component.id;
