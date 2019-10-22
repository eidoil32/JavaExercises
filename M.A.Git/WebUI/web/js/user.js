function loadSingleRepo(id, user) {
	$("#users-div").load("single-repository.html");
	document.getElementById("repo_id").innerHTML = id;
	document.getElementById("selected_user").innerHTML = user;
}

function forkRepository(id, user) {
	$.ajax({
		async: true, 
		data: {
			"user_id" : user,
			"repository_id": id
			},
		url: "fork_repo",
		timeout: 2000,
		error: function(data) {
			magitShowError(data.responseText)
		},
		success: function(data) {
			magitShowSuccess("Forking repository finish successfully!");
		}
	});	
}

function loadUserData(data, user_selected) {
	var json = jQuery.parseJSON(data);
	var size = Object.keys(json).length;
	for (var index = 0; index < size; index++) {
		$('#user-repositories-table tr:last')
		.after(	'<tr onmouseover="elementHover(this)" onmouseout="elementOut(this)"><td>' + 
				(index + 1) + 
				'</td><td style="cursor: pointer;" onclick=\'loadSingleRepo("' + index + '","' + user_selected + '")\' ><a href="#">' + 
				json[index].WSA_REPOSITORY_NAME +
				'</a></td><td onclick="forkRepository(' + index + ',\'' + user_selected + '\')"><i class="fa fa-share-alt-square"></i> <a href="#">FORK</a></td></tr>');
	}
}

$(function() { // onload...do
	var user_selected = getUrlParameter('user');
	if (user_selected != null) {
		$.ajax({
			type: 'POST',
			async: true, 
			data: {"username":user_selected},
			url: "single_user",
			timeout: 2000,
			error: function() {
				console.error("Error from server!");
			},
			success: function(data) {
				loadUserData(data, user_selected)
			}
		});
	}
})