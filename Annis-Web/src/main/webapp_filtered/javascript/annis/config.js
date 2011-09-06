// the context path used by this installation
var conf_context='${annis.webcontext}';
var conf_revision='Annis ${project.version} ${annis.versionExtra}';

Ext.BLANK_IMAGE_URL='images/s.gif';
Ext.SSL_SECURE_URL='https://korpling.german.hu-berlin.de' + conf_context + '/empty.html';

// defacto do never a timeout (10 minutes)
var global_timeout=600000;

var search_context = [[0], [1], [2], [5], [10]];
var search_context_default = 5;
