function handler() {
	if(this.status == 200 && this.response != null) {
		processData(jQuery.parseJSON(this.response));
	}
}

function loadSingleRepo(id, user) {
	$("#repositories-page").load("single-repository.html");
	document.getElementById("repo_id").innerHTML = id;
	document.getElementById("selected_user").innerHTML = "null";
}

function processData(data) {
	var size = Object.keys(data).length;
	if (size > 0) {
		for (var index = 0; index < size; index++) {
			$('#repositories-table tr:last')
				.after('<tr ' + addTagsForTR("repo_" + index, "loadSingleRepo(" + index + ")") + '><td>' + 
					(index + 1) + 
					'</td><td>' + 
					data[index].WSA_REPOSITORY_NAME +
					'</td><td>' + 
					data[index].WSA_JSON_ACTIVE_BRANCH +
					'</td><td>' + 
					data[index].WSA_JSON_NUM_OF_BRANCHES +
					'</td><td>' + 
					data[index].WSA_JSON_LAST_COMMIT_DATA +
					'</td><td>' + 
					data[index].WSA_JSON_LAST_COMMIT_COMMANT +
					'</td></tr>');
		}
	} else {
		var string = "There is no repository for this user";
		$('#repositories-table tr:last')
				.after('<tr><td colspan="6" class="text-center text-muted">' + string.toUpperCase() + '</td></tr>');
	}
}

$(function() {
	var repositoriesLoader = new XMLHttpRequest();
	repositoriesLoader.onload = handler;
	repositoriesLoader.open("GET", "repositories", true);
	repositoriesLoader.setRequestHeader("Content-Type", "text/plain;charset=UTF-8");
	repositoriesLoader.send();
});