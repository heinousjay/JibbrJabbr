var collector = inject('jj.configuration.ConfigurationCollector');
var base = 'jj.resource.ResourceConfiguration.';

var ResourceSettings = Packages.jj.resource.ResourceSettings;

var mediaTypes = ['application','audio','example','image','message','model','multipart','text','video'];

function validateMimeType(mimeType) {
	var slash = mimeType.indexOf('/');
	if (slash == -1) {
		throw new Error("mimeType must be a standard-compliant mime type");
	}
	var type = mimeType.substring(0, slash);
	
	if (mediaTypes.indexOf(type) == -1) {
		throw new Error(type + " is not a valid media type");
	}
	
	// var subType = mimeType.substring(slash + 1);
	
	// some sort of check here? there seems to be an allowed
	// character set, if i can find it i will validate it
}

function makeIs(key, valueKey) {
	return function(props) {
		
		if (typeof props !== 'object') {
			throw new Error("is requires an object argument");
		}
		
		// require mime! require it be a string! require it
		// have one of mediaTypes followed by / followed by something.

		var mime = props.mimeType;
		if (typeof mime !== 'string') {
			throw new Error("mimeType must be present and must be a string");
		}
		
		validateMimeType(mime);
		
		if (typeof props.charset !== 'undefined' && typeof props.charset !== 'string') {
			throw new Error("charset must be a string if present");
		}
		
		// charset can be null, but if not null and we get null back, error!
		var charset = ResourceSettings.makeCharset(props.charset);
		
		if (charset === null && props.charset) {
			throw new Error(props.charset + " is not a recognized charset");
		}
		
		if (typeof props.compressible != 'undefined' && typeof props.compressible !== 'boolean') {
			throw new Error("compressible must be a boolean if present");
		}
		
		var settings = new ResourceSettings(mime, charset, props.compressible || null);
		
		collector.addConfigurationMappedElement(key, valueKey, settings);
		
		return module.exports;
	};
}

module.exports = {
		
	extension: function(ext) {
		// ext must be present, must be string, must be /^[A-Z0-9_-]+$/i
		if (typeof ext !== 'string') {
			throw new Error("extension must be a string");
		}
		
		if (!/^[A-Z0-9_-]+$/i.test(ext)) {
			throw new Error("extensions should only be letters, numbers, underscores, and dashes");
		}
		
		return {
			is: makeIs(base + 'fileTypeSettings', ext)
		};
	}
};
