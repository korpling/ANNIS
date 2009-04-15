-- **************************************************************************************************************
-- *****				SQL script Exportieren des Datenbankinhaltes in tab Dateien						*****
-- **************************************************************************************************************
-- *****	author:		Florian Zipser													*****
-- *****	version:		2.2															*****
-- *****	Datum:		16.06.2008														*****
-- **************************************************************************************************************

COPY meta_attribute TO E'/home/florian/data/bulk/tiger_176/meta_attribute_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY anno_attribute TO E'/home/florian/data/bulk/tiger_176/anno_attribute_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY anno TO E'/home/florian/data/bulk/tiger_176/anno_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY rank_anno TO E'/home/florian/data/bulk/tiger_176/rank_anno_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY rank TO E'/home/florian/data/bulk/tiger_176/rank_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY doc_2_korp TO E'/home/florian/data/bulk/tiger_176/doc_2_korp_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY struct TO E'/home/florian/data/bulk/tiger_176/struct_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY text TO E'/home/florian/data/bulk/tiger_176/text_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY korpus TO E'/home/florian/data/bulk/tiger_176/korpus_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY col_rank TO E'/home/florian/data/bulk/tiger_176/col_rank_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY collection TO E'/home/florian/data/bulk/tiger_176/collection_bulk.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
