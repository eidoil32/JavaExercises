function hideMessage() {
	window.history.replaceState({}, document.title,'index.html'); // hide url parameters
	$("#div_message").css('display','none');
}

function GetCookieValue(name) {
    var found = document.cookie.split(';').filter(c => c.trim().split("=")[0] === name);
    return found.length > 0 ? found[0].split("=")[1] : null;
}

$(function() { // onload...do
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
	var user_choosen = getUrlParameter('user');
	if (user_choosen == null) {
		var currentPageCookie = GetCookieValue("current-page");
		if (currentPageCookie != null && currentPageCookie !== "null") {
			var pageElement = GetCookieValue("element");
			if (pageElement !== "null") {
				activeOnlyThis(pageElement);
			}
			$("#page-content").load(currentPageCookie); 
		} else {
			$("#page-content").load("main.html");
		}
	} else {
		$("#page-content").load("users.html?user=" + user_choosen);
	}
})

function activeOnlyThis(item) {
	var items = ["menu-my-repositories", "menu-preferences", "menu-users"];
	items.forEach((menuItem, index) => {
		document.getElementById(menuItem).className = "";
	});
	if (item != null) {
		document.getElementById(item).className = "active";
	}
}

function selectMenuItem(element, link) {
	document.cookie = "current-page=" + link;
	if (element == null) {
		activeOnlyThis(null);
		document.cookie = "element=null";
	} else {
		document.cookie = "element=" + element.id;
		activeOnlyThis(element.id);
	}
	$("#page-content").load(link); 
}