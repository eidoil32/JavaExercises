function getUrlParameter(sParam) {
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
}

function hideMessage() {
	window.history.replaceState({}, document.title,'index.html'); // hide url parameters
	$("#div_message").css('display','none');
}

function noMessages() {
	$("#notification-counter").html("NOTIFICATION AREA <span class='badge badge-secondary'>0</span>");
	$('#notfiaction-table-body').html('<tr align="center"><td colspan="6">No messages are found.</td></tr>');
}

function GetCookieValue(name) {
    var found = document.cookie.split(';').filter(c => c.trim().split("=")[0] === name);
    return found.length > 0 ? found[0].split("=")[1] : null;
}

function fetchMessages(messages) {
	var counterMessages = 0, messagesRows = "";
	$.each(messages, function(key, value) {
		var trID = "message_" + key;
        messagesRows += '<tr id="' + trID + '"><td>' +
                (parseInt(key) + 1) +
                '</td><td>' +
				messages[key].MESSAGE_KEY_TYPE +
				'</td><td>' +
				messages[key].MESSAGE_KEY_REPOSITORY +
				'</td><td>' +
				messages[key].MESSAGE_KEY_CONTENT +
				'</td><td>' +
				messages[key].MESSAGE_KEY_TIME +
				'</td><td>' +
				messages[key].MESSAGE_KEY_CREATOR +
                '</td></tr>';
		counterMessages++;
    });
	
	if (counterMessages > 0) {
		$("#notfiaction-table-body").html(messagesRows);
		$("#notification-counter")
			.html("NOTIFICATION AREA <span class='badge badge-secondary'>" + counterMessages + "</span>")
			.css("display","block");
	} else {
		noMessages();
	}
}

function hasMessages(json) {
	try {
		jQuery.parseJSON(json);
		return true;
	} catch(error) {
		return false;
	}
}

function checkForMessages(currentMessages) {
	$.ajax({
		async: true, 
		data: {},
		url: "check_messages",
		timeout: 2000,
		error: function(data) {
			
		},
		success: function(data) {
			var messages = null;
			if (hasMessages(data)) {
				messages = jQuery.parseJSON(data);
				if (currentMessages == null || JSON.stringify(messages) !== JSON.stringify(currentMessages)) {
					fetchMessages(messages);
				}
			} else {
				noMessages();
			}
			const refInterval = window.setTimeout(checkForMessages, 3000, messages); // 3 seconds
		} 
	});
}

function logoutUser() {
	$.ajax({
		async: true, 
		data: {},
		url: "logout",
		timeout: 2000,
		error: function(data) {
			
		},
		success: function(data) {
			window.location.replace("login.html");
		} 
	});
}

$(function() { // onload...do
	checkForMessages(null);

	$("#sign_out_button").click(function() {
		logoutUser();
	});

	var user_chosen = getUrlParameter('user');
	if (user_chosen == null) {
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
		$("#page-content").load("users.html?user=" + user_chosen);
	}
})

function activeOnlyThis(item) {
	var items = ["menu-my-repositories", "menu-users"];
	for (var i = 0; i < 2; i++) {
		document.getElementById(items[i]).className = "";
	}

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