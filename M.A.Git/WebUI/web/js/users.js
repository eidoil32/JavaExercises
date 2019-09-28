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
	if (user_selected == null) {
		get_users();
	} else {
		get_user_repositories(user_selected);
	}
})

function get_users() {
		$.ajax({
			data: {},
			url: "users",
			timeout: 2000,
			error: function() {
				console.error("Error from server!");
			},
			success: get_users_details
		});
};

function get_user_repositories(user_name) {
	$("#users-container").html("Hello World");
};

function get_users_details(data) {
        var json = data;
		var index = 1;
		$.each(data, function(key, value){
			$('#user-table tr:last')
				.after(	'<tr><td>' + 
						index + 
						'</td><td><a href="?user=' + value +'">' + 
						value +
						'</a></td></tr>');
			index++;
		});
};