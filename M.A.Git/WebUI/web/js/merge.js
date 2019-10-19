function GetCookieValue(name) {
    var cookies = document.cookie;
    var str = cookies.split(";");
    var parameters = str[0].split("?");
    var urlParams = parameters[1].split("&");
    for (var i = 0; i < Object.keys(urlParams).length; i++) {
        var parts = urlParams[i].split("=");
        if (parts[0] === name) {
            return parts[1];
        }
    }
}

function endMerge(repo_id, selectedBranch, user, comment) {
	var files = {};
	
	Object.keys(sessionStorage).forEach(function(key) {
		files[key] = sessionStorage[key];
	});
	
	files["repository_id"] = repo_id;
	files["selectedBranch"] = selectedBranch;
	files["user_id"] = user;
	files["user_comment"] = comment;
	
	console.log(files);
	
	$.ajax({
            async: true,
            data: files,
            url: "end_merge",
            timeout: 2000,
            error: function() {
                console.error("Error from server!");
            },
            success: function(data) {
				console.log("end merge");
			}
    });
}

function removeFromList(elementID) {
	var lis = document.querySelectorAll('#merge_file_list li');
	for(var i = 0; li = lis[i]; i++) {
		if (li.id === elementID)
			li.parentNode.removeChild(li);
	}
	
	$("textarea#merge_file_content").val("Please choose file from list");
	$("textarea#merge_file_content").prop('disabled', true);
}

function editFileContent(repo_id, selectedBranch, user) {
	var currentFilename = $("#currentFile").html();
	sessionStorage.removeItem("repository_" + repo_id + "\\" + currentFilename);
	sessionStorage.setItem(currentFilename, $("#merge_file_content").val());
	 
	removeFromList("repository_" + repo_id + "\\" + currentFilename);
	document.getElementById("button_merge_form").setAttribute('disabled', 'disabled');
	
	var currentCounter = $("#counterFiles").html();
	if (currentCounter - 1 == 1) {
		$("#button_merge_form").html("Save");
	} else if (currentCounter - 1 == 0) {
		$('#mergeCommentModal').modal('show');
		$( "#merge_comment" ).click(function() {
			var comment = document.getElementById("merge_comment_textarea").value;
			if (comment != null && comment !== "") {
				$( "#merge_comment_close" ).trigger( "click" );
				endMerge(repo_id, selectedBranch, user, comment);
			}
		});
		
	}
	
	$("#counterFiles").html(currentCounter-1);
}

function selectFile(element, repo_id) {
	$('#merge_file_list li').each(function(i) {
        this.classList.remove("active");
	});
	
	element.classList.add('active');
	addContent(element.id,jQuery.parseJSON(sessionStorage.getItem(element.id)),repo_id);
	document.getElementById("button_merge_form").removeAttribute('disabled');
	$("textarea#merge_file_content").prop('disabled', false);
	$("textarea#merge_file_content").val("");
}

function addToFilesList(fileName, repo_id, active) {
	var file = "<li class='list-group-item " + active + "' id='" + fileName + "' onclick='selectFile(this," + repo_id + ")' style='cursor: pointer;'>" + fileName.replace("repository_" + repo_id + "\\", "") + "</li>";
	$("#merge_file_list").append(file);
}

function setTextAreaContent(textAreaID, content) {
	$("textarea#" + textAreaID).val(content);
}

function setOnClickButtonToCuston(buttonID, content) {
	$(buttonID).click(function(){
		$("textarea#merge_file_content").val(content);
	});
}

function addContent(filename, contentsJSON, repo_id) {
	$("#currentFile").html(filename.replace("repository_" + repo_id + "\\", ""));
	setTextAreaContent("file_ancestor", contentsJSON.KEY_ANCESTOR_MAP);
	setTextAreaContent("file_ours", contentsJSON.KEY_ACTIVE_MAP);
	setTextAreaContent("file_theirs", contentsJSON.KEY_TARGET_MAP);

	//$("textarea#file_theirs").val(contentsJSON.KEY_TARGET_MAP);
	setOnClickButtonToCuston("#button_ancestor_content",contentsJSON.KEY_ANCESTOR_MAP);
	setOnClickButtonToCuston("#button_ours_content",contentsJSON.KEY_ACTIVE_MAP);
	setOnClickButtonToCuston("#button_theirs_content",contentsJSON.KEY_TARGET_MAP);
}

function saveFileContent(filename, content) {
	var jsonArray = '{ "KEY_ANCESTOR_MAP":"' + content.KEY_ANCESTOR_MAP +
			'","KEY_ACTIVE_MAP":"' + content.KEY_ACTIVE_MAP +
	'","KEY_TARGET_MAP":"' + content.KEY_TARGET_MAP + '"}';
	sessionStorage.setItem(filename, jsonArray);
}

function fetchMergeStepTwoData(data, repository_id,  selectedBranch, user) {
	var files = jQuery.parseJSON(data);
	var first = "active";
	var counter = 0;
	sessionStorage.clear();
	
	Object.keys(files).forEach(function(key) {
		counter++;
        addToFilesList(key, repository_id, first);
		if (first === "active") {
			addContent(key,files[key], repository_id);
		}
		saveFileContent(key, files[key]);
		first = "";
    })
	
	$("#counterFiles").html(counter);
	
	if (counter == 1) {
		$("#button_merge_form").val("Save");
	}
	$("#merge_form").submit(function(e) {
		e.preventDefault();
		editFileContent(repository_id, selectedBranch, user);
	});
}

$(function() {
    var selectedBranch = GetCookieValue('target');
    var repository = GetCookieValue('repository');
    var user = GetCookieValue('user');

    if (selectedBranch != null) {
        $.ajax({
            async: true,
            data: {
                "selected_branch": selectedBranch,
                "repository_id": repository,
                "user_id": user
            },
            url: "merge_step_two",
            timeout: 2000,
            error: function() {
                console.error("Error from server!");
            },
            success: function(data) {
				fetchMergeStepTwoData(data, repository, selectedBranch, user);
			}
        });
    }
});