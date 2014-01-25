
(function($){
	$.fn.serializeObject = function() {
		var arrayData = this.serializeArray(),
			objectData = {};

		$.each(arrayData, function() {
			var value;

			if (this.value != null) {
				value = this.value;
			} else {
				value = '';
			}

			if (objectData[this.name] != null) {
				if (!objectData[this.name].push) {
					objectData[this.name] = [objectData[this.name]];
				}
				objectData[this.name].push(value);
			} else {
				objectData[this.name] = value;
			}
		});

		return objectData;
	};
})(jQuery);


jQuery(function($) {
	
	// the following is taken from:
	// https://github.com/joewalnes/reconnecting-websocket/
	// and redone to suit my needs :D
	// it's mainly here as a guidepost, in fact
	// 
	// MIT License:
	//
	// Copyright (c) 2010-2012, Joe Walnes
	//
	// Permission is hereby granted, free of charge, to any person obtaining a copy
	// of this software and associated documentation files (the "Software"), to deal
	// in the Software without restriction, including without limitation the rights
	// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	// copies of the Software, and to permit persons to whom the Software is
	// furnished to do so, subject to the following conditions:
	//
	// The above copyright notice and this permission notice shall be included in
	// all copies or substantial portions of the Software.
	//
	// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	// THE SOFTWARE.

	function ReconnectingWebSocket(url, protocols) {

	    // These can be altered by calling code.
	    this.reconnectInterval = 1000;
	    this.timeoutInterval = 2000;

	    var self = this;
	    var ws;
	    var forcedClose = false;
	    var timedOut = false;
	    
	    var messageBuffer = [];
	    
	    this.url = url;
	    this.protocols = protocols;
	    this.readyState = WebSocket.CONNECTING;
	    this.URL = url; // Public API

	    this.onopen = function(event) {
	    };

	    this.onclose = function(event) {
	    };

	    this.onmessage = function(event) {
	    };

	    this.onerror = function(event) {
	    };

	    function connect(reconnectAttempt) {
	        ws = new WebSocket(url, protocols);
	        debug && console.debug('ReconnectingWebSocket', 'attempt-connect', url);
	        
	        var localWs = ws;
	        var timeout = setTimeout(function() {
	            debug && console.debug('ReconnectingWebSocket', 'connection-timeout', url);
	            timedOut = true;
	            localWs.close();
	            timedOut = false;
	        }, self.timeoutInterval);
	        
	        ws.onopen = function(event) {
	            clearTimeout(timeout);
	            debug && console.debug('ReconnectingWebSocket', 'onopen', url);
	            self.readyState = WebSocket.OPEN;
	            reconnectAttempt = false;
	            
	            messageBuffer.forEach(ws.send);
	            messageBuffer.length = 0;
	            
	            self.onopen(event);
	        };
	        
	        ws.onclose = function(event) {
	            clearTimeout(timeout);
	            ws = null;
	            // for now, let's not retry.  it's weird
	            if (forcedClose || true) {
	                self.readyState = WebSocket.CLOSED;
	                self.onclose(event);
	            } else {
	                self.readyState = WebSocket.CONNECTING;
	                if (!reconnectAttempt && !timedOut) {
	                	debug && console.debug('ReconnectingWebSocket', 'onclose', url);
	                    self.onclose(event);
	                }
	                setTimeout(function() {
	                    connect(true);
	                }, self.reconnectInterval);
	            }
	        };
	        ws.onmessage = function(event) {
	            debug && console.debug('ReconnectingWebSocket', 'onmessage', url, event.data);
	            self.onmessage(event);
	        };
	        ws.onerror = function(event) {
	            debug && console.debug('ReconnectingWebSocket', 'onerror', url, event);
	            self.onerror(event);
	        };
	    }
	    connect(url);

	    this.send = function(data) {
	        if (ws) {
	        	debug && console.debug('ReconnectingWebSocket', 'send', url, data);
	            return ws.send(data);
	        } else {
	        	debug && console.debug('ReconnectingWebSocket', 'buffer', url, data);
	            messageBuffer.push(data);
	        }
	    };

	    this.close = function() {
	        if (ws) {
	            forcedClose = true;
	            ws.close();
	        }
	    };

	    this.refresh = function() {
	        if (ws) {
	            ws.close();
	        }
	    };
	}
	
	var debug = false; //me.data('jj-debug') === true;
	
	if (('WebSocket' in window) &&
		('localStorage' in window) &&
		('JSON' in window)
	) {

		// our api goes here!
		window.$j = {};
		
		var me = $('#jj-connector-script');
		me.removeAttr('data-jj-debug');
		
		$j.debug = function(on) {
			debug = !!on;
			return "debug is " + (on ? "on" : "off");
		}
		
		var heartbeat = (function() {
			var PongReceived = 0,
				WaitingForPong = 1,
				ConnectionClosed = 2,
				state,
				ponged,
				heartbeatTimeout = 30000,
				heartbeatId;
				
			
			var result = function() {

				state = PongReceived;
				ponged = true;
				clearTimeout(heartbeatId);
				heartbeatId = null;
				
				function doHeartbeat() {
					switch(state) {
					case PongReceived:
						debug && console.debug("ping");
						ws.send('jj-hi');
						state = WaitingForPong;
						ponged = false;
						heartbeatId = setTimeout(doHeartbeat, 500);
						break;
					case WaitingForPong:
						if (ponged) {
							debug && console.debug("pong");
							ponged = false;
							state = PongReceived;
							heartbeatId = setTimeout(doHeartbeat, heartbeatTimeout);
						} else {
							debug && console.debug("no pong from server.  uh oh");
						}
						break;
					}
				};
				heartbeatId = setTimeout(doHeartbeat, heartbeatTimeout);
			}
			
			result.pong = function() {
				ponged = true;
			}
			
			result.closed = function() {
				state = ConnectionClosed;
				clearTimeout(heartbeatId);
			}
			
			return result;
		})();
		
		var idSeq = 0;
		var creationHoldingPen = {};
		
		var host = me.data('jj-socket-url');
		me.removeAttr('data-jj-socket-url');
		
	
		var ws = new WebSocket(host);
		ws.onopen = function() {
			
			debug && console.debug("WebSocket open", host);
			
			heartbeat();
			$(window).trigger($.Event('socketopen'));
		}
		
		ws.onclose = function() {
			
			debug && console.debug("WebSocket closed", host);
			
			heartbeat.closed();
			$(window).trigger($.Event('socketclose'));
		}
		
		ws.onerror = function(error) {
			debug && console.error("WebSocket errored", error);
		}
		ws.onmessage = function(msg) {
			debug && console.debug("WebSocket onmessage", msg);
			if (!processRaw(msg.data)) {
				try {
					processMessages(JSON.parse(msg.data));
				} catch (e) {
					debug && console.warn('received messages that cannot be parsed');
					debug && console.warn(e);
				}
			}
		}

		var reader;
		$j.sendFile = function(file) {
			(reader || (function() {
				reader = new FileReader();
				reader.onload = function(e) {
					ws.send(e.target.result);
				}
				return reader;
			})()).readAsArrayBuffer(file);
		}
		
		var rawMessages = {
			// shutdown message
			'jj-bye': function() {
				debug && console.debug('server said bye');
				return true;
			},
			// pong for the pinger
			'jj-yo': function() {
				heartbeat.pong();
				return true;
			},
			// we are out of date and need to reload
			'jj-reload': function() {
				debug && console.debug('server said reload');
				window.location.href = window.location.href;
				return true;
			}
		}
		
		function processRaw(message) {
			return rawMessages[message] && rawMessages[message]();
		}

		function send(payload) {
			debug && console.debug("WebSocket", "send", payload);
			ws && ws.send(JSON.stringify(payload));
		}
		
		function processMessages(messages) {
			messages.forEach(function(message) {
				// interestingly, this allows us to form compound messages
				// but that's not likely to ever happen
				Object.keys(message).forEach(function(type) {
					if (type in messageProcessors) {
						messageProcessors[type](message[type]);
					} else {
						debug && console.warn('do not understand how to process a message of type ' + type);
						debug && console.warn(message[type]);
					}
				});
			});
		}
		
		var idify = function(el) {
			var id = 'jj-' + (idSeq++);
			el.attr('id', id);
			return id;
		};
		
		function determineEventConfig(binding) {
			var context = 'context' in binding ? $(binding.context) : $(document);
			var eventName = binding.type;
			var handler = sendEvent;
			var proxy = eventProxies[eventName];
			if (proxy) {
				eventName = proxy.as;
				handler = proxy.prep(binding.selector ? $(binding.selector, context) : context);
			}
			if (handler != null) {
				return {
					context: context,
					name: eventName + '.jj-events', // todo, work the context and selector into this name space
					handler: handler
				};
			}
		}
		
		var messageProcessors = {
			'bind': function(binding) {
				var data = {
					selector: binding.selector || '',
					context: binding.context || ''
				};
				var eventConfig = determineEventConfig(binding);
				if (eventConfig) {
					debug && console.log("binding", eventConfig, binding);
					eventConfig.context.on(eventConfig.name, binding.selector, data, eventConfig.handler);
				}
			},
			'unbind': function(unbinding) {
				var eventConfig = determineEventConfig(unbinding);
				if (eventConfig) {
					debug && console.log("unbinding", eventConfig, unbinding);
					eventConfig.context.off(eventConfig.name, unbinding.selector, eventConfig.handler);
					if (eventConfig.handler.clean) {
						eventConfig.handler.clean();
					}
				}
			},
			'get': function(get) {
				if ('name' in get) {
					result(get.id, _$(get.selector)[get.type](get.name));
				} else {
					result(get.id, _$(get.selector)[get.type]());
				}
			},
			'set': function(set) {
				if ('name' in set) {
					// TODO attr id setting is a special case! 
					// because we may need to update the pen
					_$(set.selector)[set.type](set.name, set.value);
				} else {
					_$(set.selector)[set.type](set.value);
				}
			},
			'create': function(create) {
				var el = $(create.html, create.args);
				var id = el.attr('id') || idify(el);
				var selector = '#' + id;
				creationHoldingPen[selector] = el;
				element(create.id, selector);
			},
			'append': function(append) {
				_$(append.parent).append(_$(append.child));
				delete creationHoldingPen[append.child];
			},
			'store': function(store) {
				localStorage[store.key] = store.value;
			},
			'retrieve': function(retrieve) {
				var value = localStorage[retrieve.key];
				if (value) {
					result(retrieve.id, JSON.parse(value));
				} else {
					result(retrieve.id);
				}
			},
			'call': function(call) {
				var toCall = window[call.name];
				if (toCall) {
					toCall.apply(window, JSON.parse(call.args));
				} else {
					debug && console.warn('asked to call a nonexistent function ' + call.name);
				}
			},
			'invoke': function(invoke) {
				var toInvoke = window[invoke.name];
				if (toInvoke) {
					result(invoke.id, toInvoke.apply(window, JSON.parse(invoke.args)));
				} else {
					debug && console.warn('asked to invoke a nonexistent function ' + invoke.name);
				}
			}
		}
		
		function _$(selector) {
			// use this to see if it's in the holding pen first
			return creationHoldingPen[selector] || $(selector); 
		}
		
		function element(id, selector) {
			if (id) {
				send({
					'element' : {
						'id' : id,
						'selector' : selector
					}
				});
			}
		}
		
		function result(id, result) {
			// only send if we have an id
			if (id) {
				send({
					'result': {
						'id': id,
						'value': JSON.stringify(result)
					}
				});
			}
		}
		
		// for now, this is fairly simple.  need to work out the messaging protocol
		// the rest of the event data will also get packaged along
		var sendEvent = function(event) {
			var target = $(event.target);
			var toSend = {
					'event' : {
						type: event.type,
						selector: event.data.selector,
						context: event.data.context,
						which: event.which,
						target: '#' + (target.attr('id') || idify(target))
					}
				};
			if (event.form) toSend.event.form = event.form;
			send(toSend);
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
				},
				prep: function(element) {
					return this.handler;
				}
			},
			'submit': {
				as: 'submit',
				handler: function(event) {
					event.preventDefault();
					var form = $(this);
					var values = form.serializeObject();
					var clicked = form.data('jj-last-clicked');
					if (clicked) {
						form.removeData('jj-last-clicked');
						var name = clicked.attr('name');
						var val = clicked.val();
						if (typeof name == 'string' && typeof val == 'string') {
							if (name in values) {
								if (!('push' in values[name])) {
									values[name] = [values[name]];
								}
								values[name].push(val);
							} else {
								values[name] = val;
							}
						}
					}
					event.form = JSON.stringify(values);
					sendEvent(event);
				},
				prep: function(element) {
					if (element.length == 1 && element.get(0).submit) {
						var event = function() {
							var el = $(this);
							$(this.form).data('jj-last-clicked', el);
						};
						this.handler.clean = function() {
							$(element).off('.jj-submit-handler');
						}
						$(element).on('click.jj-submit-handler', 'input[type=submit][name],button[type=submit][name]', event);
						return this.handler;
					} else {
						return null;
					}
				}
			}
		};
		
		processMessages(me.data('jj-startup-messages'));
		me.removeAttr('data-jj-startup-messages');
	
	} else {
		alert("this isn't going to work for you.  sorry.");
	}
	
});
