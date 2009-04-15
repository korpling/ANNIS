-- **************************************************************************************************************
-- *****			SQL create script zur Erzeugung des ANNIS_ViSUALIZATION Schemas				*****
-- **************************************************************************************************************
-- *****	author:		Florian Zipser													*****
-- *****	version:		2.0															*****
-- *****	Datum:		28.07.2008													*****
-- **************************************************************************************************************

-- ===================================== Relation: viz_type =====================================
DROP TABLE viz_type;
CREATE TABLE viz_type
(
	id		numeric(38) NOT NULL,
	"type"	character varying(100) NOT NULL,
	CONSTRAINT "PK_viz_type" PRIMARY KEY (id),
	CONSTRAINT "UNIQUE_type" UNIQUE("type")
);
COMMENT ON TABLE viz_type IS 'Relation viz_type contains all visualization types, wich are supported by ANNIS 2.0.';

-- Fuellen der Tabelle viz_type
INSERT INTO viz_type (id, type) VALUES
	(1,	'TREE');
INSERT INTO viz_type (id, type) VALUES
	(2,	'PARTITURE');
INSERT INTO viz_type (id, type) VALUES
	(3,	'RST');
INSERT INTO viz_type (id, type) VALUES
	(4,	'MMAX');
INSERT INTO viz_type (id, type) VALUES
	(5, 'AUDIO');
INSERT INTO viz_type (id, type) VALUES
	(6,	'VIDEO');
INSERT INTO viz_type (id, type) VALUES
	(7, 'IMAGE');
INSERT INTO viz_type (id, type) VALUES
	(8,	'KWIC');
INSERT INTO viz_type (id, type) VALUES
	(9,	'NONE');

-- ===================================== Relation: corp_2_viz =====================================
DROP TABLE corp_2_viz;
CREATE TABLE corp_2_viz
(
	--id			serial,
	"corpus_id"		character varying(100) NOT NULL,
	"corpus_name"	character varying(100) NOT NULL,
	"level"			character varying(100) NOT NULL,
	"type_ref"		numeric(38) NOT NULL,
	--CONSTRAINT "PK_corp_2_viz" PRIMARY KEY (id),
	CONSTRAINT "UNIQUE_corpus_2_viz" UNIQUE("corpus_id", "level")
);
COMMENT ON TABLE corp_2_viz IS 'Relation corp_2_viz contains a corpus, an annotation level, an annotation name and a reference to the correct visualization type.';

-- ===================================== Relation: Xcorp_2_viz =====================================
-- extended version der visualisierungs tabelle mit annotation
-- beide tabellen wurde der Performance wegen getrennt
DROP TABLE Xcorp_2_viz;
CREATE TABLE Xcorp_2_viz
(
	--id				serial,
	"corpus_id"		character varying(100) NOT NULL,
	"corpus_name"	character varying(100) NOT NULL,
	"level"			character varying(100) NOT NULL,
	"annotation"	character varying(100) NOT NULL,
	"type_ref"		numeric(38) NOT NULL,
	--CONSTRAINT "PK_Xcorp_2_viz" PRIMARY KEY (id),
	CONSTRAINT "UNIQUE_Xcorpus_2_viz" UNIQUE("corpus_id", "level", "annotation")
);
COMMENT ON TABLE Xcorp_2_viz IS 'Relation corp_2_viz contains a corpus, an annotation level, an annotation name and a reference to the correct visualization type.';

-- ===================================== Relation: viz_errors =====================================
-- contains errors of visualization computing
DROP TABLE viz_errors;
CREATE TABLE viz_errors
(
	"corpus_id"		character varying(100) NOT NULL,
	"corpus_name"	character varying(100) NOT NULL,
	"anno_level"	character varying(100) NOT NULL
);
COMMENT ON TABLE viz_errors IS 'Relation viz_errors contains errors of visualization computing'