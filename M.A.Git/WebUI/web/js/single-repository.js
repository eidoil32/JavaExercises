function getFullPath(file, counter) {
    var rootFolder = file.parentElement.parentElement;
    if (rootFolder.id === "filetree_div")
        return file.getElementsByTagName("a")[0].innerText;
    return getFullPath(rootFolder) + "," + file.getElementsByTagName("a")[0].innerText;
}

function checkIsRemote(branchName) {
	return branchName.split("\\").length == 2;
}

function saveFile() {
	var repository = document.getElementById('repository').value;
	var path = document.getElementById('path').value.replace(',','/');
	var user = document.getElementById('user').value;
	var content = document.getElementById('file_content').value;
	
	$.ajax({
        async: true,
        data: {
            "repository_id": repository,
            "user_id": user,
			"file_content": content,
            "file_path": path
        },
        url: "save_file",
        timeout: 2000,
        error: function() {
            magitShowError("Saving file failed!");
        },
        success: function(data) {
            magitShowSuccess("Saving file done successfully!");
        }
    });
}

function createFileViewer(filename, content, path, repository, user) {
    if (content !== "") {
		var myForm ='<form method="POST" id="saveFileForm" onsubmit=\'saveFile()\' class="form-inline"><b>' + filename.getElementsByTagName("a")[0].innerText + '\'s content:</b></br>' +
						'<input type="hidden" id="repository" name="repository" value="'+ repository +'">' +
						'<input type="hidden" id="path" name="path" value="'+ path +'">' +
						'<input type="hidden" id="user" name="user" value="'+ user +'">' +
						'<div class="form-group mb-2" style="margin-right: 5px;">' +
							'<textarea class="form-control" rows="10" style="min-width: 250px; min-height: 10%; height:100%; resize:vertical;" id="file_content" rows="3" ';
		if (user !== "null") {
			myForm += 'disabled';
		}
		myForm += 		'>' + content + 
							'</textarea>' +
						'</div>';
		if (user === "null") {
			myForm += '</br><button type="submit" style="margin-top: 5px;" class="btn btn-primary mb-2">Save</button>';
		}
		myForm += '</form>';


        document.getElementById('textEditor').innerHTML = myForm;
		$("#saveFileForm").submit(function(e) {
			e.preventDefault();
		});
    }
}

function loadFileData(file, repository, user, commitSHA) {
    var path = getFullPath(file);
    $.ajax({
        async: true,
        data: {
            "sha-1": commitSHA,
            "repository_id": repository,
            "user_id": user,
            "file_path": path
        },
        url: "file_content",
        timeout: 2000,
        error: function() {
            console.error("Error from server!");
        },
        success: function(data) {
            createFileViewer(file, data, path, repository, user);
        }
    });
}

function updateBranches(branchesList) {
	let size = Object.keys(branchesList).length;
	for (let index = 0; index < size; index++) {
		let branchName = "";
		if (checkIsRemote(branchesList[index])) {
			branchName += "<span class='badge badge-info' style='margin-right: 5px;'>RB</span>";
		}
		branchName += branchesList[index];
	$('#branches-table tr:last')
		.after('<tr><td>' + 
			(index + 1) + 
			'</td><td>' + 
			branchName +
			'</td></tr>');
	}
}

function addToElement(text) {
	let element = document.getElementById("repository_details");
	element.innerHTML += text;
}

function refreshBranchesSelector(branchSelector, headBranch) {
	let selector = document.getElementById("branchSelector");
	selector.remove(selector.selectedIndex);
	
	let option = document.createElement("option");
	option.value = headBranch;
	option.text = headBranch;
	selector.add(option);
	
	document.getElementById("headbranch-p").innerHTML = branchSelector;
}

function checkoutFunction(headBranch) {	
	
	let selectedBranch = document.getElementById('branchSelector').value;
	
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

function merge_Function(repository, user) {
	var selectedBranchToMerge = document.getElementById("merge_branchSelector").value;
	var target = "";
	if (checkIsRemote(selectedBranchToMerge)) {
		target += "&rb=true&target=" + selectedBranchToMerge.split("\\")[1];
	} else {
		target += "&target=" +selectedBranchToMerge;
	}
	selectMenuItem(null, "merge.html?repository=" + repository + "&user=" + user + target);
}


function addMergeButtonForm(branches, headBranch, location, repository, user) {
	var existsOtherBranches = false;
	var myForm =	'<td><form method="POST" id="mergeForm" onsubmit=\'merge_Function("' + repository + '","' + user + '")\' class="form-inline"><b>Merge to branch:</b></br>' +
						'<input type="hidden" id="merge_location" name="merge_location" value="'+ location +'">' +
						'<div class="form-group mb-2" style="margin-right: 5px;">' +
							'<select class="form-control form-control-sm" id="merge_branchSelector" name="merge_branchSelector">';
	for (var index = 0; index < Object.keys(branches).length; index++) {
		if (branches[index] !== headBranch.toString()) {
			existsOtherBranches = true;
			myForm += 				'<option>' + branches[index] + '</option>';
		}
	}				
	myForm 	+=				'</select>' +
						'</div>'+
						'<button type="submit" class="btn btn-primary mb-2" id="mergeButton">MERGE</button>' + 
					'</form></td>';

	
	$(myForm).appendTo('#repository_options');
	if (!existsOtherBranches) {
		$(new Option('No branch to select', 'empty')).appendTo('#merge_branchSelector');
		$("#mergeButton").prop("disabled",true);
	}
	$("#mergeForm").submit(function(e) {
		e.preventDefault();
	});
}

function addChangeBranchForm(branches, headBranch, location) {
	var existsOtherBranches = false;
	var myForm =	'<td><form method="POST" id="checkoutForm" onsubmit=\'checkoutFunction("' + headBranch + '")\' class="form-inline"><b>Checkout to other branch:</b></br>' +
						'<input type="hidden" id="location" name="location" value="'+ location +'">' +
						'<div class="form-group mb-2" style="margin-right: 5px;">' +
							'<select class="form-control form-control-sm" id="branchSelector" name="branchSelector">';
	for (var index = 0; index < Object.keys(branches).length; index++) {
		if (branches[index] !== headBranch.toString() && !checkIsRemote(branches[index])) {
			existsOtherBranches = true;
			myForm += 				'<option>' + branches[index] + '</option>';
		}
	}				
	myForm 	+=				'</select>' +
						'</div>'+
						'<button type="submit" class="btn btn-primary mb-2" id="checkoutButton" >CHECKOUT</button>' + 
					'</form></td>';

	
	$(myForm).appendTo('#repository_options');
	
	if (!existsOtherBranches) {
		$(new Option('No branch to select', 'empty')).appendTo('#branchSelector');
		$("#checkoutButton").prop("disabled",true);
	}
	
	$("#checkoutForm").submit(function(e) {
		e.preventDefault();
	});
}

function loadCommitData(data, rootFolderID) {
    var tree = jQuery.parseJSON(data);

    Object.keys(tree).forEach(function(key) {
        if (checkFolder(tree[key])) {
            var folderID = "folder_" + key;
            createFolder(key, rootFolderID, folderID);
            loadCommitData(tree[key], folderID);
        } else {
            createNode(key, rootFolderID);
        }
    })
}

function makeItTree(repository, user, commitSHA) {
    $('#filetree_div').jstree();
    $('#filetree_div').on('changed.jstree', function(e, data) {
        loadFileData(document.getElementById(data.selected), repository, user, commitSHA);
    }).jstree();
}

function setFileTree(file_tree, repo_id, user, rootFolderID) {
	loadCommitData(file_tree, rootFolderID);
	makeItTree(repo_id, user, null);
}

function addToCommitTable(commit, id, headBranch, repositoryID, user) {
	$('<tr ' + addTagsForTR("commit_" + id, "selectMenuItem(null, 'single-commit.html?sha1=" + commit.WSA_SINGLE_COMMIT_SHA1_KEY + "&repository_id=" + repositoryID + "&user_id=" + user + "');") + '><td>' + 
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
			 headBranch +
			'</td></tr>').insertBefore('table#commits-table tbody');
}

function doCommit(repository, user, comment, headBranch) {
	$.ajax({
        async: true,
        data: {
            "repository_id": repository,
            "user_id": user,
			"comment" : comment
        },
        url: "execute_commit",
        timeout: 2000,
        error: function(data) {
            magitShowError(data['responseText']);
        },
        success: function(data) {
			addToCommitTable(data, $('#commits-table tr').length, headBranch, repository, user);
            magitShowSuccess("Commit done successfully!");
        }
    });
}

function createPullRequest(comment, lr_Branch, rr_Branch, branches, repository, user) {
	console.log("RR branch: " + rr_Branch + ", LR Branch: " + lr_Branch + ", Comment: " + comment);
}

function addPullRequestButton(branches, repository, user) {
	var repositoryOptionSecondRow = document.getElementById("repository_options_second_row");
	var pullRequestButton = "<td>" + 
								"<b>Create pull request:</b></br>" + 
								"<button class='btn btn-primary mb-2' id='pull_request_button' data-toggle='modal' data-target='#pullRequestModal'>" + 
								"Pull Request</button>" + 
							"</td>";
	$(pullRequestButton).appendTo(repositoryOptionSecondRow);
	$( "#send_pr" ).click(function() {
		var comment = document.getElementById("pull_request_comment_textarea").value;
		var lr_Branch = document.getElementById("PR_LR_branchSelector").value;
		var rr_Branch = document.getElementById("PR_RR_branchSelector").value;
		if (comment != null && lr_Branch != null && rr_Branch != null && comment !== "") {
			$( "#pull_request_close" ).trigger( "click" );
			createPullRequest(comment, lr_Branch, rr_Branch, branches, repository, user);
		}
	});
}

function addCollaborationButtons(headBranch, branches, location, repository, user) {
	var repositoryDetailes = document.getElementById("repository_options");
	var commit = "<td><b>Save files current contents:</b></br><button class='btn btn-primary mb-2' id='commit_button' data-toggle='modal' data-target='#commitCommentModal'>Commit</button></td>";
	$(commit).appendTo(repositoryDetailes);
	$( "#save_comment" ).click(function() {
		var comment = document.getElementById("commit_comment_textarea").value;
		if (comment != null && comment !== "") {
			$( "#commit_comment_close" ).trigger( "click" );
			doCommit(repository, user, comment, headBranch);
		}
	});
	addMergeButtonForm(branches, headBranch, location, repository, user);
	
	
}

function setOpenedChangesFiles(filesList, repo_id) {
	var hasOpenedChanges = false;
	var arrayTypes = ["new_files", "edited_files", "deleted_files"];
	
	if (filesList.length != 0) {
		for (var i = 0; i < 3; i++) {
			var element = document.getElementById(arrayTypes[i]);
			var tempText = "";
			var json = jQuery.parseJSON(filesList[i]);
			if (json.length != 0) {
				hasOpenedChanges = true;
				Object.keys(json).forEach(function(key) {
					if (json[key] === "File") {
						tempText += "<i class='fa fa-file' aria-hidden='true'></i> ";
					} else {
						tempText += "<i class='fa fa-folder' aria-hidden='true'></i> ";
					}
					tempText += key.replace("repository_" + repo_id + "\\", "") + "</br>";
				});
			}
			if (tempText !== "") {
				element.innerHTML = tempText;
			}
		}
	}
	if (hasOpenedChanges) {
		document.getElementById("repository_opened_changes").style.display = "block";
	}
}

function checkOpenedChanges(repo_id, selected_user) {
		$.ajax({
				async: true,
				data: {
					"repository_id": repo_id,
					"user_id": selected_user
				},
				url: "check_opened_changes",
				timeout: 2000,
				error: function() {
					console.error("Error from server!");
				},
				success: function(data) {
					if (data === "true" || confirm('There is opened changes, Are you sure you want to reset branch and delete all changes?')) {
						resetBranch(repo_id, selected_user);
					}
				}
		});		
}

function resetBranch(repo_id, selected_user) {
	var commit_SHAONE = document.getElementById('commitSelector').value;
	$.ajax({
			async: true,
			data: {
				"selected_commit_shaone": commit_SHAONE,
				"repository_id": repo_id,
				"user_id": selected_user
			},
			url: "reset_branch",
			timeout: 2000,
			error: function() {
				console.error("Error from server!");
			},
			success: function(data) {
				magitShowSuccess("Reset branch ended successfully");
			}
	});	
}

function addResetBranchForm(allCommits, repo_id, selected_user) {
	var myForm =	'<td colspan="2"><form method="POST" id="resetBranchForm" onsubmit=\'checkOpenedChanges(' + repo_id + ',' + selected_user + ')\' class="form-inline"><b>Select commit sha-1 you want to reset:</b></br>' +
						'<div class="form-group mb-2" style="margin-right: 5px;">' +
							'<select class="form-control form-control-sm" id="commitSelector" name="commitSelector">';
	allCommits.forEach(function(entry) {
		myForm += 				'<option>' + jQuery.parseJSON(entry).WSA_SINGLE_COMMIT_SHA1_KEY + '</option>';
	});
		myForm 	+=			'</select>' +
						'</div>'+
						'<button type="submit" class="btn btn-primary mb-2">RESET</button>' + 
					'</form></td>';

	
	$(myForm).appendTo('#repository_options_second_row');
	$("#resetBranchForm").submit(function(e) {
		e.preventDefault();
	});
	

}

function pullRequestsCenter(data, repository, user) {
	try {
		var requests = jQuery.parseJSON(data);
		var numOfRequests = Object.keys(requests).length;
		if (numOfRequests > 0) {
			$("#repository_pull_requests").html("<a data-toggle='tab' href='#tabs-6'>Pull Requests <span class='badge badge-secondary'>" + numOfRequests + "</span></a>")
			$("#repository_pull_requests").css("display","block");
		}
	} catch (error) {}
}

function fetchRepositoryData(data, repo_id, selected_user) {
	var json = jQuery.parseJSON(data);
	var isRemote = json.WSA_SINGLE_REPOSITORY_IS_RT != null;

	addToElement("<b>Head branch:</b> <span id='headbranch-p'>" + json.WSA_SINGLE_REPOSITORY_HEAD_BRANCH + "</span>");
	addToElement("<b></br>Owner username:</b> " + json.WSA_SINGLE_REPOSITORY_OWNER_NAME);
	addToElement("<b></br>Repository name:</b> " + json.WSA_REPOSITORY_NAME);
	if (isRemote == false) {
		document.getElementById("remote_repository").style.display = "none";
	}
	updateBranches(json.WSA_SINGLE_REPOSITORY_BRANCHES);
	updateCommits(json.WSA_SINGLE_REPOSITORY_ALL_COMMITS, repo_id, selected_user);
	setFileTree(json.WSA_SINGLE_REPOSITORY_FILE_TREE, repo_id, selected_user, "root_folder");
	setOpenedChangesFiles(json.WSA_SINGLE_REPOSITORY_OPENED_CHANGES, repo_id);
	var selected_user = document.getElementById("selected_user").innerHTML;
	if (selected_user === "null") {
		addResetBranchForm(json.WSA_SINGLE_REPOSITORY_ALL_COMMITS, repo_id, selected_user);
		addChangeBranchForm(json.WSA_SINGLE_REPOSITORY_BRANCHES, json.WSA_SINGLE_REPOSITORY_HEAD_BRANCH, json.WSA_REPOSITORY_LOCATION);
		addCollaborationButtons(json.WSA_SINGLE_REPOSITORY_HEAD_BRANCH, json.WSA_SINGLE_REPOSITORY_BRANCHES, json.WSA_REPOSITORY_LOCATION, repo_id, selected_user);
		if (isRemote) {
			addPullRequestButton();
		}
		pullRequestsCenter(json.WSA_SINGLE_REPOSITORY_PR, repo_id, selected_user);
	} else {
		$("#repository_options_tab_button").css("display","none");
	}
}

function repositoryDataFetching() {
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
}

$(function() { // onload...do
	repositoryDataFetching();
});