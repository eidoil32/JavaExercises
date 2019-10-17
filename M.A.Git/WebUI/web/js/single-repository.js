
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

function addToElement(text) {
	var element = document.getElementById("repository_details");
	element.innerHTML += text;
}

function refreshBranchesSelector(branchSelector, headBranch) {
	var selector = document.getElementById("branchSelector");
	selector.remove(selector.selectedIndex);
	
	var option = document.createElement("option");
	option.value = headBranch;
	option.text = headBranch;
	selector.add(option);
	
	document.getElementById("headbranch-p").innerHTML = branchSelector;
}

function checkoutFunction(headBranch) {	
	
	var selectedBranch = document.getElementById('branchSelector').value;
	
	$.ajax({
		data: {
			location: document.getElementById('location').value,
			branchSelector: selectedBranch
		},
		async: true, 
		url: "changeHeadBranch",
		timeout: 2000,
		error: function() {
			console.error("Error from server!");
		},
		success: function(data) {
			if (data === "true") {
				refreshBranchesSelector(selectedBranch, headBranch);
				document.getElementById("checkoutForm").setAttribute("onsubmit", "checkoutFunction('" + selectedBranch + "')");
			} else {
				magitShowError("There's open changes, do commit first.");
			}
		}
	});
}

function splitNames(text, regex) {
	return text.split(regex);
}

function addCommitToTable(commit, id, repositoryID, user) {
	$('#commits-table tr:last')
	$('#commits-table tr:last')
		.after('<tr ' + addTagsForTR("commit_" + id, "selectMenuItem(null, 'single-commit.html?sha1=" + commit.WSA_SINGLE_COMMIT_SHA1_KEY + "&repository_id=" + repositoryID + "&user_id=" + user + "');") + '><td>' + 
			(id) + 
			'</td><td>' + 
			 commit.WSA_SINGLE_COMMIT_SHA1_KEY +
			 '</td><td>' + 
			 commit.WSA_SINGLE_COMMIT_COMMENT_KEY +
			 '</td><td>' + 
			 commit.WSA_SINGLE_COMMIT_DATE_KEY +
			 '</td><td>' + 
			 commit.WSA_SINGLE_COMMIT_CREATOR_KEY +
			 '</td><td>' + 
			 splitNames(commit.WSA_SINGLE_COMMIT_POINTED_BRANCHES, "****") +
			'</td></tr>');	
			
}

function updateCommits(commitsList, repositoryID, user) {
	var index = 1;
	commitsList.forEach(function(entry) {
		addCommitToTable(jQuery.parseJSON(entry), index++, repositoryID, user);
	});
}

function addChangeBranchForm(branches, headBranch, location) {
	var myForm =	'<form method="POST" id="checkoutForm" onsubmit=\'checkoutFunction("' + headBranch + '")\' class="form-inline"><b>Checkout to other branch:</b></br>' +
						'<input type="hidden" id="location" name="location" value="'+ location +'">' +
						'<div class="form-group mb-2" style="margin-right: 5px;">' +
							'<select class="form-control form-control-sm" id="branchSelector" name="branchSelector">';
	for (var index = 0; index < Object.keys(branches).length; index++) {
		if (branches[index] !== headBranch.toString())
			myForm += 				'<option>' + branches[index] + '</option>';
	}				
	myForm 	+=				'</select>' +
						'</div>'+
						'<button type="submit" class="btn btn-primary mb-2">CHECKOUT</button>' + 
					'</form>';

	
	$(myForm).appendTo('#repository_details');
	$("#checkoutForm").submit(function(e) {
		e.preventDefault();
	});
}

function fetchRepositoryData(data, repo_id, selected_user) {
	var json = jQuery.parseJSON(data);
	var repo_details = 

	addToElement("<b>Head branch:</b> <span id='headbranch-p'>" + json.WSA_SINGLE_REPOSITORY_HEAD_BRANCH + "</span>");
	addToElement("<b></br>Owner username:</b> " + json.WSA_SINGLE_REPOSITORY_OWNER_NAME);
	addToElement("<b></br>Repository name:</b> " + json.WSA_REPOSITORY_NAME);
	updateBranches(json.WSA_SINGLE_REPOSITORY_BRANCHES);
	updateCommits(json.WSA_SINGLE_REPOSITORY_ALL_COMMITS, repo_id, selected_user);
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
            success: function(data) {
				fetchRepositoryData(data, repo_id, selected_user);
			}
    });
});