var $$print = (function(){
	var context = inject('jj.repl.CurrentReplChannelHandlerContext');
	return function(arg) {
		try {
			context.current().writeAndFlush(java.lang.String.valueOf(arg()));
		} catch (e) {
			context.current().writeAndFlush(java.lang.String.valueOf(e));
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