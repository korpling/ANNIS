from pygments.lexer import RegexLexer
from pygments.token import *

__all__ = ['AqlLexer']

class AqlLexer(RegexLexer):
    name = 'AQL'
    aliases = ['aql']
    filenames = ['*.txt', '*.aql']

    tokens = {
        'root': [
            (r'"', String, 'string'),
            (r'/', String.Regex, 'regex'),
            (r'/\*', String.Comment, 'comment'),
            (r'&', Operator),
            (r'([a-zA-Z_%]([a-zA-Z0-9_\-%])*:)?([a-zA-Z_\%]([a-zA-Z0-9_\-%])*)', Name.Variable),
            (r'(==)|(_=_)|(_i_)|(_o_)|(_l_)|(_r_)|(_ol_)|(_or_)', Operator),
            (r'>([a-zA-Z_%][a-zA-Z0-9_\-%]*)?(@(l|r)?)', Operator),
            (r'((\.)|(\^)|(->)|(>))([a-zA-Z_%][a-zA-Z0-9_\-%]*)?(\s*(\*)|([ \t,0-9]+))?', Operator),
            (r'(==)|(_=_)|(_i_)|(_o_)|(_l_)|(_r_)|(_ol_)|(_or_)', Operator),
            (r'#[0-9a-zA-Z]+', Name.Label),
            (r'.', Text)
            
        ],
        'string': [
			(r'"', String, 'root'),
			(r'.', String)
        ], 
        'regex': [
			(r'/', String.Regex, 'root'),
			(r'.', String.Regex)
        ],
        'comment': [
			(r'\*/', String.Comment, 'root'),
			(r'.', String.Comment)
        ]
    }
