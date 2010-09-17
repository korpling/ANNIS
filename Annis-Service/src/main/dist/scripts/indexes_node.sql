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