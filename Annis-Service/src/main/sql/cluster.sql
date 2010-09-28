CREATE INDEX idx__sample_cluster__:id ON facts_:id(sample_node, sample_node_annotation);

CLUSTER facts_:id USING idx__sample_cluster__:id;

DROP INDEX idx__sample_cluster__:id;