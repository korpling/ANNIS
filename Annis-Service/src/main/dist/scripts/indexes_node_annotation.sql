---------------------
-- node_annotation --
---------------------
CREATE INDEX idx__node_annotation__node_:id ON node_annotation_:id(node_ref);
CREATE INDEX idx__node_annotation__value_:id ON node_annotation_:id(name,value,namespace);
CREATE INDEX idx__node_annotation__namespace_:id ON node_annotation_:id(name,namespace);