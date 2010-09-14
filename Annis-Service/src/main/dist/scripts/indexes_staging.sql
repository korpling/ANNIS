CREATE INDEX tmpidx__rank__node_ref on _rank(node_ref);
CREATE INDEX tmpidx__rank__component_ref on _rank(component_ref);
CREATE INDEX tmpidx__rank__pre on _rank(pre);

CREATE INDEX tmpidx__node_annotation__node_ref on _node_annotation(node_ref);

CREATE INDEX tmpidx__node__id on _node(id);

CREATE INDEX tmpidx__component__id on _component(id);

CREATE INDEX tmpidx__edge_annotation__rank_ref on _edge_annotation(rank_ref);