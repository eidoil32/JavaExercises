function loadCommitData(data, rootFolderID) {
    var tree = jQuery.parseJSON(data);

    Object.keys(tree).forEach(function (key) {
        if (checkFolder(tree[key])) {
            var folderID = "folder_" + key;
            createFolder(key, rootFolderID, folderID);
            loadCommitData(tree[key], folderID);
        } else {
            createNode(key, rootFolderID, "fa fa-file");
        }
    })
}

function createFileViewer(filename, content) {
    if (content !== "") {
        var viewer = '<b>' + filename.getElementsByTagName("a")[0].innerText + '\'s content:</b></br>' +
            '<textarea class="form-control" rows="10" style="min-width: 250px; min-height: 10%; height:100%; resize:vertical;" id="content" rows="3" disabled>' + content + '</textarea>';


        document.getElementById('textEditor').innerHTML = viewer;
    }
}


function getFullPath(file, counter) {
    var rootFolder = file.parentElement.parentElement;
    if (rootFolder.id === "filetree_div")
        return file.getElementsByTagName("a")[0].innerText;
    return getFullPath(rootFolder) + "," + file.getElementsByTagName("a")[0].innerText;
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
        error: function () {
            console.error("Error from server!");
        },
        success: function (data) {
            createFileViewer(file, data, path, repository, user);
        }
    });
}

function createFirstNode() {
    return $('#filetree_div').jstree().create_node(null, "Root Folder");
}

function makeItTree(repository, user, commitSHA) {
    $('#filetree_div')
        .jstree()
        .on('changed.jstree', function (e, data) {
            loadFileData(document.getElementById(data.selected), repository, user, commitSHA);
        }).jstree();
}

function sendApprovePR() {

}

function rejectPR() {

}

function fetchCommits(data) {
    let commits = jQuery.parseJSON(data);
    let i = 1;
    Object.keys(commits).forEach(function (key) {
        addCommitToTable(commits[key], i++,$("#repo_id").text(),$("#selected_user").text(), "#pr_commits-table");
    });
}

$(function () {
    $.ajax({
        async: true,
        data: {
            "pr_id": window.pr_ID,
            "repository_id": $("#repo_id").text(),
            "user_id":  $("#selected_user").text()
        },
        url: "pullRequest_data",
        timeout: 2000,
        error: function () {

        },
        success: function (data) {
/*            loadCommitData(data, "root_folder");
            makeItTree(repository, user, commitSHA);*/
            //fetchCommits(data);
            console.log(data);
        }
    });

    $("#show_all_pull_requests").click(function (e) {
        e.preventDefault();
        $("#pull_requests_outter_div").css('display', 'none');
        $('#pull_requests_inner_div').css('display', 'block');
    });

    $("#approve_pr").click(function () {
        sendApprovePR();
    });

    $("#send_reject_pr").click(function () {
        rejectPR();
    });
});