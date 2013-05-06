-- calculates the median of all available annotation names
WITH
	counted_annotation_names AS (
		SELECT count(name), name FROM _node_annotation GROUP BY name ORDER BY count
	),
	median AS (
		SELECT (count(*)/2) as median FROM counted_annotation_names
	),
	grep_median AS (
		SELECT * FROM counted_annotation_names OFFSET (SELECT (count(*)/2) as median FROM counted_annotation_names) LIMIT 1
	)

SELECT * FROM grep_median;
