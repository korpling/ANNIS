---------------------
-- node_annotation --
---------------------
CREATE INDEX idx__node_annotation__node_:id ON node_annotation_:id(node_ref);
CREATE INDEX idx__node_annotation__value_:id ON node_annotation_:id(name,value,namespace);
CREATE INDEX idx__node_annotation__namespace_:id ON node_annotation_:id(name,namespace);

-----------
--  node --
-----------
CREATE INDEX idx__node_token_index_:id ON node_:id (token_index);

CREATE INDEX idx__node__1_:id ON node_:id (text_ref,toplevel_corpus,"left","right",corpus_ref);
CREATE INDEX idx__node__2_:id ON node_:id (left_token,right_token,text_ref,toplevel_corpus,corpus_ref);
CREATE INDEX idx__node__3_:id ON node_:id (toplevel_corpus,corpus_ref);
CREATE INDEX idx__node__4_:id ON node_:id ("right",text_ref,toplevel_corpus,corpus_ref);
CREATE INDEX idx__node__5_:id ON node_:id (left_token,right_token,toplevel_corpus);
CREATE INDEX idx__node__6_:id ON node_:id (span,toplevel_corpus);
CREATE INDEX idx__node__7_:id ON node_:id (is_token,toplevel_corpus);
CREATE INDEX idx__node__name_:id ON node_:id ("name",namespace);

----------
--facts --
----------

CREATE INDEX idx__facts__id_:id ON facts_:id (id);
CREATE INDEX idx__facts__1_:id ON facts_:id (edge_annotation_name,edge_name,edge_type,parent,pre,edge_annotation_value);
CREATE INDEX idx__facts__2_:id ON facts_:id (edge_name,edge_type,"level",post,pre);
CREATE INDEX idx__facts__3_:id ON facts_:id (edge_name,edge_type,pre,parent);
CREATE INDEX idx__facts__4 ON facts_:id (edge_name,edge_type,post,pre);

CREATE INDEX idx__facts__n2_:id ON facts_:id (edge_type,"level",post,pre) WHERE edge_name IS NULL;
CREATE INDEX idx__facts__n3_:id ON facts_:id (edge_type,pre,parent) WHERE edge_name IS NULL;;
CREATE INDEX idx__facts__n4 ON facts_:id (edge_type,post,pre) WHERE edge_name IS NULL;;

CREATE INDEX idx__facts__pre_:id ON facts_:id (pre);
CREATE INDEX idx__facts__root_:id ON facts_:id (root);
CREATE INDEX idx__facts__toplevel_corpus_:id ON facts_:id (toplevel_corpus);
CREATE INDEX idx__facts__corpus_ref_:id ON facts_:id (corpus_ref);
CREATE INDEX idx__facts__parent_corpus_ref_:id ON facts_:id (parent,corpus_ref);

CREATE INDEX idx__facts__node_annotation_value_:id ON facts_:id(node_annotation_name,node_annotation_value,node_annotation_namespace);
CREATE INDEX idx__facts__node_annotation_namespace_:id ON facts_:id(node_annotation_name,node_annotation_namespace);

-- node on facts
CREATE INDEX idx__facts__6_:id ON facts_:id (span,toplevel_corpus);
CREATE INDEX idx__facts__7_:id ON facts_:id (is_token,toplevel_corpus);
CREATE INDEX idx__facts__name_:id ON facts_:id (node_name,node_namespace);

----- 2nd query
CREATE INDEX idx__2nd_query_:id ON facts_:id (text_ref, left_token, right_token);

-- optimize the select distinct
CREATE INDEX idx_distinct_helper_:id ON facts_:id(id, text_ref, left_token, right_token);
CREATE INDEX idx__column__id_:id on facts_:id using hash (id);
CREATE INDEX idx__column__text_ref_:id on facts_:id using hash (text_ref);
CREATE INDEX idx__column__left_token_:id on facts_:id using hash (left_token);
CREATE INDEX idx__column__right_token_:id on facts_:id using hash (right_token);