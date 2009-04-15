-- **************************************************************************************************************
-- *****				SQL script zum Füllen der ANNIS VISUALIZATION Tabellen					*****
-- **************************************************************************************************************
-- *****	author:		Florian Zipser													*****
-- *****	version:		2.0															*****
-- *****	Datum:		28.07.2008													*****
-- **************************************************************************************************************


-- ********************************************
-- ***			MMAX			***
-- ********************************************
-- Bestimmung aller Annotationen bzw. level, die als MMAX angezeigt werden soll
--Heuristik(informell): 	alle struct-Elemente, die in PAULA pointing relations waren
--Heuristik(formell):	haben alle einen rank_anno Eintrag	und 
--				in rank dominance= f 				und 
--				sind keine direkten Tokenannotationen (also struct.order= NULL);

-- scheint zu funktionieren und liefert alle Ergebnisse 
-- -----------------------------------------------------------------
-- ---		Einzeln für alle Annotationen		---
-- -----------------------------------------------------------------
INSERT INTO Xcorp_2_viz(corpus_id, corpus_name, level, annotation, type_ref)
	SELECT 	distinct c1.corp_id as corpus_id,  c1.name as corpus_name, s1.namespace || ':' || s1.type as level, a1.anno_level || ':' || aa1.value as annotation, vt.id as type_ref
	FROM	rank_anno as ra1, rank as r1, struct as s1 LEFT OUTER JOIN	anno as a1 ON (s1.id= a1.struct_ref) LEFT OUTER JOIN anno_attribute as aa1 ON(a1.id= aa1.anno_ref),
			doc_2_corp as d2c1, corpus as c1, viz_type as vt
	WHERE	ra1.rank_ref = r1.pre		AND
			r1.dominance= 'f'			AND
			r1.struct_ref= s1.id		AND
			s1.order is null			AND
			s1.doc_ref= d2c1.doc_ref	AND
			d2c1.corp_ref= c1.id		AND
			vt.type ILIKE 'MMAX'
;

-- -----------------------------------------------------------------
-- ---	Aggregiert für alle Annotationen		---
-- -----------------------------------------------------------------
INSERT INTO corp_2_viz(corpus_id, corpus_name, level, type_ref)
	SELECT 	distinct c1.corp_id as corpus_id,  c1.name as corpus_name, s1.namespace || ':' || s1.type as level, vt.id as type_ref
	FROM	rank_anno as ra1, rank as r1, struct as s1 LEFT OUTER JOIN	anno as a1 ON (s1.id= a1.struct_ref) LEFT OUTER JOIN anno_attribute as aa1 ON(a1.id= aa1.anno_ref),
			doc_2_corp as d2c1, corpus as c1, viz_type as vt
	WHERE	ra1.rank_ref = r1.pre		AND
			r1.dominance= 'f'			AND
			r1.struct_ref= s1.id		AND
			s1.order is null			AND
			s1.doc_ref= d2c1.doc_ref	AND
			d2c1.corp_ref= c1.id		AND
			vt.type ILIKE 'MMAX'
;

-- ********************************************
-- ***				KWIC			***
-- ********************************************
-- Bestimmung aller Annotationen bzw. level, die als KWIC angezeigt werden soll
-- Heuristik (informell):		alle Annotationen, die sich ausschließlich nur auf die Tokenebene beziehen
-- Heuristik(formell):		alle annotationen ermitteln, die sich nur auf struct.id beziehen mit struct.order!= null (Token-Knoten haben struct.order!= null) das heißt
--					es werden alle nicht Token-structs gesucht und anschließend alle annotationen, die nicht auf einen dieser structs zeigen

-- -----------------------------------------------------------------
-- ---		Einzeln für alle Annotationen		---
-- -----------------------------------------------------------------
INSERT INTO Xcorp_2_viz(corpus_id, corpus_name, level, annotation, type_ref)
	SELECT 	distinct c1.corp_id as corpus_id, c1.name as corpus_name, s1.namespace || ':' || s1.type as level, a1.anno_level || ':' || aa1.name as annotation, vt1.id as type_ref
	FROM	anno_attribute as aa1, anno as a1, struct as s1, doc_2_corp as d2c1, corpus as c1, viz_type as vt1 
	WHERE	aa1.anno_ref= a1.id	AND
			aa1.name NOT IN 
					(	SELECT	aa2.name	
						FROM 	anno_attribute as aa2, anno as a2, struct as s2
						WHERE	aa2.anno_ref= a2.id	AND
								a2.struct_ref= s2.id	AND
								s2.order IS null
					)					AND
			a1.struct_ref= s1.id		AND
			s1.doc_ref= d2c1.doc_ref	AND
			d2c1.corp_ref= c1.id		AND
			vt1.type ILIKE 'KWIC'
;

-- -----------------------------------------------------------------
-- ---	Aggregiert für alle Annotationen		---
-- -----------------------------------------------------------------
INSERT INTO corp_2_viz(corpus_id, corpus_name, level, type_ref)
	SELECT 	distinct c1.corp_id as corpus_id, c1.name as corpus_name, s1.namespace || ':' || s1.type as level, vt1.id as type_ref
	FROM	anno_attribute as aa1, anno as a1, struct as s1, doc_2_corp as d2c1, corpus as c1, viz_type as vt1 
	WHERE	aa1.anno_ref= a1.id	AND
			aa1.name NOT IN 
					(	SELECT	aa2.name	
						FROM 	anno_attribute as aa2, anno as a2, struct as s2
						WHERE	aa2.anno_ref= a2.id	AND
								a2.struct_ref= s2.id	AND
								s2.order IS null
					)					AND
			a1.struct_ref= s1.id		AND
			s1.doc_ref= d2c1.doc_ref	AND
			d2c1.corp_ref= c1.id		AND
			vt1.type ILIKE 'KWIC'
;
-- ********************************************
-- ***			PARTITURE			***
-- ********************************************
-- Bestimmung aller Annotationen bzw. level, die als PARTITURE angezeigt werden soll
-- Heuristik (informell):		Der Typ PARTITURE bezieht sich auf alle struct-Elemente, die keine Token sind und die aus dem PAULA-Typ markable entspringen, 
--					sie dürfen aber keine pointing relations gewesen sein, haben also keine rank_anno Eintrag
-- Heuristik(formell):		alle struct-Elemente mit order== 0 und dominance== f (also s.order= 0 und s.id=r.struct_ref und r2.parent= r.pre und r2.dominance=f)

-- -----------------------------------------------------------------
-- ---		Einzeln für alle Annotationen		---
-- -----------------------------------------------------------------
INSERT INTO Xcorp_2_viz(corpus_id, corpus_name, level, annotation, type_ref)
SELECT 	distinct c1.corp_id as corpus_id, c1.name as corpus_name, s1.namespace || ':' || s1.type as level, a1.anno_level || ':' || aa1.name as annotation, vt1.id as type_ref
FROM struct as s1, rank as r1, rank as r2, anno as a1, anno_attribute as aa1, doc_2_corp as d2c1, corpus as c1, viz_type as vt1
where s1.order IS NULL and
s1.id= r1.struct_ref and
r2.parent= r1.pre and
r2.dominance= 'f' AND
s1.id= a1.struct_ref AND
aa1.anno_ref= a1.id AND
aa1.name NOT ILIKE 'audio' AND
s1.doc_ref= d2c1.doc_ref  AND
d2c1.corp_ref= c1.id AND
vt1.type ILIKE 'PARTITURE' 
;		

-- -----------------------------------------------------------------
-- ---	Aggregiert für alle Annotationen		---
-- -----------------------------------------------------------------
INSERT INTO corp_2_viz(corpus_id, corpus_name, level, type_ref)
SELECT distinct c1.corp_id as corpus_id, c1.name as corpus_name, s1.namespace || ':' || s1.type as level, vt1.id as type_ref
FROM struct as s1, rank as r1, rank as r2, anno as a1, anno_attribute as aa1, doc_2_corp as d2c1, corpus as c1, viz_type as vt1
where s1.order IS NULL and
s1.id= r1.struct_ref and
r2.parent= r1.pre and
r2.dominance= 'f' AND
s1.id= a1.struct_ref AND
aa1.anno_ref= a1.id AND
aa1.name NOT ILIKE 'audio' AND
s1.doc_ref= d2c1.doc_ref  AND
d2c1.corp_ref= c1.id AND
vt1.type ILIKE 'PARTITURE' 
;
-- ********************************************
-- ***				TREE				***
-- ********************************************
-- Bestimmung aller Annotationen bzw. level, die als TREE angezeigt werden soll
-- Heuristik (informell):		Der Typ TREE bezieht sich auf alle struct-Elemente, die keine Token sind, die aus dem PAULA-Typ struct entspringen
-- Heuristik(formell):		alle struct-Elemente  mit order== null, mit rank.dominance= t
-- -----------------------------------------------------------------
-- ---		Einzeln für alle Annotationen		---
-- -----------------------------------------------------------------
INSERT INTO Xcorp_2_viz(corpus_id, corpus_name, level, annotation, type_ref)
	SELECT	distinct c1.corp_id as corpus_id, c1.name as corpus_name, s1.namespace || ':' || s1.type as level, a1.anno_level || ':' || aa1.name as annotation, vt1.id as type_ref
	FROM	struct as s1, rank as r1, anno as a1, anno_attribute as aa1, doc_2_corp as d2c1, corpus as c1, viz_type as vt1
	WHERE	s1.order IS NULL			AND
			s1.id=	r1.struct_ref		AND
			r1.dominance= 't'			AND
			aa1.anno_ref= a1.id			AND
			a1.struct_ref= s1.id		AND
			s1.doc_ref= d2c1.doc_ref	AND
			d2c1.corp_ref= c1.id		AND
			vt1.type ILIKE 'TREE'
;		

-- -----------------------------------------------------------------
-- ---	Aggregiert für alle Annotationen		---
-- -----------------------------------------------------------------
INSERT INTO corp_2_viz(corpus_id, corpus_name, level, type_ref)
	SELECT	distinct c1.corp_id as corpus_id, c1.name as corpus_name, s1.namespace || ':' || s1.type as level, vt1.id as type_ref
	FROM	struct as s1, rank as r1, anno as a1, anno_attribute as aa1, doc_2_corp as d2c1, corpus as c1, viz_type as vt1
	WHERE	s1.order IS NULL			AND
			s1.id=	r1.struct_ref		AND
			r1.dominance= 't'			AND
			aa1.anno_ref= a1.id			AND
			a1.struct_ref= s1.id		AND
			s1.doc_ref= d2c1.doc_ref	AND
			d2c1.corp_ref= c1.id		AND
			vt1.type ILIKE 'TREE'
;			
-- ********************************************
-- ***				AUDIO			***
-- ********************************************
-- Bestimmung aller Annotationen bzw. level, die als AUDIO angezeigt werden soll
-- Heuristik (informell):		Der Typ AUDIO bezieht sich auf alle struct-Elemente, die vom Typ Audio sind
-- Heuristik(formell):		alle struct-Elemente  mit order== null, mit rank.dominance= t
-- -----------------------------------------------------------------
-- ---		Einzeln für alle Annotationen		---
-- -----------------------------------------------------------------
INSERT INTO Xcorp_2_viz(corpus_id, corpus_name, level, annotation, type_ref)
	SELECT	distinct c1.corp_id as corpus_id, c1.name as corpus_name, s1.namespace || ':' || s1.type as level, a1.anno_level || ':' || aa1.name as annotation, vt1.id as type_ref
	FROM	struct as s1, rank as r1, anno as a1, anno_attribute as aa1, doc_2_corp as d2c1, corpus as c1, viz_type as vt1
	WHERE	aa1.name ILIKE 'audio'		AND
			aa1.anno_ref= a1.id			AND
			a1.struct_ref= s1.id		AND
			s1.doc_ref= d2c1.doc_ref	AND
			d2c1.corp_ref= c1.id		AND
			vt1.type ILIKE 'AUDIO'
;
-- -----------------------------------------------------------------
-- ---	Aggregiert für alle Annotationen		---
-- -----------------------------------------------------------------
INSERT INTO corp_2_viz(corpus_id, corpus_name, level, type_ref)
	SELECT	distinct c1.corp_id as corpus_id, c1.name as corpus_name, s1.namespace || ':' || s1.type as level, vt1.id as type_ref
	FROM	struct as s1, rank as r1, anno as a1, anno_attribute as aa1, doc_2_corp as d2c1, corpus as c1, viz_type as vt1
	WHERE	aa1.name ILIKE 'audio'		AND
			aa1.anno_ref= a1.id			AND
			a1.struct_ref= s1.id		AND
			s1.doc_ref= d2c1.doc_ref	AND
			d2c1.corp_ref= c1.id		AND
			vt1.type ILIKE 'AUDIO'
;
-- *************************************************
-- ***		looking for errors while computing		***
-- *************************************************
INSERT INTO viz_errors(corpus_id, corpus_name, anno_level)
SELECT DISTINCT c1.corp_id, c1.name, s1.namespace || ':' || s1.type as anno_level
FROM corpus as c1, struct as s1, doc_2_corp as d2c1, anno as a1
WHERE (CAST(c1.corp_id AS character varying(100)) || (s1.namespace || ':' || s1.type)) NOT IN (SELECT c2v.corpus_id || c2v.level FROM corp_2_viz as c2v) AND
 s1.id= a1.struct_ref AND
 s1.doc_ref=d2c1.doc_ref AND
 d2c1.corp_ref=c1.id;