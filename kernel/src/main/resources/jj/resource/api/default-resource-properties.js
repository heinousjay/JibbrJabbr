// provides a default set of resource configurations
// if no configuration is specified, this should be loaded
// anyway! but how...

function extend(base, dest) {
  Object.keys(base).forEach(function(key) {
    dest[key] = base[key];
  });
  return dest;
}

var rp = require('jj/resource-properties');
var compressible = {
  compressible: true, // indicates that the type can be compressed
};
function compressedMime(type) {
  return extend(compressible, {
    mimeType: type
  });
}
var UTF_8 = 'UTF-8';
var text = extend(compressible, {
  charset: UTF_8 // the character set, indicates the type is text
});
function textMime(type) {
  return extend(text, {
    mimeType: type
  });
}
var html = textMime('text/html');

rp.extension('html').is(html)
  .extension('htm') .is(html)
  .extension('js')  .is(textMime('application/javascript'))
  .extension('css') .is(textMime('text/css'))
  .extension('less').is(textMime('text/css'))
  .extension('txt') .is(textMime('text/plain'))
  .extension('csv') .is(textMime('text/csv'))
  .extension('xml') .is(compressedMime('application/xml'))  // charset is inside
  .extension('json').is(compressedMime('application/json')) // always utf-8
  .extension('pdf') .is(compressedMime('application/pdf'))
  .extension('zip') .is({mimeType: 'application/zip'})
  .extension('tar') .is({mimeType: 'application/x-tar'})
  .extension('gzip').is({mimeType: 'application/x-gzip'})
  .extension('gz')  .is({mimeType: 'application/x-gzip'})
  .extension('bmp') .is({mimeType: 'image/bmp'})
  .extension('gif') .is({mimeType: 'image/gif'})
  .extension('jpe') .is({mimeType: 'image/jpeg'})
  .extension('jpg') .is({mimeType: 'image/jpeg'})
  .extension('jpeg').is({mimeType: 'image/jpeg'})
  .extension('png') .is({mimeType: 'image/png'})
  .extension('tiff').is({mimeType: 'image/tiff'})
  .extension('tif') .is({mimeType: 'image/tiff'})
  .extension('svg') .is(compressedMime('image/svg+xml')) // xml, so carries charset inside
  .extension('ico') .is({mimeType: 'image/vnd.microsoft.icon'})
  .extension('eot') .is(compressedMime('application/vnd.ms-fontobject'))
  .extension('otf') .is(compressedMime('application/font-sfnt'))
  .extension('ttf') .is(compressedMime('application/font-sfnt'))
  .extension('woff').is({mimeType: 'application/font-woff'})
  .extension('aac') .is({mimeType: 'audio/aac'})
  .extension('m4a') .is({mimeType: 'audio/mp4'})
  .extension('mp1') .is({mimeType: 'audio/mpeg'})
  .extension('mp2') .is({mimeType: 'audio/mpeg'})
  .extension('mp3') .is({mimeType: 'audio/mpeg'})
  .extension('oga') .is({mimeType: 'audio/ogg'})
  .extension('ogg') .is({mimeType: 'audio/ogg'})
  .extension('wav') .is({mimeType: 'audio/wav'})
  .extension('mp4') .is({mimeType: 'video/mp4'})
  .extension('ogv') .is({mimeType: 'video/ogg'})
  .extension('webm').is({mimeType: 'video/webm'})
  
  // there are some weird ones, that probably won't get served, but
  // are configured by default with charsets
  .extension('properties').is(textMime('text/plain'))
  .extension('coffee')    .is(textMime('text/coffeescript'));

// re-export the underlying config for convenience
module.exports = rp;
