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

function checkFolder(data) {
    try {
        jQuery.parseJSON(data);
        return true;
    } catch (err) {
        return false;
    }
}

function createFolder(text, parent, newID) {
    var parentUL = document.getElementById(parent);
    var li = document.createElement("li");
    li.appendChild(document.createTextNode(text));
    var newUL = document.createElement("ul");
    newUL.id = newID;
    li.appendChild(newUL);
    li.setAttribute("data-jstree", '{"icon" : "fa fa-folder"}');
    parentUL.appendChild(li);
}

function createNode(text, parent) {
    var li = document.createElement("li");
    var parentUL = document.getElementById(parent);
    li.appendChild(document.createTextNode(text));
    parentUL.appendChild(li);
    li.setAttribute("data-jstree", '{"icon" : "fa fa-file"}');
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
        error: function() {
            console.error("Error from server!");
        },
        success: function(data) {
            createFileViewer(file, data);
        }
    });
}

function createFirstNode() {
    return $('#filetree_div').jstree().create_node(null, "Root Folder");
}

function makeItTree(repository, user, commitSHA) {
    $('#filetree_div').jstree();
    $('#filetree_div').on('changed.jstree', function(e, data) {
        loadFileData(document.getElementById(data.selected), repository, user, commitSHA);
    }).jstree();
}

$(function() {
    var commitSHA = GetCookieValue('sha1');
    var repository = GetCookieValue('repository_id');
    var user = GetCookieValue('user_id');

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
            error: function() {
                console.error("Error from server!");
            },
            success: function(data) {
                loadCommitData(data, "root_folder");
                makeItTree(repository, user, commitSHA);
            }
        });
    }
});