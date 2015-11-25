CREATE INDEX tmpidx_pk_node_token ON _node(id) WHERE token_index is not null;
CREATE INDEX tmpidx_unique_name_appendix_helper ON _node ("name", corpus_ref, id); 

CREATE INDEX tmpidx_fk1_rank ON _rank (node_ref);
CREATE INDEX tmpidx__continuous_helper1 ON _rank (component_ref, parent, node_ref);

CREATE INDEX tmpidx_fk_edge_annotation ON _edge_annotation (rank_ref);



