-- alle Tabelleneinträge löschen
delete from anno_attribute;
delete from anno;
delete from rank;
delete from struct;
delete from text;
delete from doc_2_korp;
delete from korpus;
delete from meta_attribute;
delete from collection;


-- alle Structurknoten, die eine Annotation haben (mit Annotationslevel)
SELECT	s1.id, s1.name, s1. cont, a1.anno_level
FROM	anno as a1, struct as s1
WHERE	a1.struct_ref= s1.id;

-- alle Textknoten mit Textdateien in Collection
SELECT 	t1.id, t1.name, c1.type as COL_TYPE, c1.name AS COL_NAME
FROM 	text as t1, collection as c1
WHERE	c1.id= t1.col_ref;

-- alle Annotationsknoten mit Textdateien in Collection
SELECT 	a1.id, a1.struct_ref, a1.anno_level, c1.type as COL_TYPE, c1.name AS COL_NAME
FROM 	anno as a1, collection as c1
WHERE	c1.id= a1.col_ref;

-- alle Strukturknoten mit Textdateien in Collection
SELECT 	s1.id, s1.name, t1.name as text_name, s1.left, s1.right, s1.text, c1.type as COL_TYPE, c1.name AS COL_NAME
FROM 	struct as s1, collection as c1, text as t1
WHERE	c1.id= s1.col_ref AND
		t1.id= s1.text_ref;	
-- alle Collections mit deren Annotationen
SELECT	c1.name, c1.type, ma1.name, ma1.value
FROM	collection as c1, meta_attribute as ma1
WHERE	c1.id= ma1.col_ref;