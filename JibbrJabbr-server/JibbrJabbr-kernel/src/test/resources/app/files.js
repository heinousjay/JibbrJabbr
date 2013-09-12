$(function() {

	window.URL = window.URL || window.webkitURL;

	var fileSelect = $("#fileSelect"), 
		fileElem = $("#fileElem"), 
		fileList = $("#fileList");

	fileSelect.click(function(e) {
		if (fileElem) {
			fileElem.click();
		}
		e.preventDefault(); // prevent navigation to "#"
	});

	fileElem.change(function() {
		var files = this.files;
		if (!files.length) {
			fileList.html("<p>No files selected!</p>");
		} else {
			var list = $("<ul>");
			for ( var i = 0; i < files.length; ++i) {
				var li = $("<li>");
				list.append(li);

				var img = 
					$("<img>")
					.attr({
						src: window.URL.createObjectURL(files[i]),
						height: 60
					})
					.on('load', function() {
						window.URL.revokeObjectURL(this.src);
					});
				li.append(img);

				var info = $("<span>");
				info.html(files[i].name + ": " + files[i].size + " bytes, " + files[i].type);
				li.append(info);
				
				$j.sendFile(files[i]);
			}
			fileList.append(list);
		}
	});
});

function what() {
	alert('what?');
}