var $$print = (function(){
	var context = inject('jj.repl.CurrentReplChannelHandlerContext');
	return function(arg) {
		try {
			context.current().writeAndFlush(java.lang.String.valueOf(arg()));
		} catch (e) {
			context.current().writeAndFlush(java.lang.String.valueOf(e));
			if ('stack' in e) {
				context.current().writeAndFlush('\n' + e.stack);
			}
		} finally {
			context.current().writeAndFlush('\n>');
		}
	}
})();

var quit = (function() {
	var context = inject('jj.repl.CurrentReplChannelHandlerContext');
	return function() {
		context.current().close();
	}
})();

var objectKeys = function(object) {
	return JSON.stringify(Object.keys(object));
}