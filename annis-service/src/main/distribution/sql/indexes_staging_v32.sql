CREATE INDEX tmpidx_pk_corpus ON _corpus (id);
CREATE INDEX tmpidx_pk_node ON _node (id);
CREATE INDEX tmpidx_pk_text_ref ON _node (text_ref);
CREATE INDEX tmpidx_pk_node_token ON _node(id) WHERE token_index is not null;
CREATE INDEX tmpidx_left_token_helper ON _node ("left", corpus_ref, text_ref, token_index) WHERE token_index IS NOT NULL;
CREATE INDEX tmpidx_right_token_helper ON _node ("right", corpus_ref, text_ref, token_index) WHERE token_index IS NOT NULL; 
CREATE INDEX tmpidx_unique_name_appendix_helper ON _node ("name", corpus_ref, id); 

CREATE INDEX tmpidx_fk_node_annotation ON _node_annotation (node_ref);
CREATE INDEX tmpidx_pk_component ON _component (id);
CREATE INDEX tmpidx_type_component ON _component ("type");

CREATE INDEX tmpidx_fk1_rank ON _rank (node_ref);
CREATE INDEX tmpidx_fk2_rank ON _rank (component_ref);
CREATE INDEX tmpidx_rank_pre ON _rank (pre, component_ref);

CREATE INDEX tmpidx_fk_edge_annotation ON _edge_annotation (rank_ref);


CREATE INDEX tmpidx__continuous_helper1 ON _rank (component_ref, parent, node_ref);
CREATE INDEX tmpidx__continuous_helper2 ON _node (id, token_index) WHERE token_index IS NOT NULL;
