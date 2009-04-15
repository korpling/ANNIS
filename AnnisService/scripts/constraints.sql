--- Meta-Attribute
ALTER TABLE _collection ADD CONSTRAINT "_PK_collection" PRIMARY KEY (id);

ALTER TABLE _col_rank ADD CONSTRAINT "_FK_col_rank_2_collection" FOREIGN KEY (col_ref) REFERENCES _collection (id);
ALTER TABLE _col_rank ADD CONSTRAINT "_KEY_col_rank_pre" UNIQUE (pre);
ALTER TABLE _col_rank ADD CONSTRAINT "_KEY_col_rank_post" UNIQUE (post);

ALTER TABLE _meta_attribute ADD CONSTRAINT "_FK_meta_attribute_2_collection" FOREIGN KEY (col_ref) REFERENCES _collection (id);

--- Korpus-Verwaltung
ALTER TABLE _document ADD CONSTRAINT "_PK_document" PRIMARY KEY (id);

ALTER TABLE _corpus ADD CONSTRAINT "_PK_corpus" PRIMARY KEY (id);
-- ALTER TABLE _corpus ADD CONSTRAINT "_KEY_corpus_pre" UNIQUE (pre);
-- ALTER TABLE _corpus ADD CONSTRAINT "_KEY_corpus_post" UNIQUE (post);

ALTER TABLE _doc_2_corp ADD CONSTRAINT "_PK_doc_2_corpus" PRIMARY KEY (doc_id, corpus_ref);
ALTER TABLE _doc_2_corp ADD CONSTRAINT "_FK_doc_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES _corpus (id);
ALTER TABLE _doc_2_corp ADD CONSTRAINT "_KEY_doc_id" UNIQUE (doc_id);

--- ODAG
ALTER TABLE _text ADD CONSTRAINT "_PK_text" PRIMARY KEY (id);
ALTER TABLE _text ADD CONSTRAINT "_FK_text_2_collection" FOREIGN KEY (col_ref) REFERENCES _collection (id);

ALTER TABLE _struct ADD CONSTRAINT "_PK_struct" PRIMARY KEY (id);
ALTER TABLE _struct ADD CONSTRAINT "_FK_struct_2_collection" FOREIGN KEY (col_ref) REFERENCES _collection (id);
ALTER TABLE _struct ADD CONSTRAINT "_FK_struct_2_text" FOREIGN KEY (text_ref) REFERENCES _text (id);
ALTER TABLE _struct ADD CONSTRAINT "_FK_struct_2_doc" FOREIGN KEY (doc_ref) REFERENCES _doc_2_corp (doc_id);

ALTER TABLE _rank ADD CONSTRAINT "_PK_rank" PRIMARY KEY (struct_ref, pre);
ALTER TABLE _rank ADD CONSTRAINT "_KEY_rank_pre" UNIQUE (pre);
ALTER TABLE _rank ADD CONSTRAINT "_KEY_rank_post" UNIQUE (post);
ALTER TABLE _rank ADD CONSTRAINT "_FK_rank_2_struct" FOREIGN KEY (struct_ref) REFERENCES _struct (id);
ALTER TABLE _rank ADD CONSTRAINT "_FK_rank_2_rank" FOREIGN KEY (parent) REFERENCES _rank (pre);

ALTER TABLE _rank_anno ADD CONSTRAINT "_FK_rank_anno_2_rank" FOREIGN KEY (rank_ref) REFERENCES _rank (pre);

ALTER TABLE _anno ADD CONSTRAINT "_PK_anno" PRIMARY KEY (id);
ALTER TABLE _anno ADD CONSTRAINT "_FK_anno_2_collection" FOREIGN KEY (col_ref) REFERENCES _collection (id);
ALTER TABLE _anno ADD CONSTRAINT "_FK_anno_2_struct" FOREIGN KEY (struct_ref) REFERENCES _struct (id);

ALTER TABLE _anno_attribute ADD CONSTRAINT "_PK_anno_attribute" PRIMARY KEY (anno_ref, attribute);
ALTER TABLE _anno_attribute ADD CONSTRAINT "_FK_anno_attribute_2_anno" FOREIGN KEY (anno_ref) REFERENCES _anno (id);

-- Visualisierung
ALTER TABLE _corp_2_viz ADD CONSTRAINT "_FK_corp_2_viz_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES _corpus (id);
ALTER TABLE _xcorp_2_viz ADD CONSTRAINT "_FK_xcorp_2_viz_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES _corpus (id);
