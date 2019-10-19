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


function changeElementStyles(element, background_color, text_color, text_weight) {
    element.style.backgroundColor = background_color;
    element.style.fontWeight = text_weight;
    element.style.color = text_color;
}

function elementHover(element) {
    changeElementStyles(element, "#f9f9f9", "#39ADB4", "bold");
}

function elementOut(element) {
    changeElementStyles(element, "#ffffff", "black", "normal");
}

function addTagsForTR(id, url) {
    return 'id="' + id + '" onmouseover="elementHover(' + id + ')" onmouseout="elementOut(' + id + ')" onclick="' + url + '" style="cursor: pointer;"';
}

function showCustomAlert(message, type) {
    document.getElementById("div_message").style.display = "block";
    document.getElementById("div_message_" + type).style.display = "block";
    document.getElementById("message_" + type).innerHTML = message;
}

function magitShowError(message) {
    showCustomAlert(message, "failed");
}

function magitShowSuccess(message) {
    showCustomAlert(message, "success");
}

$(document).ready(function() {
    /* Mobile menu */
    $('.mobile-menu-icon').click(function() {
        $('.templatemo-left-nav').slideToggle();
    });

    /* Close the widget when clicked on close button */
    $('.templatemo-content-widget .fa-times').click(function() {
        $(this).parent().slideUp(function() {
            $(this).hide();
        });
    });

    $("#modal-popups").load("addons/modals.html");

    //window.location.pathname - show current page;
    $("#form-load-repository").submit(function() {
        var file1 = this[0].files[0];

        var formData = new FormData();
        formData.append("fake-key-1", file1);


        $.ajax({
            method: 'POST',
            async: true,
            data: formData,
            url: "/load-repository",
            processData: false, // Don't process the files
            contentType: false, // Set content type to false as jQuery will tell the server its a query string request
            timeout: 4000,
            error: function(e) {
                console.error("Failed to submit");
                $("#result").text("Failed to get result from server " + e);
            },
            success: function(r) {
                $("#result").text(r);
            }
        });

        // return value of the submit operation
        // by default - we'll always return false so it doesn't redirect the user.
        return false;
    })

    var getUrlParameter = function getUrlParameter(sParam) {
        var sPageURL = window.location.search.substring(1),
            sURLVariables = sPageURL.split('&'),
            sParameterName,
            i;

        for (i = 0; i < sURLVariables.length; i++) {
            sParameterName = sURLVariables[i].split('=');

            if (sParameterName[0] === sParam) {
                return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
            }
        }
    };

    var file_is_uploaded = getUrlParameter('file_upload');
    if (file_is_uploaded != null) {
        if (file_is_uploaded == "success") {
            magitShowSuccess("XML File uploaded successfully");
        } else {
            magitShowError(file_is_uploaded.replace(/_/g, " "));
        }
    }
});