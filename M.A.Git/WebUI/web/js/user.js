function loadSingleRepo(id, user) {
	$("#users-div").load("single-repository.html");
	document.getElementById("repo_id").innerHTML = id;
	document.getElementById("selected_user").innerHTML = user;
}

function loadUserData(data, user_selected) {
	var json = jQuery.parseJSON(data);
	console.log(json);
	var size = Object.keys(json).length;
	for (var index = 0; index < size; index++) {
		$('#user-repositories-table tr:last')
		.after(	'<tr><td>' + 
				(index + 1) + 
				'</td><td style="cursor: pointer;" onclick=\'loadSingleRepo("' + index + '","' + user_selected + '")\' ><a href="#">' + 
				json[index].WSA_REPOSITORY_NAME +
				'</a></td><td><i class="fa fa-share-alt-square"></i> <a href="#">FORK</a></td></tr>');
	}
}

$(function() { // onload...do
	var getUrlParameter = function getUrlParameter(sParam) {
		var sPageURL = window.location.search.substring(1),
			sURLVariables = sPageURL.split('&'),
			sParameterName,
			i;

		for (i = 0; i < sURLVariables.length; i++) {
			sParameterName = sURLVariables[i].split('=');

			if (sParameterName[0] === sParam) {
				return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
			}
		}
	};
	
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