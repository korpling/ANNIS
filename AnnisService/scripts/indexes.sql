-- Suche kombiniert mit parent
CREATE INDEX idx_c__parent__node ON facts (toplevel_corpus, parent);
CREATE INDEX idx_c__parent__token ON facts (toplevel_corpus, parent) WHERE token_index IS NULL;
CREATE INDEX idx_c__parent__span ON facts (toplevel_corpus, span varchar_pattern_ops, parent);
CREATE INDEX idx_c__parent__node_anno_ex ON facts (toplevel_corpus, node_annotation_name, parent);
CREATE INDEX idx_c__parent__node_anno ON facts (toplevel_corpus, node_annotation_name, node_annotation_value varchar_pattern_ops, parent);
CREATE INDEX idx_c__parent__edge_anno_ex ON facts (toplevel_corpus, edge_annotation_name, parent);
CREATE INDEX idx_c__parent__edge_anno ON facts (toplevel_corpus, edge_annotation_name, edge_annotation_value varchar_pattern_ops, parent);
--CREATE INDEX idx_c__parent__node_anno_ns_ex ON facts (toplevel_corpus, node_annotation_namespace, node_annotation_name, parent);
--CREATE INDEX idx_c__parent__node_anno_ns ON facts (toplevel_corpus, node_annotation_namespace, node_annotation_name, node_annotation_value varchar_pattern_ops, parent);
--CREATE INDEX idx_c__parent__edge_anno_ns_ex ON facts (toplevel_corpus, edge_annotation_namespace, edge_annotation_name, parent);
--CREATE INDEX idx_c__parent__edge_anno_ns ON facts (toplevel_corpus, edge_annotation_namespace, edge_annotation_name, edge_annotation_value varchar_pattern_ops, parent);

-- Suche kombiniert mit pre WHERE type = d
CREATE INDEX idx_c__dom__node ON facts (toplevel_corpus, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__token ON facts (toplevel_corpus, pre) WHERE token_index IS NULL AND edge_type = 'd';
CREATE INDEX idx_c__dom__span ON facts (toplevel_corpus, span varchar_pattern_ops, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__node_anno_ex ON facts (toplevel_corpus, node_annotation_name, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__node_anno ON facts (toplevel_corpus, node_annotation_name, node_annotation_value varchar_pattern_ops, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__edge_anno_ex ON facts (toplevel_corpus, edge_annotation_name, pre) WHERE edge_type = 'd';
CREATE INDEX idx_c__dom__edge_anno ON facts (toplevel_corpus, edge_annotation_name, edge_annotation_value varchar_pattern_ops, pre) WHERE edge_type = 'd';
--CREATE INDEX idx_c__dom__node_anno_ns_ex ON facts (toplevel_corpus, node_annotation_namespace, node_annotation_name, pre) WHERE edge_type = 'd';
--CREATE INDEX idx_c__dom__node_anno_ns ON facts (toplevel_corpus, node_annotation_namespace, node_annotation_name, node_annotation_value varchar_pattern_ops, pre) WHERE edge_type = 'd';
--CREATE INDEX idx_c__dom__edge_anno_ns_ex ON facts (toplevel_corpus, edge_annotation_namespace, edge_annotation_name, pre) WHERE edge_type = 'd';
--CREATE INDEX idx_c__dom__edge_anno_ns ON facts (toplevel_corpus, edge_annotation_namespace, edge_annotation_name, edge_annotation_value varchar_pattern_ops, pre) WHERE edge_type = 'd';

-- Suche kombiniert mit edge_name, pre WHERE type = p
CREATE INDEX idx_c__pr__node ON facts (toplevel_corpus, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__token ON facts (toplevel_corpus, edge_name, pre) WHERE token_index IS NULL AND edge_type = 'p';
CREATE INDEX idx_c__pr__span ON facts (toplevel_corpus, span varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__node_anno_ex ON facts (toplevel_corpus, node_annotation_name, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__node_anno ON facts (toplevel_corpus, node_annotation_name, node_annotation_value varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__edge_anno_ex ON facts (toplevel_corpus, edge_annotation_name, edge_name, pre) WHERE edge_type = 'p';
CREATE INDEX idx_c__pr__edge_anno ON facts (toplevel_corpus, edge_annotation_name, edge_annotation_value varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';
--CREATE INDEX idx_c__pr__node_anno_ns_ex ON facts (toplevel_corpus, node_annotation_namespace, node_annotation_name, edge_name, pre) WHERE edge_type = 'p';
--CREATE INDEX idx_c__pr__node_anno_ns ON facts (toplevel_corpus, node_annotation_namespace, node_annotation_name, node_annotation_value varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';
--CREATE INDEX idx_c__pr__edge_anno_ns_ex ON facts (toplevel_corpus, edge_annotation_namespace, edge_annotation_name, edge_name, pre) WHERE edge_type = 'p';
--CREATE INDEX idx_c__pr__edge_anno_ns ON facts (toplevel_corpus, edge_annotation_namespace, edge_annotation_name, edge_annotation_value varchar_pattern_ops, edge_name, pre) WHERE edge_type = 'p';

----- Prezedenz
-- Suche kombiniert mit text_ref
CREATE INDEX idx_c__text__node ON facts (toplevel_corpus, text_ref);
CREATE INDEX idx_c__text__token ON facts (toplevel_corpus, text_ref) WHERE token_index IS NULL;
CREATE INDEX idx_c__text__span ON facts (toplevel_corpus, span varchar_pattern_ops, text_ref);
CREATE INDEX idx_c__text__node_anno_ex ON facts (toplevel_corpus, node_annotation_name, text_ref);
CREATE INDEX idx_c__text__node_anno ON facts (toplevel_corpus, node_annotation_name, node_annotation_value varchar_pattern_ops, text_ref);
CREATE INDEX idx_c__text__edge_anno_ex ON facts (toplevel_corpus, edge_annotation_name, text_ref);
CREATE INDEX idx_c__text__edge_anno ON facts (toplevel_corpus, edge_annotation_name, edge_annotation_value varchar_pattern_ops, text_ref);
--CREATE INDEX idx_c__text__node_anno_ns_ex ON facts (toplevel_corpus, node_annotation_namespace, node_annotation_name, text_ref);
--CREATE INDEX idx_c__text__node_anno_ns ON facts (toplevel_corpus, node_annotation_namespace, node_annotation_name, node_annotation_value varchar_pattern_ops, text_ref);
--CREATE INDEX idx_c__text__edge_anno_ns_ex ON facts (toplevel_corpus, edge_annotation_namespace, edge_annotation_name, text_ref);
--CREATE INDEX idx_c__text__edge_anno_ns ON facts (toplevel_corpus, edge_annotation_namespace, edge_annotation_name, edge_annotation_value varchar_pattern_ops, text_ref);

----- _=_
CREATE INDEX idx__exact_cover ON facts (text_ref, "left", "right");
