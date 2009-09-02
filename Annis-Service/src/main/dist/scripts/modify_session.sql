--- Session
SET enable_seqscan TO off;
-- SET enable_nestloop TO off;
-- SET enable_mergejoin TO off;
-- SET enable_hashjoin TO off;
-- SET enable_bitmapscan TO off;

--- Spaltenindexe
DROP INDEX idx_f_component_id;
DROP INDEX idx_f_corpus_ref;
DROP INDEX idx_f_edge_anno_name;
DROP INDEX idx_f_edge_anno_value;
DROP INDEX idx_f_edge_name;
DROP INDEX idx_f_edge_type;
DROP INDEX idx_f_left;
DROP INDEX idx_f_left_token;
DROP INDEX idx_f_level;
DROP INDEX idx_f_node_anno_name;
DROP INDEX idx_f_node_anno_value;
DROP INDEX idx_f_parent;
DROP INDEX idx_f_post;
DROP INDEX idx_f_pre;
DROP INDEX idx_f_right;
DROP INDEX idx_f_right_token;
DROP INDEX idx_f_root;
DROP INDEX idx_f_span;
DROP INDEX idx_f_text_ref;
DROP INDEX idx_f_token_index;
DROP INDEX idx_f_toplevel_corpus;

--- kombinierte Indexe
--DROP INDEX idx_c__dom__edge_anno;
--DROP INDEX idx_c__dom__edge_anno_ex;
--DROP INDEX idx_c__dom__node;
--DROP INDEX idx_c__dom__node_anno;
--DROP INDEX idx_c__dom__node_anno_ex;
--DROP INDEX idx_c__dom__span;
--DROP INDEX idx_c__dom__token;
--DROP INDEX idx_c__parent__edge_anno;
--DROP INDEX idx_c__parent__edge_anno_ex;
--DROP INDEX idx_c__parent__node;
--DROP INDEX idx_c__parent__node_anno;
--DROP INDEX idx_c__parent__node_anno_ex;
--DROP INDEX idx_c__parent__span;
--DROP INDEX idx_c__parent__token;
--DROP INDEX idx_c__pr__edge_anno;
--DROP INDEX idx_c__pr__edge_anno_ex;
--DROP INDEX idx_c__pr__node;
--DROP INDEX idx_c__pr__node_anno;
--DROP INDEX idx_c__pr__node_anno_ex;
--DROP INDEX idx_c__pr__span;
--DROP INDEX idx_c__pr__token;
--DROP INDEX idx_c__text__edge_anno;
--DROP INDEX idx_c__text__edge_anno_ex;
--DROP INDEX idx_c__text__node;
--DROP INDEX idx_c__text__node_anno;
--DROP INDEX idx_c__text__node_anno_ex;
--DROP INDEX idx_c__text__span;
--DROP INDEX idx_c__text__token;

--- Mit Namespace
--DROP INDEX idx_c__dom__edge_anno_ns;
--DROP INDEX idx_c__dom__edge_anno_ns_ex;
--DROP INDEX idx_c__dom__node_anno_ns;
--DROP INDEX idx_c__dom__node_anno_ns_ex;
--DROP INDEX idx_c__parent__edge_anno_ns;
--DROP INDEX idx_c__parent__edge_anno_ns_ex;
--DROP INDEX idx_c__parent__node_anno_ns;
--DROP INDEX idx_c__parent__node_anno_ns_ex;
--DROP INDEX idx_c__pr__edge_anno_ns;
--DROP INDEX idx_c__pr__edge_anno_ns_ex;
--DROP INDEX idx_c__pr__node_anno_ns;
--DROP INDEX idx_c__pr__node_anno_ns_ex;
--DROP INDEX idx_c__text__edge_anno_ns;
--DROP INDEX idx_c__text__edge_anno_ns_ex;
--DROP INDEX idx_c__text__node_anno_ns;
--DROP INDEX idx_c__text__node_anno_ns_ex;

--- Experimentell
-- DROP INDEX d__c__exact_cover;
-- DROP INDEX idx__2nd_query;
-- DROP INDEX idx__exact_cover;
