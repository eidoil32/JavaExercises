function sendApprovePR(pr_id, repo_id) {
    $.ajax({
        async: true,
        data: {
            "pr_id": pr_id,
            "repository_id": repo_id,
            "user_id": "null"
        },
        url: "approve_pr",
        timeout: 2000,
        error: function (data) {
            magitShowError(data.responseText);
        },
        success: function (data) {
            magitShowSuccess("Pull request as been approved,</br>" +
                "Message has been sent to the relevant user.");
            $("#pull_request_table  tr:eq(" + pr_id + ")").remove();
        }
    });
}

function rejectPR(pr_id, repo_id) {
    let cause_text = $("#reject_pull_request_comment_textarea").val();
    if (cause_text.length > 0) {
        $.ajax({
            async: true,
            data: {
                "cause": cause_text,
                "pr_id": pr_id,
                "repository_id": repo_id,
                "user_id": "null"
            },
            url: "reject_pr",
            timeout: 2000,
            error: function (data) {
                magitShowError(data.responseText);
            },
            success: function (data) {
                $("#show_all_pull_requests").click();
                magitShowSuccess("Pull request as been reject successfully,</br>" +
                    "Message has been sent to the relevant user.");
                $("#pull_request_table  tr:eq(" + pr_id + ")").remove();
            }
        });
    } else {
        magitShowError("Please fill your cause for rejecting this pull request!");
    }
}

function showFileContent(path, commitSHA1) {
    let realFileName = path.replace("repository_" + window.repo_ID + "\\", "");
    makeAjaxRequests({
        "repo_id": window.repo_ID,
        "user": "null",
        "pr_id": window.pr_ID,
        "sha_1": commitSHA1,
        "path": realFileName
    }, "show_pr_file_content", function (data) {
        magitShowError(data.responseText);
    }, function (data) {
        if (data.CONTENT !== "") {
            document.getElementById('pr_textEditor').innerHTML = '<b>' + realFileName + '\'s content:</b></br>' +
                '<textarea class="form-control" rows="10" style="min-width: 250px; min-height: 10%; height:100%; resize:vertical;" id="content" rows="3" disabled>' + data.CONTENT + '</textarea>';
        }
    });
}

function getIcon(icon) {
    switch (icon) {
        case "LIST_NEW":
            return "fa fa-plus-circle";
        case "LIST_DELETED":
            return "fa fa-minus-circle"
        case "LIST_CHANGED":
            return "fa fa-pencil";
    }
}

function showCommitChanges(commitSHA1) {
    makeAjaxRequests({
        "repo_id": window.repo_ID,
        "user": "null",
        "pr_id": window.pr_ID,
        "sha_1": commitSHA1
    }, "commit_changes", function (data) {
        magitShowError(data.responseText);
    }, function (data) {
        $("#modified_files_list").html("");
        $("#commits_files_viewer").css('display', 'block');
        Object.keys(data).forEach(function (file) {
            let icon = data[file];
            file = file.replace("\\","\\\\");
            let realFileName = file.replace("repository_" + window.repo_ID + "\\", "");
            let i_filename = "<button style='border: none;padding: 0;background: none;' " +
                "onclick=\"showFileContent('" + file + "','" + commitSHA1 + "')\"><i class='" + getIcon(icon) + "' aria-hidden='true'></i> " +
                realFileName + "</button></br>";
            $("#modified_files_list").append(i_filename);
        })
    });
}

function addPRCommitToTable(commit, id, repositoryID, user, tableID) {
    $(tableID + ' tr:last')
        .after('<tr ' + addTagsForTR("pr_commit_" + id, "showCommitChanges('" + commit.WSA_SINGLE_COMMIT_SHA1_KEY + "');") + '><td>' +
            (id) +
            '</td><td>' +
            commit.WSA_SINGLE_COMMIT_SHA1_KEY +
            '</td><td>' +
            commit.WSA_SINGLE_COMMIT_COMMENT_KEY +
            '</td><td>' +
            commit.WSA_SINGLE_COMMIT_DATE_KEY +
            '</td><td>' +
            commit.WSA_SINGLE_COMMIT_CREATOR_KEY +
            '</td></tr>');
}

function fetchCommits(data) {
    let commits = jQuery.parseJSON(data.PR_ALL_COMMITS);
    let i = 1;
    Object.keys(commits).forEach(function (key) {
        addPRCommitToTable(jQuery.parseJSON(commits[key]), i++, $("#repo_id").text(), $("#selected_user").text(), "#pr_commits-table");
    });
}

$(function () {
    $.ajax({
        async: true,
        data: {
            "pr_id": window.pr_ID,
            "repository_id": $("#repo_id").text(),
            "user_id": $("#selected_user").text()
        },
        url: "pullRequest_data",
        timeout: 2000,
        error: function () {

        },
        success: function (data) {
            window.repo_ID = $("#repo_id").text();
            fetchCommits(data);
        }
    });

    $("#show_all_pull_requests").click(function (e) {
        e.preventDefault();
        $("#pull_requests_outter_div").css('display', 'none');
        $('#pull_requests_inner_div').css('display', 'block');
    });

    $("#approve_pr").click(function () {
        sendApprovePR(window.pr_ID, $("#repo_id").text());
    });

    $("#send_reject_pr").click(function () {
        $("#reject_pull_request_close").click();
        rejectPR(window.pr_ID, $("#repo_id").text());
    });
});