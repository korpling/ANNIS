BEGIN; -- transaction
----------
--facts --
----------
-- Suche kombiniert mit parent
CREATE INDEX idx_c__parent__node_:id ON facts_:id (parent);
CREATE INDEX idx_c__parent__token_:id ON facts_:id (parent) WHERE is_token IS TRUE;
CREATE INDEX idx_c__parent__span_:id ON facts_:id (span varchar_pattern_ops, parent);
CREATE INDEX idx_c__parent__node_anno_ex_:id ON facts_:id (node_annotation_name, parent);
CREATE INDEX idx_c__parent__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, parent);
CREATE INDEX idx_c__parent__edge_anno_ex_:id ON facts_:id (edge_annotation_name, parent);
CREATE INDEX idx_c__parent__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, parent);

-- Suche kombiniert mit pre WHERE type = d
CREATE INDEX idx_c__dom__node_:id ON facts_:id (pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__token_:id ON facts_:id (pre) WHERE is_token IS TRUE AND edge_type = 'd';
CREATE INDEX idx_c__dom__span_:id ON facts_:id (span varchar_pattern_ops, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__node_anno_ex_:id ON facts_:id (node_annotation_name, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__edge_anno_ex_:id ON facts_:id (edge_annotation_name, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, pre) WHERE edge_type = 'd';

-- Suche kombiniert mit edge_name, pre WHERE type = p
CREATE INDEX idx_c__pr__node_:id ON facts_:id (edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__token_:id ON facts_:id (edge_name, pre) WHERE is_token IS TRUE AND edge_type = 'p';
CREATE INDEX idx_c__pr__span_:id ON facts_:id (span varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__node_anno_ex_:id ON facts_:id (node_annotation_name, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__edge_anno_ex_:id ON facts_:id (edge_annotation_name, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';

----- Prezedenz
-- Suche kombiniert mit text_ref
CREATE INDEX idx_c__text__node_:id ON facts_:id (text_ref);
CREATE INDEX idx_c__text__token_:id ON facts_:id (text_ref) WHERE is_token IS TRUE;
CREATE INDEX idx_c__text__span_:id ON facts_:id (span varchar_pattern_ops, text_ref);
CREATE INDEX idx_c__text__node_anno_ex_:id ON facts_:id (node_annotation_name, text_ref);
CREATE INDEX idx_c__text__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, text_ref);
CREATE INDEX idx_c__text__edge_anno_ex_:id ON facts_:id (edge_annotation_name, text_ref);
CREATE INDEX idx_c__text__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, text_ref);

----- _=_, _i_ etc.
CREATE INDEX idx__exact_cover_:id ON facts_:id (text_ref,"left", "right");

-- search for token
CREATE INDEX idx__token_search_:id on facts_:id (is_token, toplevel_corpus);

----- 2nd query
CREATE INDEX idx__2nd_query_:id ON facts_:id (text_ref,left_token, right_token);

-- optimize the select distinct
CREATE INDEX idx_distinct_helper_:id ON facts_:id(id, text_ref, left_token, right_token);

-- allow search for nodes
CREATE INDEX idx__sample_n__:id ON facts_:id(sample_n);
CREATE INDEX idx__sample_n_r_c__:id ON facts_:id(sample_n_r_c);
CREATE INDEX idx__sample_n_na__:id ON facts_:id(sample_n_na);
CREATE INDEX idx__sample_n_r_c_ea__:id ON facts_:id(sample_n_r_c_ea);
CREATE INDEX idx__sample_n_r_c_na__:id ON facts_:id(sample_n_r_c_na);

END; -- transaction
