--- Meta-Attribute
CREATE TABLE collection
(
    id            numeric(38) NOT NULL,
    type        character varying(100) NOT NULL,
    name        character varying(100) NOT NULL
);
ALTER TABLE collection ADD CONSTRAINT "PK_collection" PRIMARY KEY (id);

CREATE TABLE col_rank
(
    col_ref        numeric(38) NOT NULL,
    pre            numeric(38)    NOT NULL,
    post        numeric(38)    NOT NULL
);
ALTER TABLE col_rank ADD CONSTRAINT "FK_col_rank_2_collection" FOREIGN KEY (col_ref) REFERENCES collection (id);
ALTER TABLE col_rank ADD CONSTRAINT "KEY_col_rank_pre" UNIQUE (pre);
ALTER TABLE col_rank ADD CONSTRAINT "KEY_col_rank_post" UNIQUE (post);

CREATE TABLE meta_attribute
(
    col_ref        numeric(38) NOT NULL,
    name        character varying(2000) NOT NULL,
    value        character varying(2000)
);
ALTER TABLE meta_attribute ADD CONSTRAINT "FK_meta_attribute_2_collection" FOREIGN KEY (col_ref) REFERENCES collection (id);

--- Korpusverwaltung
CREATE TABLE document
(
    id                numeric(38)    NOT NULL,
    name            character varying(100) NOT NULL
);

CREATE TABLE corpus
(
    id                 numeric(38) NOT NULL,
    timestamp_id    numeric(38) NOT NULL,
    name            character varying(100) NOT NULL,
    pre                numeric(38) NOT NULL,
    post            numeric(38)    NOT NULL,
    -- type        character varying(100) NOT NULL, ('DOCUMENT' when pre = post - 1, else 'CORPUS')
    top_level        boolean NOT NULL
);
ALTER TABLE corpus ADD CONSTRAINT "PK_corpus" PRIMARY KEY (id);

CREATE TABLE corpus_stats
(
    corpus_ref    numeric(38) NOT NULL,
    struct    numeric(38) NOT NULL,
    anno    numeric(38) NOT NULL,
    anno_attribute    numeric(38) NOT NULL,
    rank    numeric(38) NOT NULL,
    rank_anno    numeric(38) NOT NULL,
    text    numeric(38) NOT NULL,
    corpus    numeric(38) NOT NULL,
    doc_2_corp    numeric(38) NOT NULL,
    collection    numeric(38) NOT NULL,
    col_rank    numeric(38) NOT NULL,
    meta_attribute    numeric(38) NOT NULL,
    n_tokens    numeric(38)    NOT NULL,
    n_roots    numeric(38) NOT NULL,
    depth    numeric(38) NOT NULL,
    avg_level    real NOT NULL,
    avg_children    real NOT NULL,
    avg_duplicates    real NOT NULL
);
ALTER TABLE corpus_stats ADD CONSTRAINT "FK_corpus_stats_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);

CREATE TABLE doc_2_corp
(
    doc_id                numeric(38)    NOT NULL,
    corpus_ref            numeric(38)    NOT NULL
);
ALTER TABLE doc_2_corp ADD CONSTRAINT "PK_doc" PRIMARY KEY (doc_id, corpus_ref);
ALTER TABLE doc_2_corp ADD CONSTRAINT "FK_doc_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);
ALTER TABLE doc_2_corp ADD CONSTRAINT "KEY_doc_id" UNIQUE (doc_id);

--- ODAG
CREATE TABLE text
(
    id         numeric(38) NOT NULL,
    name    character varying(150) NOT NULL,
    text     text,
    col_ref    numeric(38) NOT NULL
);
ALTER TABLE text ADD CONSTRAINT "PK_text" PRIMARY KEY (id);
ALTER TABLE text ADD CONSTRAINT "FK_text_2_collection" FOREIGN KEY (col_ref) REFERENCES collection (id);

CREATE TABLE struct
(
    id                 numeric(38)    NOT NULL,
    text_ref         numeric(38),
    col_ref            numeric(38),
    doc_ref            numeric(38),
    element_ns        varchar(2000),
    name             character varying(100) NOT NULL,
    type             character varying(100) NOT NULL,
    "left"             integer,
    "right"         integer,
    token_index        integer,
    cont             boolean,
    span            character varying(2000),
    left_token        integer,
    right_token        integer,
    corp_ref        numeric(38)
);
ALTER TABLE struct ADD CONSTRAINT "PK_struct" PRIMARY KEY (id);
ALTER TABLE struct ADD CONSTRAINT "FK_struct_2_collection" FOREIGN KEY (col_ref) REFERENCES collection (id);
ALTER TABLE struct ADD CONSTRAINT "FK_struct_2_text" FOREIGN KEY (text_ref) REFERENCES text (id);
ALTER TABLE struct ADD CONSTRAINT "FK_struct_2_doc" FOREIGN KEY (doc_ref) REFERENCES doc_2_corp (doc_id);

CREATE TABLE rank
(
    pre                numeric(38)    NOT NULL,
    post            numeric(38)    NOT NULL,
    struct_ref        numeric(38)    NOT NULL,
    parent            numeric(38) NULL,
    dominance        boolean,
    level            numeric(38) NOT NULL,
    edge_type            char NOT NULL,
    name            varchar(255),
    zshg            numeric NOT NULL
);
ALTER TABLE rank ADD CONSTRAINT "PK_rank" PRIMARY KEY (struct_ref, pre);
ALTER TABLE rank ADD CONSTRAINT "KEY_rank_pre" UNIQUE (pre);
ALTER TABLE rank ADD CONSTRAINT "KEY_rank_post" UNIQUE (post);
ALTER TABLE rank ADD CONSTRAINT "FK_rank_2_struct" FOREIGN KEY (struct_ref) REFERENCES struct (id);
ALTER TABLE rank ADD CONSTRAINT "FK_rank_2_rank" FOREIGN KEY (parent) REFERENCES rank (pre);

CREATE TABLE rank_anno
(
    rank_ref        numeric(38)    NOT NULL,
    type            character varying(100),
    edge            character varying(100),
    value            character varying(100)
);
ALTER TABLE rank_anno ADD CONSTRAINT "FK_rank_anno_2_rank" FOREIGN KEY (rank_ref) REFERENCES rank (pre);

CREATE TABLE anno
(
    id                 numeric(38)    NOT NULL,
    struct_ref        numeric(38)    NOT NULL,
    col_ref            numeric(38) NOT NULL,
    anno_level        character varying(150)
);
ALTER TABLE anno ADD CONSTRAINT "PK_anno" PRIMARY KEY (id);
ALTER TABLE anno ADD CONSTRAINT "FK_anno_2_collection" FOREIGN KEY (col_ref) REFERENCES collection (id);
ALTER TABLE anno ADD CONSTRAINT "FK_anno_2_struct" FOREIGN KEY (struct_ref) REFERENCES struct (id);

CREATE TABLE anno_attribute
(
    anno_ref        numeric(38) NOT NULL,
    attribute        character varying(150) NOT NULL,
    value            character varying(1500)
);
ALTER TABLE anno_attribute ADD CONSTRAINT "PK_anno_attribute" PRIMARY KEY (anno_ref, attribute);
ALTER TABLE anno_attribute ADD CONSTRAINT "FK_anno_attribute_2_anno" FOREIGN KEY (anno_ref) REFERENCES anno (id);


--- Resolver
CREATE TABLE corp_2_viz
(
    "corpus_name"    character varying(100) NOT NULL,
    "level"            character varying(100) NOT NULL,
    "type_ref"        character varying(100) NOT NULL,
    "corpus_ref"    numeric(38) NOT NULL
);
ALTER TABLE corp_2_viz ADD CONSTRAINT "FK_corp_2_viz_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);

CREATE TABLE xcorp_2_viz
(
    "corpus_name"    character varying(100) NOT NULL,
    "level"            character varying(100) NOT NULL,
    "annotation"    character varying(100) NOT NULL,
    "type_ref"        character varying(100) NOT NULL,
    "corpus_ref"    numeric(38) NOT NULL
);
ALTER TABLE xcorp_2_viz ADD CONSTRAINT "FK_xcorp_2_viz_2_corpus" FOREIGN KEY (corpus_ref) REFERENCES corpus (id);

CREATE TABLE viz_type
(
    id        numeric(38) NOT NULL,
    "type"    character varying(100) NOT NULL
);
ALTER TABLE viz_type ADD CONSTRAINT "PK_viz_type" PRIMARY KEY (id);
ALTER TABLE viz_type ADD  CONSTRAINT "UNIQUE_type" UNIQUE("type");

CREATE TABLE viz_errors
(
    "corpus_id"        character varying(100) NOT NULL,
    "corpus_name"    character varying(100) NOT NULL,
    "anno_level"    character varying(100) NOT NULL
);
COMMENT ON TABLE viz_errors IS 'Relation viz_errors contains errors of visualization computing';

-- External data

CREATE TABLE extData
(
    id            serial,
    "filename"    character varying(500) NOT NULL,
    "orig_name"    character varying(100) NOT NULL,
    "branch"    character varying(100) NOT NULL,
    "mime"        character varying(100) NOT NULL,
    "comment"    character varying(1500) NOT NULL,
    
    CONSTRAINT "PK_extData" PRIMARY KEY (id),
    UNIQUE (filename, branch) 
);
COMMENT ON TABLE extData IS 'This relation stores references and metainformation of external data like files, e.g. waves for the ANNIS system.';

-- Views
CREATE VIEW corpus_info AS SELECT 
    id,
     name,
    timestamp_id,
    n_tokens,
    n_roots,
    depth,
    to_char(avg_level, '990.99') as avg_level,
    to_char(avg_children, '990.99') as avg_children,
    to_char(avg_duplicates, '990.99') as avg_duplicates
FROM 
    corpus, corpus_stats
WHERE
    corpus_stats.corpus_ref = corpus.id;
    
CREATE VIEW table_stats AS select
    (select count(*) from struct ) as struct,    
    (select count(*) from anno ) as anno,
    (select count(*) from anno_attribute ) as anno_attribute,
    (select count(*) from rank ) as rank,    
    (select count(*) from rank_anno ) as rank_anno,    
    (select count(*) from text ) as text,
    (select count(*) from corpus ) as corpus,    
    (select count(*) from doc_2_corp ) as doc_2_corp,    
    (select count(*) from document ) as document,    
    (select count(*) from collection ) as collection,    
    (select count(*) from col_rank ) as col_rank,
    (select count(*) from meta_attribute) as meta_attribute,
    (select count(*) from corp_2_viz) as corp_2_viz,
    (select count(*) from xcorp_2_viz) as xcorp_2_viz,
    (select count(*) from extdata) as extdata
;
