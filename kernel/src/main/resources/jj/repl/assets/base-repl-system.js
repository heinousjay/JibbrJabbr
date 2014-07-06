var $$print = (function(){
	var context = inject('jj.repl.CurrentReplChannelHandlerContext');
	return function(arg) {
		try {
			context.current().writeAndFlush(java.lang.String.valueOf(arg()));
			// this is intentionally not in a finally because the continuation
			// causes finally blocks to execute.  whoops
			context.current().writeAndFlush('\n>');
		} catch (e) {
			context.current().writeAndFlush(java.lang.String.valueOf(e));
			if ('stack' in e) {
				context.current().writeAndFlush('\n' + e.stack);
			}
			// this is intentionally not in a finally because the continuation
			// causes finally blocks to execute even though it's too early
			context.current().writeAndFlush('\n>');
		}
	}
})();

var quit = (function() {
	var context = inject('jj.repl.CurrentReplChannelHandlerContext');
	return function() {
		context.current().writeAndFlush("goodbye!\n").addListener(Packages.io.netty.channel.ChannelFutureListener.CLOSE);
	}
})();

var objectKeys = function(object) {
	return JSON.stringify(Object.keys(object));
}