var support = require('jj/configuration-support')('jj.resource.ResourceConfiguration');
var ResourceSettings = Packages.jj.resource.ResourceSettings;
var name = 'fileTypeSettings';
var mediaTypes = ['application','audio','example','image','message','model','multipart','text','video'];

function validateMimeType(mimeType) {
	var slash = mimeType.indexOf('/');
	if (slash == -1) {
		return support.accumulateError(name, "mimeType must be a standard-compliant mime type");
	}
	var type = mimeType.substring(0, slash);
	
	if (mediaTypes.indexOf(type) == -1) {
		return support.accumulateError(name, type + " is not a valid media type");
	}
	
	// var subType = mimeType.substring(slash + 1);
	
	// some sort of check here? there seems to be an allowed
	// character set, if i can find it i will validate it
}

function makeIs(key) {
	return function(props) {
		
		if (typeof props !== 'object') {
			support.accumulateError(name, "is requires an object argument");
		} else {
		
			// require mime! require it be a string! require it
			// have one of mediaTypes followed by / followed by something.
			var error = false;
			var mime = props.mimeType;
			if (typeof mime !== 'string') {
				error = support.accumulateError(name, "mimeType must be present and must be a string");
			} else {
				error = validateMimeType(mime);
			}
			
			if (typeof props.charset !== 'undefined' && typeof props.charset !== 'string') {
				error = support.accumulateError(name, "charset must be a string if present");
			}
			
			// charset can be null, but if not null and we get null back, error!
			var charset = ResourceSettings.makeCharset(props.charset);
			
			if (charset === null && props.charset) {
				error = support.accumulateError(name, props.charset + " is not a recognized charset");
			}
			
			if (typeof props.compressible != 'undefined' && typeof props.compressible !== 'boolean') {
				error = support.accumulateError(name, "compressible must be a boolean if present");
			}
			
			if (!error) {
				var settings = new ResourceSettings(mime, charset, props.compressible || null);
				
				support.addToMap(name, key, settings);
			}
		}
		
		return module.exports;
	};
}

var fakeIs = { is: function() { return module.exports; } }

module.exports = {
		
	extension: function(ext) {
		// ext must be present, must be string, must be /^[A-Z0-9_-]+$/i
		if (typeof ext !== 'string') {
			support.accumulateError(name, 'extension must be a string');
		} else if (!/^[A-Z0-9_-]+$/i.test(ext)) {
			support.accumulateError(name, 'extensions should only be letters, numbers, underscores, and dashes');
		} else {
			return {
				is: makeIs(ext)
			};
		}
		return fakeIs;
	}
};
