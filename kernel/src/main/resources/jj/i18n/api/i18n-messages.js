var sm = inject('jj.i18n.ScriptMessages');


module.exports = function(name, locale) {
	// okay here's the rule - give it nothing and you
	// get the name of the current script, and whatever
	// locale gets resolved out of the current user?
	// need a current user.  SHE-IT. so for now, it's
	// either the configured or system default locale
	
	return sm.getMessagesResource(name, locale);
}