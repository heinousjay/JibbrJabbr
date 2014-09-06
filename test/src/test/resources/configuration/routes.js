// this is a silly helper, it just copies the exports
// of the given script into the given object
var globalize = require('jj/globalize');

// so bam.  life is easy.
globalize('jj/uri-routing-configuration', this);

welcomeFile('root');

//for example!
route.GET('/chat/').to.document('/chat/list');

var chatRoom = '/chat/room';

route.POST('/chat/:room').to.document(chatRoom);
route.PUT('/chat/:room/*secret').to.document(chatRoom);
route.DELETE('/chat/:room/*secret').to.document(chatRoom);

/*
// but this! is what i'm aiming at
route.GET('/chat/').to(document('/chat/list'));

var chatRoom = document('/chat/room');

route.POST('/chat/:room').to(chatRoom);
route.PUT('/chat/:room/*secret').to(chatRoom);
route.DELETE('/chat/:room/*secret').to(chatRoom);
*/

