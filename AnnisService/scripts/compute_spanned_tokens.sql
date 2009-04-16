CREATE OR REPLACE FUNCTION compute_spanned_tokens() RETURNS INTEGER AS $$
DECLARE
	count INTEGER := 0;
	old_count INTEGER := 0;
BEGIN
	-- get # of entries where left_token, right_token is unset or changed from previous run
	PERFORM * FROM _tmp_spanned_tokens
		WHERE old_left IS NULL OR old_left != left_token OR old_right IS NULL OR old_right != right_token;
	GET DIAGNOSTICS count = ROW_COUNT;

	-- first step: init left_token, right_token
	-- do it outside the loop, because count = old_count after this step
	RAISE NOTICE 'computing minimum and maximum covered token_index for each node, % nodes to go', count;
	SELECT compute_spanned_tokens_step() INTO count;

	-- loop...
	WHILE count > 0 AND count != old_count LOOP
		old_count := count;
		SELECT compute_spanned_tokens_step() INTO count;

		-- count nodes that still need update
		PERFORM * FROM _tmp_spanned_tokens
			WHERE old_left IS NULL OR old_left != left_token OR old_right IS NULL OR old_right != right_token;
		GET DIAGNOSTICS count = ROW_COUNT;
		RAISE NOTICE 'updating nodes by component, % nodes updated, % to go', old_count - count, count;
	END LOOP;

	RAISE NOTICE '% nodes could not be connected to the rest of the graph', count;

	RETURN count;
END;
$$ language plpgsql;

CREATE OR REPLACE FUNCTION compute_spanned_tokens_step() RETURNS INTEGER AS $$
DECLARE
	count INTEGER := 0;
BEGIN

	-- update old values to look for changes
	UPDATE _tmp_spanned_tokens SET old_left = left_token, old_right = right_token;

	-- compute left and right token for a component
	UPDATE _tmp_spanned_tokens SET
		left_token = (SELECT min(t2.left_token)
		FROM _tmp_spanned_tokens t2
		WHERE t2.pre >= _tmp_spanned_tokens.pre AND t2.pre <= _tmp_spanned_tokens.post),

		right_token = (SELECT max(t2.right_token)
		FROM _tmp_spanned_tokens t2
		WHERE t2.pre >= _tmp_spanned_tokens.pre AND t2.pre <= _tmp_spanned_tokens.post);

	-- link components together
	UPDATE _tmp_spanned_tokens SET
		left_token = (SELECT min(t.left_token)
		FROM _tmp_spanned_tokens t
		WHERE _tmp_spanned_tokens.id = t.id),

		right_token = (SELECT max(t.right_token)
		FROM _tmp_spanned_tokens t
		WHERE _tmp_spanned_tokens.id = t.id);
	
	-- count nodes that still need update
	PERFORM * FROM _tmp_spanned_tokens
		WHERE old_left IS NULL OR old_left != left_token OR old_right IS NULL OR old_right != right_token;
	GET DIAGNOSTICS count = ROW_COUNT;

	RETURN count;
END;
$$ language plpgsql;
