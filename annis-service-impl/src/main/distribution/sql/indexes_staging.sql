BEGIN; -- transaction

CREATE INDEX tmpidx_pk_corpus ON _corpus (id);
CREATE INDEX tmpidx_pk_node ON _node (id);
CREATE INDEX tmpidx_pk_node_token ON _node(id) WHERE token_index is not null;
CREATE INDEX tmpidx_fk_node_annotation ON _node_annotation (node_ref);
CREATE INDEX tmpidx_pk_component ON _component (id);
CREATE INDEX tmpidx_fk1_rank ON _rank (node_ref);
CREATE INDEX tmpidx_fk2_rank ON _rank (component_ref);
CREATE INDEX tmpidx_rank_pre ON _rank (pre, component_ref);
CREATE INDEX tmpidx_fk2_component_type ON _component ("type");
CREATE INDEX tmpidx_fk_edge_annotation ON _edge_annotation (rank_ref);

END; -- transaction