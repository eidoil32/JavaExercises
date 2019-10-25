function makeAjaxRequests(data, url, errorFunction, successFunction) {
    $.ajax({
        contentType: 'application/json; charset=utf-8',
        async: true,
        data: data,
        timeout: 2000,
        url: url,
        error: function (data) {
            if (data.status === 200) {
                successFunction(data);
            } else {
                errorFunction(data);
            }
        },
        success: function (data) {
            successFunction(data);
        }
    });
}

function getFullPath(file, counter) {
    if (file != null) {
        let rootFolder = file.parentElement.parentElement;
        if (rootFolder.id === "full_filetree_div")
            return file.getElementsByTagName("a")[0].innerText;
        return getFullPath(rootFolder) + "," + file.getElementsByTagName("a")[0].innerText;
    }
}

function checkIsRemote(branchName) {
    return branchName.split("\\").length === 2;
}

function saveFile() {
    let repository = document.getElementById('repository').value,
        path = document.getElementById('path').value.replace(',', '/'),
        user = document.getElementById('user').value,
        content = document.getElementById('file_content').value;

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
        error: function () {
            magitShowError("Saving file failed!");
        },
        success: function (data) {
            updateOpenedChanges(path, "edited_files");
            magitShowSuccess("Saving file done successfully!");
        }
    });
}

function updateOpenedChanges(fileFullPath, category) {
    let fileInDiv = document.getElementById(fileFullPath + "_" + category);
    if (fileInDiv == null) {
        $("#repository_opened_changes").css('display', 'block');
        $("#" + category).append("</br><i class='fa fa-file' aria-hidden='true' id='" + fileFullPath + "_" + category + "'></i> " + fileFullPath);
        $("#" + category + "_empty").css('display', 'none');
    }
}

function createFileViewer(filename, content, path, repository, user) {

    if (content !== "") {
        let myForm = '<form method="POST" id="saveFileForm" onsubmit=\'saveFile()\' class="form-inline"><b>' + filename.getElementsByTagName("a")[0].innerText + '\'s content:</b></br>' +
            '<input type="hidden" id="repository" name="repository" value="' + repository + '">' +
            '<input type="hidden" id="path" name="path" value="' + path + '">' +
            '<input type="hidden" id="user" name="user" value="' + user + '">' +
            '<div class="form-group mb-2" style="margin-right: 5px;">' +
            '<textarea class="form-control" rows="10" style="min-width: 250px; min-height: 10%; height:100%; resize:vertical;" id="file_content" rows="3" ';
        if (user !== "null") {
            myForm += 'disabled';
        }
        myForm += '>' + content +
            '</textarea>' +
            '</div>';
        if (user === "null") {
            myForm += '<div style="margin-top: 5px;"></br><button id="deleted_file_button" class="btn btn-danger">Delete File</button>'
            myForm += '<button type="submit" style="margin-left: 10px" class="btn btn-success" id="button_save_file_content" disabled>Save</button></div>';
        }
        myForm += '</form>';


        document.getElementById('full_textEditor').innerHTML = myForm;
        $('#file_content').on('input selectionchange propertychange', function () {
            $('#button_save_file_content').prop('disabled', '');
        });
        $("#saveFileForm").submit(function (e) {
            e.preventDefault();
        });

        $("#deleted_file_button").click(function (e) {
            e.preventDefault();
            makeAjaxRequests({
                    "repo_id": repository,
                    "user": user,
                    "path": path
                }, "delete_file",
                function (data) {
                    magitShowError(data.responseText);
                }, function (data) {
                    $('#full_filetree_div').jstree().delete_node(filename);
                    updateOpenedChanges(path, "deleted_files");
                    magitShowSuccess("File deleted successfully!");
                });
        });
    }
}

function createNewFile(path, repository, user, file) {
    let realPath = path.replace("Add new file", "");
    realPath = realPath.replace(/,/g, "\\");
    let fileParent;
    if (realPath === "") {
        realPath = "\\";
        fileParent = "#";
    } else {
        fileParent = file.parentElement.parentElement;
    }
    $("#create_new_file_btn").click();
    $("#new_file_folder").val(realPath);
    $("#create_new_file").click(function () {
        makeAjaxRequests({
            "repo_id": repository,
            "user": user,
            "content": $("#new_file_content").val(),
            "file_name": $("#new_file_name").val(),
            "path": realPath
        }, "create_file", function (data) {
            magitShowError("Creating file failed!");
        }, function (data) {
            $("#close_modal_new_file").click();
            $('#full_filetree_div').jstree().create_node(fileParent, $("#new_file_name").val(), "first");
            updateOpenedChanges(realPath + "\\" + $("#new_file_name").val(), "new_files");
            magitShowSuccess("The file " + $("#new_file_name").val() + " Created successfully!");
        });
    });
}

function loadFileData(file, repository, user, commitSHA) {
    if (file != null) {
        let path = getFullPath(file);
        if (path.indexOf("Add new file") >= 0) {
            createNewFile(path, repository, user, file);
        }
        makeAjaxRequests({
            "sha-1": commitSHA,
            "repository_id": repository,
            "user_id": user,
            "file_path": path
        }, "file_content", function (data) {
            magitShowError(data.responseText);
        }, function (data) {
            createFileViewer(file, data, path, repository, user);
        });
    }
}

function updateBranches(branchesList) {
    let size = Object.keys(branchesList).length;
    window.numOfBranches = size;
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
        error: function () {
            console.error("Error from server!");
        },
        success: function (data) {
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

function addCommitToTable(commit, id, repositoryID, user, tableID) {
    $(tableID + ' tr:last')
        .after('<tr ' + addTagsForTR("commit_" + id, "showCommit('" + commit.WSA_SINGLE_COMMIT_SHA1_KEY + "','" + repositoryID + "','" + user + "');") + '><td>' +
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
    commitsList.forEach(function (entry) {
        addCommitToTable(jQuery.parseJSON(entry), index++, repositoryID, user, "#commits-table");
    });
}

function merge_Function(repository, user) {
    var selectedBranchToMerge = document.getElementById("merge_branchSelector").value;
    var target = "";
    if (checkIsRemote(selectedBranchToMerge)) {
        target += "&rb=true&target=" + selectedBranchToMerge.split("\\")[1];
    } else {
        target += "&target=" + selectedBranchToMerge;
    }
    selectMenuItem(null, "merge.html?repository=" + repository + "&user=" + user + target);
}


function addMergeButtonForm(branches, headBranch, location, repository, user) {
    var existsOtherBranches = false;
    var myForm = '<td><form method="POST" id="mergeForm" onsubmit=\'merge_Function("' + repository + '","' + user + '")\' class="form-inline"><b>Merge to branch:</b></br>' +
        '<input type="hidden" id="merge_location" name="merge_location" value="' + location + '">' +
        '<div class="form-group mb-2" style="margin-right: 5px;">' +
        '<select class="form-control form-control-sm" id="merge_branchSelector" name="merge_branchSelector">';
    for (var index = 0; index < Object.keys(branches).length; index++) {
        if (branches[index] !== headBranch.toString()) {
            existsOtherBranches = true;
            myForm += '<option>' + branches[index] + '</option>';
        }
    }
    myForm += '</select>' +
        '</div>' +
        '<button type="submit" class="btn btn-primary mb-2" id="mergeButton">MERGE</button>' +
        '</form></td>';


    $(myForm).appendTo('#repository_options');
    if (!existsOtherBranches) {
        $(new Option('No branch to select', 'empty')).appendTo('#merge_branchSelector');
        $("#mergeButton").prop("disabled", true);
    }
    $("#mergeForm").submit(function (e) {
        e.preventDefault();
    });
}

function addChangeBranchForm(branches, headBranch, location) {
    var existsOtherBranches = false;
    var myForm = '<td><form method="POST" id="checkoutForm" onsubmit=\'checkoutFunction("' + headBranch + '")\' class="form-inline"><b>Checkout to other branch:</b></br>' +
        '<input type="hidden" id="location" name="location" value="' + location + '">' +
        '<div class="form-group mb-2" style="margin-right:5px;">' +
        '<select class="form-control form-control-sm" id="branchSelector" name="branchSelector">';
    for (var index = 0; index < Object.keys(branches).length; index++) {
        if (branches[index] !== headBranch.toString() && !checkIsRemote(branches[index])) {
            existsOtherBranches = true;
            myForm += '<option>' + branches[index] + '</option>';
        }
    }
    myForm += '</select>' +
        '</div>' +
        '<button type="submit" class="btn btn-primary mb-2" id="checkoutButton" >CHECKOUT</button>' +
        '</form></td>';


    $(myForm).appendTo('#repository_options');

    if (!existsOtherBranches) {
        $(new Option('No branch to select', 'empty')).appendTo('#branchSelector');
        $("#checkoutButton").prop("disabled", true);
    }

    $("#checkoutForm").submit(function (e) {
        e.preventDefault();
    });
}

function loadCommitData(data, rootFolderID, user) {
    let tree = jQuery.parseJSON(data);

    Object.keys(tree).forEach(function (key) {
        if (checkFolder(tree[key])) {
            let folderID = "folder_" + key;
            createFolder(key, rootFolderID, folderID);
            loadCommitData(tree[key], folderID, user);
            if (user === "null") {
                createNode("Add new file", folderID, "fa fa-plus");
            }
        } else {
            createNode(key, rootFolderID, "fa fa-file");
        }
    })
}

function makeItTree(repository, user, commitSHA) {
    $('#full_filetree_div').on('changed.jstree', function (e, data) {
        loadFileData(document.getElementById(data.selected), repository, user, commitSHA);
    }).jstree({
        'core': {
            'check_callback': true
        }
    });
}

function setFileTree(file_tree, repo_id, user, rootFolderID) {
    loadCommitData(file_tree, rootFolderID, user);
    if (user === "null") {
        createNode("Add new file", rootFolderID, "fa fa-plus");
    }
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
            "comment": comment
        },
        url: "execute_commit",
        timeout: 2000,
        error: function (data) {
            magitShowError(data['responseText']);
        },
        success: function (data) {
            addToCommitTable(jQuery.parseJSON(data), $('#commits-table tr').length, headBranch, repository, user);
            magitShowSuccess("Commit done successfully!");
        }
    });
}

function createPullRequest(comment, lr_Branch, rr_Branch, repository, user) {
    makeAjaxRequests({
            "comment": comment,
            "lr_Branch": lr_Branch,
            "rr_Branch": rr_Branch,
            "repository_id": repository,
            "user_id": user
        }, "create_pr",
        function (data) {
            magitShowError(data);
        }, function (data) {
            magitShowSuccess("Pull request sent successfully!</br>" +
                "Now wait for remote repository owner to approve you request!");
        });
}

function addPullRequestButton(branches, headBranch, repository, user) {
    var repositoryOptionSecondRow = document.getElementById("repository_options_second_row");
    var pullRequestButton = "<td>" +
        "<b>Create pull request:</b></br>" +
        "<button class='btn btn-primary mb-2' id='pull_request_button' data-toggle='modal' data-target='#pullRequestModal'>" +
        "Pull Request</button>" +
        "</td>";
    $(pullRequestButton).appendTo(repositoryOptionSecondRow);

    for (var index = 0; index < Object.keys(branches).length; index++) {
        if (checkIsRemote(branches[index])) {
            $(new Option(branches[index], branches[index])).appendTo('#PR_RR_branchSelector');
        } else {
            $(new Option(branches[index], branches[index])).appendTo('#PR_LR_branchSelector');
        }
    }

    $("#send_pr").click(function () {
        var comment = document.getElementById("pull_request_comment_textarea").value;
        var lr_Branch = document.getElementById("PR_LR_branchSelector").value;
        var rr_Branch = document.getElementById("PR_RR_branchSelector").value;
        if (comment != null && lr_Branch != null && rr_Branch != null && comment !== "") {
            $("#pull_request_close").trigger("click");
            createPullRequest(comment, lr_Branch, rr_Branch, repository, user);
        }
    });
}

function addPullButton(repository, user) {
    let repositoryDetails = document.getElementById("repository_options"),
        pushButton = "<td><b>Simple pull data from RR:</b></br>" +
            "<button class='btn btn-primary mb-2' id='pull_data_button'>" +
            "Pull</button></td>";
    $(pushButton).appendTo(repositoryDetails);
    $("#pull_data_button").click(function () {
        makeAjaxRequests({
            "repo_id": repository,
            "user": user
        }, "pull_head", function (data) {
            magitShowError(data.responseText);
        }, function (data) {
            magitShowSuccess("Pull data ended successfully!");
        })
    });
}

function addCollaborationButtons(headBranch, branches, location, repository, user) {
    let repositoryDetails = document.getElementById("repository_options"),
        commit = "<td><b>Save files current contents:</b></br><button class='btn btn-primary mb-2' id='commit_button' data-toggle='modal' data-target='#commitCommentModal'>Commit</button></td>";
    $(commit).appendTo(repositoryDetails);
    $("#save_comment").click(function () {
        let comment = document.getElementById("commit_comment_textarea").value;
        if (comment != null && comment !== "") {
            $("#commit_comment_close").trigger("click");
            doCommit(repository, user, comment, headBranch);
        }
    });
    addMergeButtonForm(branches, headBranch, location, repository, user);
    addPullButton(repository, user);
}

function setOpenedChangesFiles(filesList, repo_id) {
    let hasOpenedChanges = false,
        arrayTypes = ["new_files", "edited_files", "deleted_files"];

    if (filesList.length > 0) {
        for (let i = 0; i < 3; i++) {
            let element = document.getElementById(arrayTypes[i]), tempText = "",
                json = jQuery.parseJSON(filesList[i]);
            if (json.length > 0) {
                hasOpenedChanges = true;
                Object.keys(json).forEach(function (key) {
                    let filename = key.replace("repository_" + repo_id + "\\", "");
                    if (json[key] === "File") {
                        tempText += "<i class='fa fa-file' aria-hidden='true' id='" + filename + "_" + arrayTypes[i] + "'></i> ";
                    } else {
                        tempText += "<i class='fa fa-folder' aria-hidden='true' id='" + filename + "_" + arrayTypes[i] + "'></i> ";
                    }
                    tempText += filename + "</br>";
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
    makeAjaxRequests({
        "repository_id": repo_id,
        "user_id": selected_user
    }, "check_opened_changes", function (data) {
        console.error("Error from server!");
    }, function (data) {
        if (data === "true" || confirm('There is opened changes, Are you sure you want to reset branch and delete all changes?')) {
            resetBranch(repo_id, selected_user);
        }
    });
}

function resetBranch(repo_id, selected_user) {
    const commit_SHAONE = document.getElementById('commitSelector').value;
    makeAjaxRequests({
        "selected_commit_shaone": commit_SHAONE,
        "repository_id": repo_id,
        "user_id": selected_user
    }, "reset_branch", function (data) {
        console.error("Error from server!");
    }, function (data) {
        magitShowSuccess("Reset branch ended successfully");
    });
}

function addResetBranchForm(allCommits, repo_id, selected_user, isRemote) {
    let myForm = '<td colspan="' + (isRemote ? 3 : 4) + '"><form method="POST" id="resetBranchForm" onsubmit=\'checkOpenedChanges(' + repo_id + ',' + selected_user + ')\' class="form-inline"><b>Select commit sha-1 you want to reset:</b></br>' +
        '<div class="form-group mb-2" style="margin-right: 5px;">' +
        '<select class="form-control form-control-sm" id="commitSelector" name="commitSelector">';
    allCommits.forEach(function (entry) {
        myForm += '<option>' + jQuery.parseJSON(entry).WSA_SINGLE_COMMIT_SHA1_KEY + '</option>';
    });
    myForm += '</select>' +
        '</div>' +
        '<button type="submit" class="btn btn-primary mb-2">RESET</button>' +
        '</form></td>';


    $(myForm).appendTo('#repository_options_second_row');
    $("#resetBranchForm").submit(function (e) {
        e.preventDefault();
    });


}

function showPullRequest(i) {
    $('#pull_requests_inner_div').css('display', 'none');
    window.pr_ID = i;
    $("#pull_requests_outter_div").css('display', 'block').load("single-pullRequest.html");
}

function showCommit(sha_1, repository_id, user) {
    $("#single_commit_sha1").html(sha_1);
    $("#single_commit_repo_id").html(repository_id);
    $("#single_commit_user").html(user);
    $("#single_commit_show").css('display', 'block').load("single-commit.html");
    $("#all_commits_show").css('display', 'none');
}

function pullRequestsCenter(data, repository, user) {
    const pullRequestsSize = Object.keys(data).length;
    try {
        if (pullRequestsSize > 0) {
            for (let i = 0; i < pullRequestsSize; i++) {
                let pullRequest = jQuery.parseJSON(data[i]);
                const row = '<tr ' + addTagsForTR("pull_request_" + i, "") + '><td>' +
                    (parseInt(pullRequest.PR_ID) + 1) +
                    '</td><td>' +
                    pullRequest.PR_REQUEST_CREATOR +
                    '</td><td>' +
                    pullRequest.PR_LOCAL_BRANCH_NAME +
                    '</td><td>' +
                    pullRequest.PR_REMOTE_BRANCH_NAME +
                    '</td><td>' +
                    pullRequest.PR_COMMENT +
                    '</td><td>' +
                    pullRequest.PR_DATE_CREATION +
                    '</td></tr>';
                $('#pull_request_table tr:last').after(row);

                $('#pull_request_' + i).click(function () {
                    showPullRequest(i);
                });
            }

            $("#repository_pull_requests").html("<a data-toggle='tab' href='#tabs-6'>Pull Requests <span class='badge badge-secondary'>" + pullRequestsSize + "</span></a>")
            $("#repository_pull_requests").css("display", "block");
        }
    } catch (error) {
    }
}

function addBranchToLists(branchName) {
    $(new Option(branchName, branchName)).appendTo("#branchSelector");
    if ($("#branchSelector").length > 1) {
        $("#checkoutButton").prop('disabled', '');
    }
    $(new Option(branchName, branchName)).appendTo("#merge_branchSelector");
    $(new Option(branchName, branchName)).appendTo("#select_branch_to_push");
    window.numOfBranches++;
    $('#branches-table tr:last').after('<tr><td>' + window.numOfBranches + '</td><td>' + branchName + '</td></tr>');
}

function addCreateBranchForm(repo_id, selected_user, commits, branches, remoteRepo) {
    const createBranch = "<td><button id='show_create_branch_modal' data-toggle='modal' data-target='#createNewBranchModal' class=\"btn btn-primary mb-2\">Create new branch</button></td>";
    $(createBranch).appendTo("#repository_options_third_row");

    Object.keys(commits).forEach(function (key) {
        let sha1 = jQuery.parseJSON(commits[key]).WSA_SINGLE_COMMIT_SHA1_KEY;
        $(new Option(sha1, sha1)).appendTo('#branch_commit');
    });


    if (remoteRepo) {
        $("#show_create_branch_modal").click(function () {
            $("#remote_tracking_branch").css("display", "block");
        });
        $("#branch_remote").change(function () {
            if ($("#branch_remote").val() !== "none") {
                $("#branch_name").prop('disabled', 'disabled');
            } else {
                $("#branch_name").prop('disabled', '');
            }
        });
        $(new Option('select branch', 'none')).appendTo('#branch_remote');
        Object.keys(branches).forEach(function (key) {
            if (checkIsRemote(branches[key])) {
                $(new Option(branches[key], branches[key])).appendTo('#branch_remote');
            }
        });
    } else {
        $("#show_create_branch_modal").click(function () {
            $("#remote_tracking_branch").css("display", "none");
        });
    }

    $("#branch_create").click(function () {
        const commit = $("#branch_commit").val(),
            branch_remote = $("#branch_remote").val(),
            name = $("#branch_name").val();
        $("#branch_close").click();
        makeAjaxRequests({
            "commit": commit,
            "tracking-after": branch_remote,
            "branch_name": name,
            "repo_id": repo_id,
            "user_id": selected_user
        }, "create_branch", function (data) {
            magitShowError(data.responseText);
        }, function (data) {
            addBranchToLists(name);
            magitShowSuccess("Creating branch '" + name + "' finish successfully!");
        });

    });
}

function addPushBranchButton(branches, repo_id, selected_user) {
    let pushBranch = "<td colspan='3'>" +
        "<div class='row'>" +
        "<div class='col-md-8'>" +
        "<select class=\"form-control\" id=\"select_branch_to_push\"></select>" +
        "</div><div class='col-md-4'>" +
        "<button id='push_branch_button' class=\"btn btn-primary mb-2\">Push Branch</button>" +
        "</div></td>";
    $(pushBranch).appendTo("#repository_options_third_row");
    Object.keys(branches).forEach(function (key) {
        if (!checkIsRemote(branches[key]))
            $(new Option(branches[key], branches[key])).appendTo('#select_branch_to_push')
    });

    $("#push_branch_button").click(function () {
        makeAjaxRequests({
            "branch_name": $("#select_branch_to_push").val(),
            "repo_id": repo_id,
            "user": selected_user
        }, "push_branch", function (data) {
            magitShowError(data.responseText);
        }, function (data) {
            magitShowSuccess("Push branch finish successfully!");
        });
    });
}

function fetchRepositoryData(data, repo_id, selected_user) {
    const json = jQuery.parseJSON(data);
    let isRemote = json.WSA_SINGLE_REPOSITORY_IS_RT != null;

    addToElement("<b>Head branch:</b> <span id='headbranch-p'>" + json.WSA_SINGLE_REPOSITORY_HEAD_BRANCH + "</span>");
    addToElement("<b></br>Owner username:</b> " + json.WSA_SINGLE_REPOSITORY_OWNER_NAME);
    addToElement("<b></br>Repository name:</b> " + json.WSA_REPOSITORY_NAME);
    if (isRemote === false) {
        document.getElementById("remote_repository").style.display = "none";
    }
    updateBranches(json.WSA_SINGLE_REPOSITORY_BRANCHES);
    updateCommits(json.WSA_SINGLE_REPOSITORY_ALL_COMMITS, repo_id, selected_user);
    setFileTree(json.WSA_SINGLE_REPOSITORY_FILE_TREE, repo_id, selected_user, "full_root_folder");
    setOpenedChangesFiles(json.WSA_SINGLE_REPOSITORY_OPENED_CHANGES, repo_id);

    if (selected_user === "null") {
        addCreateBranchForm(repo_id, selected_user, json.WSA_SINGLE_REPOSITORY_ALL_COMMITS,
            json.WSA_SINGLE_REPOSITORY_BRANCHES, isRemote);
        addResetBranchForm(json.WSA_SINGLE_REPOSITORY_ALL_COMMITS, repo_id, selected_user, isRemote);
        addChangeBranchForm(json.WSA_SINGLE_REPOSITORY_BRANCHES, json.WSA_SINGLE_REPOSITORY_HEAD_BRANCH, json.WSA_REPOSITORY_LOCATION);
        addCollaborationButtons(json.WSA_SINGLE_REPOSITORY_HEAD_BRANCH, json.WSA_SINGLE_REPOSITORY_BRANCHES, json.WSA_REPOSITORY_LOCATION, repo_id, selected_user);
        if (isRemote) {
            addPushBranchButton(json.WSA_SINGLE_REPOSITORY_BRANCHES, repo_id, selected_user);
            addPullRequestButton(json.WSA_SINGLE_REPOSITORY_BRANCHES, json.WSA_SINGLE_REPOSITORY_HEAD_BRANCH, repo_id, selected_user);
        }
        pullRequestsCenter(json.WSA_SINGLE_REPOSITORY_PR, repo_id, selected_user);
    } else {
        $("#repository_options_tab_button").css("display", "none");
    }
}

function repositoryDataFetching() {
    let repo_id = document.getElementById("repo_id").innerHTML,
        selected_user = document.getElementById("selected_user").innerHTML;

    makeAjaxRequests({"repo_id": repo_id, "username": selected_user}
        , "single_repository", function (data) {
            console.error("Error from server!");
        }, function (data) {
            fetchRepositoryData(data, repo_id, selected_user);
        });
}

$(function () { // onload...do
    repositoryDataFetching();
});