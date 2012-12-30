jQuery(function($) {
	
	if (('WebSocket' in window) && ('localStorage' in window)) {
		
		var me = $('#jj-connector-script');
		
		var debug = me.data('jj-debug') === true;
		me.removeAttr('data-jj-debug');
		
		var idSeq = 0;
		var creationHoldingPen = {};
		
		var host = me.data('jj-socket-url');
		me.removeAttr('data-jj-socket-url');
		
		var connected = false;
		var ws = new WebSocket(host);
		ws.onopen = function() {
			connected = true;
			$(window).trigger($.Event('socketopen'));
		}
		ws.onclose = function() {
			connected = false;
			if (debug) console.log('server closed the connection.  need a reconnect algorithm here');
			$(window).trigger($.Event('socketclose'));
			// need a reconnect
		}
		ws.onerror = function(error) {
			// try to send the error?
		}
		ws.onmessage = function(msg) {
			if (debug) console.log('received ' + msg.data);
			try {
				processMessages(JSON.parse(msg.data));
			} catch (e) {
				console.warn('received messages that cannot be parsed');
				console.warn(e);
			}
		}
		
		function processMessages(messages) {
			messages.forEach(function(message) {
				// interestingly, this allows us to form compound messages
				// but that's not likely to ever happen
				Object.keys(message).forEach(function(type) {
					if (type in messageProcessors) {
						messageProcessors[type](message[type]);
					} else {
						console.warn('do not understand how to process a message of type ' + type);
						console.warn(message[type]);
					}
				});
			});
		}
		
		var commands = {
			reload: function() {
				// should perhaps present something nice?
				window.location.href = window.location.href;
			}
		}
		
		var messageProcessors = {
			'command': function(command) {
				if (command in commands) commands[command]();
			},
			'bind': function(binding) {
				var context = 'context' in binding ? $(binding.context) : $(document);
				var eventName = binding.type;
				var handler = sendEvent;
				var proxy = eventProxies[eventName];
				if (proxy) {
					eventName = proxy.as;
					handler = proxy.handler;
				}
				// we pass our selector in as data
				context.on(eventName + '.jj', binding.selector, binding.selector, handler);
			},
			'get': function(get) {
				result(get.id, _$(get.selector)[get.type]());
			},
			'set': function(set) {
				// TODO attr id setting is a special case! 
				// may need to update the pen
				// may become its own message actually
				_$(set.selector)[set.type](set.value);
			},
			'create': function(create) {
				var el = $(create.html, create.args);
				var selector = '#' + (el.attr('id') || (function() {
					var id = 'jj-' + (idSeq++);
					el.attr('id', id);
					return id;
				})());
				creationHoldingPen[selector] = el;
				result(create.id, selector);
			},
			'append': function(append) {
				_$(append.parent).append(_$(append.child));
				delete creationHoldingPen[append.child];
			},
			'store': function(store) {
				localStorage[store.key] = store.value;
			},
			'retrieve': function(retrieve) {
				result(retrieve.id, localStorage[retrieve.key]);
			},
			'call': function(call) {
				var toCall = window[call.name];
				if (toCall) {
					toCall.apply(window, JSON.parse(call.args));
				} else {
					console.warn('asked to call a nonexistent function ' + call.name);
				}
			},
			'invoke': function(invoke) {
				var toInvoke = window[invoke.name];
				if (toInvoke) {
					result(invoke.id, JSON.stringify(toInvoke.apply(window, JSON.parse(invoke.args))));
				} else {
					console.warn('asked to invoke a nonexistent function ' + invoke.name);
				}
			}
		}
		
		function _$(selector) {
			// use this to see if it's in the holding pen first
			return creationHoldingPen[selector] || $(selector); 
		}
		
		function result(id, result) {
			// only send if we have an id
			if (id) {
				// clearly this can be cleaner although tags will help
				send({
					'result' : {
						'id': id,
						'value' : result
					}
				});
			}
		}
		
		function send(payload) {
			if (connected) {
				var message = JSON.stringify(payload);
				if (debug) console.log('sending ' + message);
				ws.send(message);
			}
		}
		
		// for now, this is fairly simple.  need to work out the messaging protocol
		// the rest of the event data will also get packaged along
		var sendEvent = function(event) {
			send({
				'event' : {
					type: event.type,
					selector: event.data,
					which: event.which
				}
			});
		}
		
		// specialized event handlers to handle common cases, throttle and debounce, and so on 
		var eventProxies = {
			'enter': { 
				as: 'keydown', 
				handler: function(event) {
					if (event.which == 13) { // incorporate jquery ui? maybe, maybe
						event.type = 'enter';
						sendEvent(event);
					}
				}
			}
		};
		
		processMessages(me.data('jj-startup-messages'));
		me.removeAttr('data-jj-startup-messages');
	
	} else {
		
	}
	
});
