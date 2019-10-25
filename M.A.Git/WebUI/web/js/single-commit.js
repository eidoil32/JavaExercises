function loadCommitData(data, rootFolderID) {
    let tree = jQuery.parseJSON(data);

    Object.keys(tree).forEach(function (key) {
        if (checkFolder(tree[key])) {
            let folderID = "folder_" + key;
            createFolder(key, rootFolderID, folderID);
            loadCommitData(tree[key], folderID);
        } else {
            createNode(key, rootFolderID, "fa fa-file");
        }
    })
}

function createFileViewer(filename, content) {
    if (content !== "") {
        document.getElementById('textEditor').innerHTML = '<b>' + filename.getElementsByTagName("a")[0].innerText + '\'s content:</b></br>' +
            '<textarea class="form-control" rows="10" style="min-width: 250px; min-height: 10%; height:100%; resize:vertical;" id="content" rows="3" disabled>' + content + '</textarea>';
    }
}


function getFullPath(file, counter) {
    let rootFolder = file.parentElement.parentElement;
    if (rootFolder.id === "filetree_div")
        return file.getElementsByTagName("a")[0].innerText;
    return getFullPath(rootFolder) + "," + file.getElementsByTagName("a")[0].innerText;
}

function loadFileData(file, repository, user, commitSHA) {
    let path = getFullPath(file);
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
    $('#filetree_div').on('changed.jstree', function (e, data) {
        loadFileData(document.getElementById(data.selected), repository, user, commitSHA);
    }).jstree();
}

$(function () {
    let commitSHA = $("#single_commit_sha1").html(),
        repository = $("#single_commit_repo_id").html(),
        user = $("#single_commit_user").html();

    $("#show_all_commits_btn").click(function () {
        $("#single_commit_show").css('display', 'none');
        $("#all_commits_show").css('display','block');
    });

    if (commitSHA != null) {
        $.ajax({
            async: true,
            data: {
                "sha-1": commitSHA,
                "repository_id": repository,
                "user_id": user
            },
            url: "single_commit",
            timeout: 2000,
            error: function () {
                console.error("Error from server!");
            },
            success: function (data) {
                loadCommitData(data, "root_folder");
                makeItTree(repository, user, commitSHA);
            }
        });
    }
});