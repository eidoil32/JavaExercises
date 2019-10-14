
function updateBranches(branchesList) {
	var size = Object.keys(branchesList).length;
	for (var index = 0; index < size; index++) {
	$('#branches-table tr:last')
		.after('<tr><td>' + 
			(index + 1) + 
			'</td><td>' + 
			branchesList[index] +
			'</td></tr>');
	}
}

function addToElement(element, text) {
	element.innerHTML += text;
}

function addChangeBranchForm(branches, headBranch, location) {
	var myForm =	'<form method="POST" action="changeHeadBranch" class="form-inline"><b>Checkout to other branch:</b></br>' +
						'<input type="hidden" id="location" value="'+ location +'">' +
						'<div class="form-group mb-2" style="margin-right: 5px;">' +
							'<select class="form-control form-control-sm" id="branchSelector">';
	for (var index = 0; index < Object.keys(branches).length; index++) {
		if (branches[index] !== headBranch.toString())
			myForm += 				'<option>' + branches[index] + '</option>';
	}				
	myForm 	+=				'</select>' +
						'</div>'+
						'<button type="submit" class="btn btn-primary mb-2">CHECKOUT</button>' + 
					'</form>';

	
	$(myForm).appendTo('#repository_details');
}

function fetchRepositoryData(data) {
	var json = jQuery.parseJSON(data);
	var repo_details = document.getElementById("repository_details");

	addToElement(repo_details,"<b>Head branch:</b> " + json.WSA_SINGLE_REPOSITORY_HEAD_BRANCH);
	addToElement(repo_details,"<b></br>Owner username:</b> " + json.WSA_SINGLE_REPOSITORY_OWNER_NAME);
	addToElement(repo_details,"<b></br>Repository name:</b> " + json.WSA_REPOSITORY_NAME);
	updateBranches(json.WSA_SINGLE_REPOSITORY_BRANCHES);
	var selected_user = document.getElementById("selected_user").innerHTML;
	if (selected_user === "null")
		addChangeBranchForm(json.WSA_SINGLE_REPOSITORY_BRANCHES, json.WSA_SINGLE_REPOSITORY_HEAD_BRANCH, json.WSA_REPOSITORY_LOCATION);
}


$(function() { // onload...do
	var repo_id = document.getElementById("repo_id").innerHTML;
	var selected_user = document.getElementById("selected_user").innerHTML;
	
	$.ajax({
            data: { "repo_id":repo_id, "username":selected_user },
            url: "single_repository",
			async: true, 
            timeout: 2000,
            error: function() {
                console.error("Error from server!");
            },
            success: fetchRepositoryData
    });
});