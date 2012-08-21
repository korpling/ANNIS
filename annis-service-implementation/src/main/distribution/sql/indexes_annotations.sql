---------------------
-- node_annotation --
---------------------
CREATE INDEX idx__annotatations__order_:id ON annotations_:id(namespace, name, occurences);
CREATE INDEX idx__annotatations__value_:id ON annotations_:id(value);
