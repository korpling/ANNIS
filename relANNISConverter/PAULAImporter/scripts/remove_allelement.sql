-- **************************************************************************************************************
-- *****			SQL script zum Entfernen des ALL-Knotens in der struct und rank-Tabelle				*****
-- **************************************************************************************************************
-- *****	author:		Florian Zipser													*****
-- *****	version:		2.2															*****
-- *****	Datum:		16.06.2008														*****
-- **************************************************************************************************************

-- Alle rank-Einträge updaten (parent auf NULL setzen), deren parent Wert auf den rank-Eintrag zum ALL-Knoten zeigt  		
UPDATE	rank
SET		parent= NULL
WHERE	parent	IN (	SELECT r.pre
						FROM 	rank as r, struct as s
						WHERE	s.name ILIKE 'ALL'	AND
								r.struct_ref= s.id);
				
-- rank-Eintrag löschen, der auf das ALL-Element zeigt
DELETE 
FROM	rank
WHERE	rank.struct_ref IN 	(SELECT	r.struct_ref
							FROM	rank as r, struct as s
							WHERE	s.name ILIKE 'ALL'	AND
									r.struct_ref = s.id);

-- ALL-Element der struct-Tabelle löschen
DELETE 
FROM	struct
WHERE	name ILIKE 'ALL';