
let [$$print, quit, objectKeys] = (function() {
	var context = inject('jj.repl.CurrentReplChannelHandlerContext');
	var showPrompt = true;
	return [
		function $$print(arg) {
			try {
				context.current().writeAndFlush(java.lang.String.valueOf(arg()));
				// this is intentionally not in a finally because the continuation
				// causes finally blocks to execute.  whoops
				showPrompt && context.current().writeAndFlush('\n>');
			} catch (e) {
				context.current().writeAndFlush(java.lang.String.valueOf(e));
				if ('stack' in e) {
					context.current().writeAndFlush('\n' + e.stack);
				}
				// this is intentionally not in a finally because the continuation
				// causes finally blocks to execute even though it's too early
				showPrompt && context.current().writeAndFlush('\n>');
			}
			showPrompt = true;
		},
		function quit() {
			showPrompt = false;
			context.current().writeAndFlush("goodbye!\n").addListener(Packages.io.netty.channel.ChannelFutureListener.CLOSE);
			return '';
		},
		function objectKeys(object) {
			return JSON.stringify(Object.keys(object));
		}
	];
})();
