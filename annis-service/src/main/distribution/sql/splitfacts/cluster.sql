CREATE INDEX idx__cluster__:id ON facts_node_:id(corpus_ref, n_sample, id);
CLUSTER facts_node_:id USING idx__cluster__:id;
DROP INDEX idx__cluster__:id;

CREATE INDEX idx__cluster__:id ON facts_edge_:id(component_id, r_c_sample, pre);
CLUSTER facts_edge_:id USING idx__cluster__:id;
DROP INDEX idx__cluster__:id;