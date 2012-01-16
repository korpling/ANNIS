-- _corpus
ALTER TABLE _corpus ADD CONSTRAINT "_PK__corpus" PRIMARY KEY (id);
-- ALTER TABLE _corpus ADD CONSTRAINT "_UNIQ__corpus_name" UNIQUE (name);
ALTER TABLE _corpus ADD CONSTRAINT "_UNIQ__corpus_pre" UNIQUE (pre);
ALTER TABLE _corpus ADD CONSTRAINT "_UNIQ__corpus_post" UNIQUE (post);

-- _corpus_annotation
ALTER TABLE _corpus_annotation ADD CONSTRAINT "_UNIQ__corpus_annotation" UNIQUE (corpus_ref, namespace, name);
ALTER TABLE _corpus_annotation ADD CONSTRAINT "_FK__corpus_annotation__corpus" FOREIGN KEY (corpus_ref) REFERENCES _corpus (id);

-- _text
ALTER TABLE _text ADD CONSTRAINT "_PK__text" PRIMARY KEY (id);

-- _node
ALTER TABLE _node ADD CONSTRAINT "_PK__node" PRIMARY KEY (id);
ALTER TABLE _node ADD CONSTRAINT "_FK__node__text" FOREIGN KEY (text_ref) REFERENCES _text (id);
ALTER TABLE _node ADD CONSTRAINT "_FK__node__corpus" FOREIGN KEY (corpus_ref) REFERENCES _corpus (id);

-- _component
ALTER TABLE _component ADD CONSTRAINT "__PK_component" PRIMARY KEY (id);

-- _rank
ALTER TABLE _rank ADD CONSTRAINT "_PK__rank" PRIMARY KEY (pre);
ALTER TABLE _rank ADD CONSTRAINT "_UNIQ__rank_pre" UNIQUE (pre);
ALTER TABLE _rank ADD CONSTRAINT "_UNIQ__rank_post" UNIQUE (post);
ALTER TABLE _rank ADD CONSTRAINT "_FK__rank_parent" FOREIGN KEY (parent) REFERENCES _rank (pre);
ALTER TABLE _rank ADD CONSTRAINT "_FK__rank__node" FOREIGN KEY (node_ref) REFERENCES _node (id);
ALTER TABLE _rank ADD CONSTRAINT "_FK__rank__component" FOREIGN KEY (component_ref) REFERENCES _component (id);

ALTER TABLE _node_annotation ADD CONSTRAINT "__UNIQ_node_annotation" UNIQUE (node_ref, namespace, name);
ALTER TABLE _node_annotation ADD CONSTRAINT "__FK_node_annotation__node" FOREIGN KEY (node_ref) REFERENCES _node (id);

ALTER TABLE _edge_annotation ADD CONSTRAINT "__UNIQ_edge_annotation" UNIQUE (rank_ref, namespace, name);
ALTER TABLE _edge_annotation ADD CONSTRAINT "__FK_edge_annotation__rank" FOREIGN KEY (rank_ref) REFERENCES _rank (pre);

ALTER TABLE _media_files ADD CONSTRAINT "__UNIQUE_corpus_ref_title" UNIQUE (corpus_ref, title);
ALTER TABLE _media_files ADD CONSTRAINT "__corpus_ref" FOREIGN KEY (corpus_ref) REFERENCES _corpus (id);