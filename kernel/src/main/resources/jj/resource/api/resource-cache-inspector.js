// fairly thin layer? i guess this does it, really
// just expose a function that, when called, returns
// the nodes and links of the resource cache to the caller

module.exports = function() {

	// ResourceCacheInspector directly outputs the javascript objects
	// because it is a sweetheart
	var rci = inject('jj.resource.ResourceCacheInspector');
	
	return {
		nodes: rci.nodes(),
		links: rci.links(),
		types: rci.types(),
		bases: rci.bases()
	}
}