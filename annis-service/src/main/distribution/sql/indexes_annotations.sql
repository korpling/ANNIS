---------------------
-- node_annotation --
---------------------
CREATE INDEX idx__annotations__order_:id ON annotations_:id(namespace, name, occurences);
CREATE INDEX idx__annotations__order2_:id ON annotations_:id (namespace, "name", edge_namespace, edge_name, occurences DESC);
CREATE INDEX idx__annotations__value_:id ON annotations_:id(value);