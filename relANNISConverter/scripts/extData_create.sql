-- ===================================== Relation: externalData =====================================
DROP TABLE extData;
CREATE TABLE extData
(
	id			serial,
	"filename"	character varying(500) NOT NULL,
	"orig_name"	character varying(100) NOT NULL,
	"branch"	character varying(100) NOT NULL,
	"mime"		character varying(100) NOT NULL,
	"comment"	character varying(1500) NOT NULL,
	
	CONSTRAINT "PK_extData" PRIMARY KEY (id),
	UNIQUE (filename, branch) 
);
COMMENT ON TABLE extData IS 'This relation stores references and metainformation of external data like files, e.g. waves for the ANNIS system.';
