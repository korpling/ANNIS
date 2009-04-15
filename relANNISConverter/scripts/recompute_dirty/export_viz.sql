COPY corp_2_viz TO E'/home/annis/data/bulk/TMP/corp_2_viz.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY xcorp_2_viz TO E'/home/annis/data/bulk/TMP/xcorp_2_viz.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY viz_type TO E'/home/annis/data/bulk/TMP/viz_type.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 
COPY viz_errors TO E'/home/annis/data/bulk/TMP/viz_errors.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL'; 