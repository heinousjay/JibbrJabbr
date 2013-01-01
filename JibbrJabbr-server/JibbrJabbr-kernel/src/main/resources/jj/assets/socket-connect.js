

jQuery(function($) {
	
	// the following is taken from:
	// https://github.com/joewalnes/reconnecting-websocket/
	// and redone to suit my needs :D
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
	        if (debug) {
	            console.debug('ReconnectingWebSocket', 'attempt-connect', url);
	        }
	        
	        var localWs = ws;
	        var timeout = setTimeout(function() {
	            if (debug) {
	                console.debug('ReconnectingWebSocket', 'connection-timeout', url);
	            }
	            timedOut = true;
	            localWs.close();
	            timedOut = false;
	        }, self.timeoutInterval);
	        
	        ws.onopen = function(event) {
	            clearTimeout(timeout);
	            if (debug) {
	                console.debug('ReconnectingWebSocket', 'onopen', url);
	            }
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
	                    if (debug) {
	                        console.debug('ReconnectingWebSocket', 'onclose', url);
	                    }
	                    self.onclose(event);
	                }
	                setTimeout(function() {
	                    connect(true);
	                }, self.reconnectInterval);
	            }
	        };
	        ws.onmessage = function(event) {
	            if (debug) {
	                console.debug('ReconnectingWebSocket', 'onmessage', url, event.data);
	            }
	         self.onmessage(event);
	        };
	        ws.onerror = function(event) {
	            if (debug) {
	                console.debug('ReconnectingWebSocket', 'onerror', url, event);
	            }
	            self.onerror(event);
	        };
	    }
	    connect(url);

	    this.send = function(data) {
	        if (ws) {
	            if (debug) {
	                console.debug('ReconnectingWebSocket', 'send', url, data);
	            }
	            return ws.send(data);
	        } else {
	            if (debug) {
	                console.debug('ReconnectingWebSocket', 'buffer', url, data);
	            }
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
	
	
	if (('WebSocket' in window) &&
		('localStorage' in window) &&
		('console' in window)) 
	{
		// our api goes here!
		window.$j = {};
		
		var me = $('#jj-connector-script');
		
		var debug = true; //me.data('jj-debug') === true;
		me.removeAttr('data-jj-debug');
		
		$j.debug = function(on) {
			debug = on;
		}
		
		// from a configuration
		var heartbeatTimeout = 5000;
		var heartbeatId;
		
		var idSeq = 0;
		var creationHoldingPen = {};
		
		var host = me.data('jj-socket-url');
		me.removeAttr('data-jj-socket-url');
		
	
		var ws = new ReconnectingWebSocket(host);
		ws.onopen = function() {
			
			$(window).trigger($.Event('socketopen'));
			heartbeatId = setTimeout(null, heartbeatTimeout);
		}
		
		ws.onclose = function() {
			$(window).trigger($.Event('socketclose'));
		}
		
		ws.onerror = function(error) {
			console.error(error);
		}
		ws.onmessage = function(msg) {
			if (!processRaw(msg)) {
				try {
					processMessages(JSON.parse(msg.data));
				} catch (e) {
					console.warn('received messages that cannot be parsed');
					console.warn(e);
				}
			}
		}
		
		var rawMessages = {
			'jj-hi': function() {
				if (debug) console.debug('server said hi');
				ws.send('jj-yo');
				return true;
			},
			'jj-yo': function() {
				if (debug) console.debug('server said yo');
				return true;
			}
			'jj-reload': function() {
				if (debug) console.debug('server said reload');
				window.location.href = window.location.href;
				return true;
			}
		}
		
		function processRaw(message) {
			return (message in rawMessages) && rawMessages[message]();
		}

		function send(payload) {
			var message = JSON.stringify(payload);
			ws.send(message);
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
		
		var messageProcessors = {
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
		alert("this isn't going to work for you.  sorry.");
	}
	
});
