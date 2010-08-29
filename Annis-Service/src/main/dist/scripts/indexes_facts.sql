-- column indexes, these will take some space, but are very useful
-- for genric query testing
--CREATE INDEX idx__column_id_:id on facts_:id(id);
--CREATE INDEX idx__column_text_ref_:id on facts_:id(text_ref);
--CREATE INDEX idx__column_corpus_ref_:id on facts_:id(corpus_ref);
--CREATE INDEX idx__column_toplevel_corpus_:id on facts_:id(toplevel_corpus);
--CREATE INDEX idx__column_node_namespace_:id on facts_:id(node_namespace);
--CREATE INDEX idx__column_node_name_:id on facts_:id(node_name);
--CREATE INDEX idx__column_left_:id on facts_:id("left");
--CREATE INDEX idx__column_right_:id on facts_:id("right");
--CREATE INDEX idx__column_token_index_:id on facts_:id(token_index);
--CREATE INDEX idx__column_continuous_:id on facts_:id(continuous);
--CREATE INDEX idx__column_span_:id on facts_:id(span);
--CREATE INDEX idx__column_left_token_:id on facts_:id(left_token);
--CREATE INDEX idx__column_right_token_:id on facts_:id(right_token);
--CREATE INDEX idx__column_pre_:id on facts_:id(pre);
--CREATE INDEX idx__column_post_:id on facts_:id(post);
--CREATE INDEX idx__column_parent_:id on facts_:id(parent);
--CREATE INDEX idx__column_root_:id on facts_:id(root);
--CREATE INDEX idx__column_level_:id on facts_:id(level);
--CREATE INDEX idx__column_component_id_:id on facts_:id(component_id);
--CREATE INDEX idx__column_edge_type_:id on facts_:id(edge_type);
--CREATE INDEX idx__column_edge_namespace_:id on facts_:id(edge_namespace);
--CREATE INDEX idx__column_edge_name_:id on facts_:id(edge_name);
--CREATE INDEX idx__column_node_annotation_namespace_:id on facts_:id(node_annotation_namespace);
--CREATE INDEX idx__column_node_annotation_name_:id on facts_:id(node_annotation_name);
--CREATE INDEX idx__column_node_annotation_value_:id on facts_:id(node_annotation_value);
--CREATE INDEX idx__column_edge_annotation_namespace_:id on facts_:id(edge_annotation_namespace);
--CREATE INDEX idx__column_edge_annotation_name_:id on facts_:id(edge_annotation_name)


-- Suche kombiniert mit parent
CREATE INDEX idx_c__parent__node_:id ON facts_:id (parent);
CREATE INDEX idx_c__parent__token_:id ON facts_:id (parent) WHERE token_index IS NULL;
CREATE INDEX idx_c__parent__span_:id ON facts_:id (span varchar_pattern_ops, parent);
CREATE INDEX idx_c__parent__node_anno_ex_:id ON facts_:id (node_annotation_name, parent);
CREATE INDEX idx_c__parent__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, parent);
CREATE INDEX idx_c__parent__edge_anno_ex_:id ON facts_:id (edge_annotation_name, parent);
CREATE INDEX idx_c__parent__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, parent);

-- Suche kombiniert mit pre WHERE type = d
CREATE INDEX idx_c__dom__node_:id ON facts_:id (pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__token_:id ON facts_:id (pre) WHERE token_index IS NULL AND edge_type = 'd';
CREATE INDEX idx_c__dom__span_:id ON facts_:id (span varchar_pattern_ops, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__node_anno_ex_:id ON facts_:id (node_annotation_name, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__edge_anno_ex_:id ON facts_:id (edge_annotation_name, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, pre) WHERE edge_type = 'd';

-- Suche kombiniert mit edge_name, pre WHERE type = p
CREATE INDEX idx_c__pr__node_:id ON facts_:id (edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__token_:id ON facts_:id (edge_name, pre) WHERE token_index IS NULL AND edge_type = 'p';
CREATE INDEX idx_c__pr__span_:id ON facts_:id (span varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__node_anno_ex_:id ON facts_:id (node_annotation_name, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__edge_anno_ex_:id ON facts_:id (edge_annotation_name, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';

----- Prezedenz
-- Suche kombiniert mit text_ref
CREATE INDEX idx_c__text__node_:id ON facts_:id (text_ref);
CREATE INDEX idx_c__text__token_:id ON facts_:id (text_ref) WHERE token_index IS NULL;
CREATE INDEX idx_c__text__span_:id ON facts_:id (span varchar_pattern_ops, text_ref);
CREATE INDEX idx_c__text__node_anno_ex_:id ON facts_:id (node_annotation_name, text_ref);
CREATE INDEX idx_c__text__node_anno_:id ON facts_:id (node_annotation_name, node_annotation_value varchar_pattern_ops, text_ref);
CREATE INDEX idx_c__text__edge_anno_ex_:id ON facts_:id (edge_annotation_name, text_ref);
CREATE INDEX idx_c__text__edge_anno_:id ON facts_:id (edge_annotation_name, edge_annotation_value varchar_pattern_ops, text_ref);

----- _=_
CREATE INDEX idx__exact_cover_:id ON facts_:id (text_ref, "left", "right");

----- 2nd query
CREATE INDEX idx__column__id_:id on facts_:id using hash (id);

-- index the facts_context;
CREATE INDEX idx_facts_context_main_:id ON facts_context_:id
  (id, text_ref, left_token,right_token, corpus_ref, toplevel_corpus)
WHERE toplevel_corpus = :id;
