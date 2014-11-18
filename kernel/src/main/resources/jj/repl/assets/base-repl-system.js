
let [$$print, quit, objectKeys] = (function() {
	var context = inject('jj.repl.CurrentReplChannelHandlerContext');
	var showPrompt = true;
	function prompt() {
		showPrompt && context.current().writeAndFlush('\n>');
	}
	return [
		function $$print(arg) {
			try {
				context.current().writeAndFlush(java.lang.String.valueOf(arg()));
			} catch (e) {
				context.current().writeAndFlush(java.lang.String.valueOf(e));
				if ('stack' in e) {
					context.current().writeAndFlush('\n' + e.stack);
				}
			}
			// this is intentionally not in a finally because any continuation
			// causes finally blocks to execute.  whoops. apparently finally is
			// implemented using java finally.  shouldn't matter, though
			prompt();
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
